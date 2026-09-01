package com.avishai.ui.panels;

import com.avishai.domain.Survey;
import com.avishai.domain.SurveyParticipant;
import com.avishai.manager.SurveyListener;
import com.avishai.manager.SurveyManager;
import com.avishai.ui.components.BasePanel;
import com.avishai.ui.components.Theme;
import com.avishai.ui.dialogs.SurveyResultsDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

public class HistoryPanel extends BasePanel implements SurveyListener {
    private JPanel listContainer;
    private final DateTimeFormatter timeFormatter;

    public HistoryPanel() {
        super("Survey History");
        this.timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());
        SurveyManager.getInstance().addListener(this);
        loadHistory();
    }

    @Override
    protected JComponent buildContent() {
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }

    @Override
    protected JPanel buildControlBar() {
        return null;
    }

    private void loadHistory() {
        listContainer.removeAll();
        for (Survey survey : SurveyManager.getInstance().getSurveyHistory()) {
            listContainer.add(createCard(survey));
            listContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createCard(Survey survey) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String date = timeFormatter.format(Instant.ofEpochMilli(survey.getStartTime()));

        Collection<SurveyParticipant> participants = survey.getParticipants().values();
        int totalParticipants = participants.size();
        long completedParticipants = participants.stream()
                .filter(SurveyParticipant::isCompleted)
                .count();
        int questionsCount = survey.getQuestions() != null ? survey.getQuestions().size() : 0;

        JLabel title = new JLabel("Survey - " + date);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));

        JLabel info = new JLabel(String.format("Questions: %d | Participants: %d | Completed: %d",
                questionsCount, totalParticipants, completedParticipants));
        info.setForeground(Color.GRAY);

        card.add(title, BorderLayout.NORTH);
        card.add(info, BorderLayout.SOUTH);

        Color origBg = card.getBackground();

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(Theme.PRIMARY_BLUE);
                title.setForeground(Color.WHITE);
                info.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(origBg);
                title.setForeground(UIManager.getColor("Label.foreground"));
                info.setForeground(Color.GRAY);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                new SurveyResultsDialog(SwingUtilities.getWindowAncestor(HistoryPanel.this), survey)
                        .setVisible(true);
            }
        };

        card.addMouseListener(adapter);
        title.addMouseListener(adapter);
        info.addMouseListener(adapter);

        return card;
    }

    @Override
    public void onSurveyScheduled(long executeTimeMillis) {
    }

    @Override
    public void onSurveyStarted() {
    }

    @Override
    public void onParticipantUpdated() {
    }

    @Override
    public void onSurveyEnded(Survey completedSurvey) {
        SwingUtilities.invokeLater(this::loadHistory);
    }
}
