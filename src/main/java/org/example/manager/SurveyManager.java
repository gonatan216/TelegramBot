package org.example.manager;

import org.example.bot.TelegramSender;
import org.example.domain.CommunityUser;
import org.example.domain.Question;
import org.example.domain.Survey;
import org.example.domain.SurveyParticipant;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class SurveyManager {
    private static final Logger log = LoggerFactory.getLogger(SurveyManager.class);
    private static volatile SurveyManager instance;
    private final ScheduledExecutorService scheduler;
    private final List<SurveyListener> listeners;
    private final Map<String, PollTracker> activePolls = new ConcurrentHashMap<>();
    private final List<Survey> surveyHistory = new CopyOnWriteArrayList<>();
    private Survey activeSurvey;
    private ScheduledFuture<?> reminderTask;
    private ScheduledFuture<?> closeTask;

    @Getter
    private long scheduledExecuteTime;
    @Setter
    private TelegramSender telegramSender;

    private record PollTracker(Long chatId, Integer messageId, int questionIndex) {}

    private SurveyManager() {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public static SurveyManager getInstance() {
        if (instance == null) {
            synchronized (SurveyManager.class) {
                if (instance == null) {
                    instance = new SurveyManager();
                }
            }
        }
        return instance;
    }

    public void addListener(SurveyListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    public List<Survey> getSurveyHistory() {
        return Collections.unmodifiableList(this.surveyHistory);
    }

    public synchronized void scheduleSurvey(Survey survey, int delayMinutes) {
        if (this.activeSurvey != null) {
            throw new IllegalStateException("A survey is already active.");
        }
        this.activeSurvey = survey;
        this.scheduledExecuteTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(delayMinutes);
        this.activeSurvey.getParticipants().putAll(
                CommunityManager.getInstance().getAllUsers().stream()
                        .collect(Collectors.toMap(
                                CommunityUser::getChatId,
                                user -> new SurveyParticipant(
                                        user,
                                        new HashMap<>(),
                                        false
                                )
                        ))
        );
        this.listeners.forEach(listener -> listener.onSurveyScheduled(this.scheduledExecuteTime));

        this.scheduler.schedule(
                () -> {
                    this.activeSurvey.setActive(true);
                    log.info("Survey Started");
                    this.reminderTask = this.scheduler.schedule(this::sendReminders, 3, TimeUnit.MINUTES);
                    this.closeTask = this.scheduler.schedule(this::closeSurvey, 5, TimeUnit.MINUTES);
                    this.dispatchQuestions();
                    SwingUtilities.invokeLater(() ->
                            this.listeners.forEach(SurveyListener::onSurveyStarted)
                    );
                },
                delayMinutes,
                TimeUnit.MINUTES
        );
    }

    private synchronized void dispatchQuestions() {
        this.activePolls.clear();
        if (this.activeSurvey != null && this.telegramSender != null) {
            int closeDateUnix = (int) ((System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)) / 1000);
            for (SurveyParticipant participant : this.activeSurvey.getParticipants().values()) {
                for (int i = 0; i < this.activeSurvey.getQuestions().size(); i++) {
                    Message msg = this.telegramSender.sendPoll(
                            participant.getCommunityUser().getChatId(),
                            this.activeSurvey.getQuestions().get(i),
                            closeDateUnix
                    );
                    if (msg != null && msg.getPoll() != null) {
                        this.activePolls.put(
                                msg.getPoll().getId(),
                                new PollTracker(participant.getCommunityUser().getChatId(), msg.getMessageId(), i)
                        );
                    }
                }
            }
        }
    }

    public synchronized void registerPollAnswer(String pollId, Long userId, List<Integer> optionIds) {
        if (this.activeSurvey == null || !this.activeSurvey.isActive()) return;
        if (optionIds.isEmpty()) return;

        var tracker = this.activePolls.get(pollId);
        if (tracker == null) return;

        var participant = this.activeSurvey.getParticipants().get(userId);
        if (participant == null || participant.isCompleted()) return;

        Question question = this.activeSurvey.getQuestions().get(tracker.questionIndex());
        String selectedOption = question.getOptions().get(optionIds.getFirst());

        participant.getAnswers().putIfAbsent(question, selectedOption);

        if (participant.getAnswers().size() == this.activeSurvey.getQuestions().size()) {
            GlobalStatusManager.getInstance().showSuccess(
                    participant.getCommunityUser().getFirstName() + " completed the survey!"
            );
        } else GlobalStatusManager.getInstance().showInfo(
                participant.getCommunityUser().getFirstName() + " answered a question...",
                Color.CYAN, 2000
        );

        if (participant.getAnswers().size() == this.activeSurvey.getQuestions().size()) {
            participant.setCompleted(true);
        }

        SwingUtilities.invokeLater(() -> this.listeners.forEach(SurveyListener::onParticipantUpdated));

        if (this.telegramSender != null) {
            this.telegramSender.stopPoll(tracker.chatId(), tracker.messageId());
        }

        boolean allDone = this.activeSurvey
                .getParticipants()
                .values()
                .stream()
                .allMatch(SurveyParticipant::isCompleted);
        if (allDone) this.closeSurvey();
    }

    private synchronized void sendReminders() {
        if (this.activeSurvey != null && this.activeSurvey.isActive()) {
            for (SurveyParticipant participant : this.activeSurvey.getParticipants().values()) {
                if (!participant.isCompleted() && this.telegramSender != null) {
                    this.telegramSender.sendMessage(
                            participant.getCommunityUser().getChatId(),
                            "Reminder: You have 2 minutes left to complete the survey!"
                    );
                }
            }
        }
    }

    public synchronized void closeSurvey() {
        if (this.activeSurvey == null || !this.activeSurvey.isActive()) {
            return;
        }
        this.activeSurvey.setActive(false);

        if (this.reminderTask != null && !this.reminderTask.isDone()) {
            this.reminderTask.cancel(true);
        }
        if (this.closeTask != null && !this.closeTask.isDone()) {
            this.closeTask.cancel(true);
        }

        Survey completedSurvey = this.activeSurvey;
        this.surveyHistory.add(completedSurvey);
        this.activeSurvey = null;

        SwingUtilities.invokeLater(() -> this.listeners.forEach(l -> l.onSurveyEnded(completedSurvey)));
    }

    public synchronized Survey getActiveSurvey() {
        return this.activeSurvey;
    }
}
