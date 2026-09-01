package com.avishai.ui.panels;

import com.avishai.config.Config;
import com.avishai.domain.Question;
import com.avishai.domain.Survey;
import com.avishai.manager.CommunityManager;
import com.avishai.manager.GlobalStatusManager;
import com.avishai.manager.SurveyManager;
import com.avishai.service.ChatGPTService;
import com.avishai.ui.components.BasePanel;
import com.avishai.ui.components.QuestionInputPanel;
import com.avishai.ui.components.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CreateSurveyPanel extends BasePanel {
    private final List<QuestionInputPanel> cards = new ArrayList<>();
    private JPanel questionsContainer;
    private JSpinner delaySpinner;
    private JButton btnAiGen;
    private JButton btnStartSurvey;

    public CreateSurveyPanel() {
        super("Create Survey");
        addCard();
    }

    @Override
    protected JComponent buildContent() {
        questionsContainer = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(questionsContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    @Override
    protected JPanel buildControlBar() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        btnAiGen = new JButton("Generate with AI");
        btnAiGen.setBackground(Theme.PRIMARY_BLUE);
        btnAiGen.setForeground(Color.WHITE);

        JButton btnAddQuestion = new JButton("Add Question");

        delaySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 60, 1));
        delaySpinner.setPreferredSize(new Dimension(70, 26));

        btnStartSurvey = new JButton("Start Survey");
        btnStartSurvey.setBackground(Theme.SUCCESS_GREEN);
        btnStartSurvey.setForeground(Color.WHITE);

        btnAiGen.addActionListener(e -> handleAiGeneration());
        btnAddQuestion.addActionListener(e -> addCard());
        btnStartSurvey.addActionListener(e -> startSurvey());

        buttonPanel.add(btnAiGen);
        buttonPanel.add(btnAddQuestion);
        buttonPanel.add(new JLabel("Delay (mins):"));
        buttonPanel.add(delaySpinner);
        buttonPanel.add(btnStartSurvey);

        return buttonPanel;
    }

    private boolean hasPreconditionErrors() {
        if (SurveyManager.getInstance().getActiveSurvey() != null) {
            GlobalStatusManager.getInstance().showError("Error: A survey is already active.");
            return true;
        }
        int minUsers = Config.MIN_USERS_TO_START_SURVEY;
        if (CommunityManager.getInstance().getCommunitySize() < minUsers) {
            GlobalStatusManager.getInstance().showError(
                    "Error: At least " + minUsers + " community member required."
            );
            return true;
        }
        return false;
    }

    private void handleAiGeneration() {
        if (hasPreconditionErrors()) {
            return;
        }

        String topic = JOptionPane.showInputDialog(
                this,
                "Enter a topic for the survey:",
                "AI Generation",
                JOptionPane.QUESTION_MESSAGE
        );

        if (topic == null || topic.trim().isEmpty()) {
            return;
        }

        GlobalStatusManager.getInstance().setStableStatus(
                "Generating survey via AI... Please wait.",
                Color.ORANGE
        );
        btnAiGen.setEnabled(false);
        btnStartSurvey.setEnabled(false);

        ChatGPTService.getInstance().generateSurvey(topic)
                .thenAccept(generatedQuestions -> SwingUtilities.invokeLater(() -> {
                    btnAiGen.setEnabled(true);
                    btnStartSurvey.setEnabled(true);
                    cards.clear();
                    renderCards();
                    for (Question question : generatedQuestions) {
                        addCard();
                        QuestionInputPanel card = cards.getLast();
                        card.populateQuestion(question);
                    }
                    GlobalStatusManager.getInstance().setReady();
                    GlobalStatusManager.getInstance().showSuccess("AI Generation complete!");
                }))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        btnAiGen.setEnabled(true);
                        btnStartSurvey.setEnabled(true);
                        GlobalStatusManager.getInstance().setReady();
                        GlobalStatusManager.getInstance().showError("Error generating from AI.");
                    });
                    return null;
                });
    }

    private void renderCards() {
        questionsContainer.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        for (int i = 0; i < cards.size(); i++) {
            QuestionInputPanel card = cards.get(i);
            card.setQuestionTitle(i + 1);
            card.setDeleteEnabled(cards.size() > 1);
            gbc.gridy = i;
            questionsContainer.add(card, gbc);
        }

        JPanel filler = new JPanel();
        GridBagConstraints fillerGbc = new GridBagConstraints();
        fillerGbc.gridy = cards.size();
        fillerGbc.weighty = 1.0;
        fillerGbc.fill = GridBagConstraints.BOTH;
        questionsContainer.add(filler, fillerGbc);

        questionsContainer.revalidate();
        questionsContainer.repaint();
    }

    private void addCard() {
        if (cards.size() < 3) {
            QuestionInputPanel[] panelRef = new QuestionInputPanel[1];
            panelRef[0] = new QuestionInputPanel(cards.size() + 1, () -> {
                cards.remove(panelRef[0]);
                renderCards();
            });
            cards.add(panelRef[0]);
            renderCards();
        }
    }

    private void startSurvey() {
        if (hasPreconditionErrors()) return;

        for (QuestionInputPanel card : cards) {
            if (!card.validateCard()) {
                GlobalStatusManager.getInstance().showError("Error: Please complete all required fields.");
                return;
            }
        }

        List<Question> questions = new ArrayList<>();
        for (QuestionInputPanel card : cards) {
            card.buildQuestion().ifPresent(questions::add);
        }

        Survey newSurvey = Survey.builder()
                .questions(questions)
                .startTime(System.currentTimeMillis())
                .isActive(true)
                .build();

        int delayMinutes = (Integer) delaySpinner.getValue();
        SurveyManager.getInstance().scheduleSurvey(newSurvey, delayMinutes);

        GlobalStatusManager.getInstance().setReady();
        GlobalStatusManager.getInstance().showSuccess("Survey successfully scheduled!");
    }
}
