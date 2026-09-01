package org.example.ui.dialogs;

import org.example.domain.Question;
import org.example.domain.Survey;
import org.example.ui.components.Theme;

import javax.swing.*;
import java.awt.*;

public class SurveyPreviewDialog extends JDialog {
    public SurveyPreviewDialog(Window parent, Survey survey) {
        super(parent, "Survey Preview");
        setModal(true);
        setSize(700, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(createCenterScrollPane(survey), BorderLayout.CENTER);
        add(createSouthPanel(), BorderLayout.SOUTH);
    }

    private JScrollPane createCenterScrollPane(Survey survey) {
        java.util.List<JPanel> cards = new java.util.ArrayList<>();
        if (survey != null && survey.getQuestions() != null) {
            for (Question q : survey.getQuestions()) cards.add(createQuestionCard(q));
        }
        return Theme.createVerticalScrollPane(cards);
    }

    private JPanel createQuestionCard(Question question) {
        JPanel card = Theme.createQuestionCard(question);
        if (question.getOptions() != null) {
            for (String option : question.getOptions()) {
                JPanel optWrapper = new JPanel(new BorderLayout(10, 0));

                JLabel dot = new JLabel("•");
                dot.setFont(dot.getFont().deriveFont(18f));
                dot.setForeground(Theme.PRIMARY_BLUE);

                JLabel optLabel = new JLabel(option);
                optLabel.setFont(optLabel.getFont().deriveFont(14f));

                optWrapper.add(dot, BorderLayout.WEST);
                optWrapper.add(optLabel, BorderLayout.CENTER);

                card.add(optWrapper);
                card.add(Box.createRigidArea(new Dimension(0, 4)));
            }
        }

        return card;
    }

    private JPanel createSouthPanel() {
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        southPanel.add(closeBtn);
        return southPanel;
    }
}
