package com.avishai.bot;

import com.avishai.config.Config;
import com.avishai.domain.CommunityUser;
import com.avishai.domain.Question;
import com.avishai.manager.CommunityManager;
import com.avishai.manager.SurveyManager;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.polls.StopPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
public class Bot extends TelegramLongPollingBot implements TelegramSender {
    private final String botUsername;

    public Bot(String botUsername, String botToken) {
        super(botToken);
        this.botUsername = botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            onTextReceived(update);
        } else if (update.hasPollAnswer()) {
            onPollAnswerReceived(update);
        }
    }

    public void onTextReceived(Update update) {
        if (!update.getMessage().hasText()) return;
        String text = update.getMessage().getText();

        if (Config.ACTIVATION_WORDS.contains(text)) {
            Long chatId = update.getMessage().getChatId();
            var userData = update.getMessage().getFrom();
            CommunityUser communityUser = CommunityUser.builder()
                    .chatId(chatId)
                    .timeAdded(System.currentTimeMillis())
                    .firstName(userData.getFirstName())
                    .lastName(userData.getLastName())
                    .username(userData.getUserName())
                    .build();

            CommunityManager.getInstance().tryAddUser(communityUser);
        }
    }

    public void onPollAnswerReceived(Update update) {
        PollAnswer pollAnswer = update.getPollAnswer();
        String pollId = pollAnswer.getPollId();
        Long userId = pollAnswer.getUser().getId();
        List<Integer> optionIds = pollAnswer.getOptionIds();

        SurveyManager.getInstance().registerPollAnswer(pollId, userId, optionIds);
    }

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chatId {}: {}", chatId, e.getMessage(), e);
        }
    }

    @Override
    public Message sendPoll(Long chatId, Question question, Integer closeDateUnix) {
        SendPoll sendPoll = new SendPoll();
        sendPoll.setChatId(chatId.toString());
        sendPoll.setQuestion(question.getText());
        sendPoll.setOptions(question.getOptions());
        sendPoll.setIsAnonymous(false);
        sendPoll.setCloseDate(closeDateUnix);

        try {
            return execute(sendPoll);
        } catch (TelegramApiException e) {
            log.error("Failed to send poll to chatId {}: {}", chatId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void stopPoll(Long chatId, Integer messageId) {
        StopPoll stopPoll = new StopPoll();
        stopPoll.setChatId(chatId.toString());
        stopPoll.setMessageId(messageId);

        try {
            execute(stopPoll);
        } catch (TelegramApiException e) {
            log.error("Failed to stop poll for chatId {}: {}", chatId, e.getMessage(), e);
        }
    }
}
