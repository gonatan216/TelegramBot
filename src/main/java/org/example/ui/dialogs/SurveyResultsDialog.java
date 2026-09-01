package org.example.ui.dialogs;

import org.example.domain.Question;
import org.example.domain.Survey;
import org.example.ui.components.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SurveyResultsDialog extends JDialog {
    public SurveyResultsDialog(Window parent, Survey survey) {
        super(parent, "Survey Results");
        setModal(true);
        setSize(700, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(createCenterScrollPane(survey), BorderLayout.CENTER);
        add(createSouthPanel(), BorderLayout.SOUTH);
    }

    private JScrollPane createCenterScrollPane(Survey survey) {
        List<JPanel> cards = new java.util.ArrayList<>();
        if (survey != null && survey.getQuestions() != null) {
            for (Question q : survey.getQuestions()) cards.add(createQuestionCard(q, survey));
        }
        return Theme.createVerticalScrollPane(cards);
    }

    private JPanel createQuestionCard(Question question, Survey survey) {
        long totalVotes = survey.getParticipants().values().stream()
                .filter(p -> p.getAnswers().containsKey(question))
                .count();

        Map<String, Long> votesPerOption = survey.getParticipants().values().stream()
                .filter(p -> p.getAnswers().containsKey(question))
                .collect(Collectors.groupingBy(
                        p -> p.getAnswers().get(question),
                        Collectors.counting()
                ));

        List<String> sortedOptions = question.getOptions().stream()
                .sorted(Comparator.comparingLong((String option) ->
                        votesPerOption.getOrDefault(option, 0L)).reversed())
                .toList();

        long maxVotes = sortedOptions.isEmpty() ? 0 : votesPerOption.getOrDefault(sortedOptions.getFirst(), 0L);

        JPanel card = Theme.createQuestionCard(question);
        for (String option : sortedOptions) {
            long votes = votesPerOption.getOrDefault(option, 0L);
            double pct = totalVotes == 0 ? 0 : (votes * 100.0) / totalVotes;

            JPanel optionWrapper = new JPanel(new BorderLayout());
            optionWrapper.add(new JLabel(option), BorderLayout.WEST);
            optionWrapper.add(new JLabel(String.format("%d votes (%.1f%%)", votes, pct)), BorderLayout.EAST);

            JProgressBar pb = new JProgressBar(0, 100);
            pb.setValue((int) pct);
            pb.putClientProperty("JProgressBar.arc", 999);
            pb.setPreferredSize(new Dimension(pb.getPreferredSize().width, 10));

            if (votes == maxVotes && maxVotes > 0) {
                pb.setForeground(Theme.PRIMARY_BLUE);
            } else {
                pb.setForeground(Color.GRAY);
            }

            card.add(optionWrapper);
            card.add(Box.createRigidArea(new Dimension(0, 2)));
            card.add(pb);
            card.add(Box.createRigidArea(new Dimension(0, 6)));
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
