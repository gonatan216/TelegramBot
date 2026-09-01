package com.avishai.ui.panels;

import com.avishai.domain.Survey;
import com.avishai.domain.SurveyParticipant;
import com.avishai.manager.GlobalStatusManager;
import com.avishai.manager.SurveyListener;
import com.avishai.manager.SurveyManager;
import com.avishai.ui.components.BasePanel;
import com.avishai.ui.components.Theme;
import com.avishai.ui.dialogs.SurveyPreviewDialog;
import com.avishai.ui.dialogs.SurveyResultsDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class ActiveSurveyPanel extends BasePanel implements SurveyListener {
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JLabel completedLabel;
    private JLabel pendingLabel;
    private JLabel timeLeftLabel;
    private Timer uiTimer;
    private long scheduledExecuteTime;

    public ActiveSurveyPanel() {
        super("Live Survey Progress");
        addHeaderAction(buildHeaderActionComponent());
        SurveyManager.getInstance().addListener(this);
    }

    private JPanel buildHeaderActionComponent() {
        JButton btnShowSurvey = new JButton("Show Survey");
        btnShowSurvey.setBackground(Theme.PRIMARY_BLUE);
        btnShowSurvey.setForeground(Color.WHITE);

        btnShowSurvey.addActionListener(e -> {
            Survey active = SurveyManager.getInstance().getActiveSurvey();
            if (active != null) {
                new SurveyPreviewDialog(SwingUtilities.getWindowAncestor(this), active).setVisible(true);
            } else {
                GlobalStatusManager.getInstance().showError("No active survey to preview.");
            }
        });

        JPanel buttonWrapper = new JPanel(new BorderLayout());
        buttonWrapper.setOpaque(false);
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        buttonWrapper.add(btnShowSurvey, BorderLayout.CENTER);

        return buttonWrapper;
    }

    @Override
    protected JComponent buildContent() {
        String[] columns = {"Name", "Progress", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);

        table.getColumnModel().getColumn(1).setCellRenderer(new ProgressCellRenderer());
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        return scrollPane;
    }

    @Override
    protected JPanel buildControlBar() {
        JPanel southPanel = new JPanel(new GridLayout(1, 4));
        totalLabel = new JLabel("Total: 0", SwingConstants.CENTER);
        completedLabel = new JLabel("Completed: 0", SwingConstants.CENTER);
        pendingLabel = new JLabel("Pending: 0", SwingConstants.CENTER);
        timeLeftLabel = new JLabel("Time Left: 00:00", SwingConstants.CENTER);
        southPanel.add(totalLabel);
        southPanel.add(completedLabel);
        southPanel.add(pendingLabel);
        southPanel.add(timeLeftLabel);
        return southPanel;
    }

    @Override
    public void onSurveyScheduled(long executeTimeMillis) {
        this.scheduledExecuteTime = executeTimeMillis;
        SwingUtilities.invokeLater(() -> {
            stopCurrentTimer();
            GlobalStatusManager.getInstance().setSurveyState("Survey starting soon...", Color.ORANGE);
            GlobalStatusManager.getInstance().startGlobalCountdown(executeTimeMillis, "Starts in:");
        });
    }

    @Override
    public void onSurveyStarted() {
        SwingUtilities.invokeLater(() -> {
            stopCurrentTimer();
            GlobalStatusManager.getInstance().setSurveyState("Survey is LIVE!", Color.GREEN);
            GlobalStatusManager.getInstance().showSuccess("Survey is now LIVE on Telegram!");
            loadParticipantsToTable();

            long endTimeMillis = this.scheduledExecuteTime + TimeUnit.MINUTES.toMillis(5);
            GlobalStatusManager.getInstance().startGlobalCountdown(endTimeMillis, "Ends in:");

            uiTimer = new Timer(1000, e -> {
                long remaining = endTimeMillis - System.currentTimeMillis();
                if (remaining > 0) {
                    timeLeftLabel.setText("Time Left: " + formatTime(remaining));
                } else {
                    stopCurrentTimer();
                    timeLeftLabel.setText("Time Left: 00:00");
                }
            });
            uiTimer.start();
        });
    }

    @Override
    public void onParticipantUpdated() {
        SwingUtilities.invokeLater(this::loadParticipantsToTable);
    }

    @Override
    public void onSurveyEnded(Survey completedSurvey) {
        SwingUtilities.invokeLater(() -> {
            stopCurrentTimer();
            GlobalStatusManager.getInstance().stopGlobalCountdown();
            GlobalStatusManager.getInstance().setSurveyState("No Active Survey", null);
            timeLeftLabel.setText("Time Left: 00:00");
            tableModel.setRowCount(0);
            totalLabel.setText("Total: 0");
            completedLabel.setText("Completed: 0");
            pendingLabel.setText("Pending: 0");
            new SurveyResultsDialog(SwingUtilities.getWindowAncestor(this), completedSurvey).setVisible(true);
        });
    }

    private void loadParticipantsToTable() {
        Survey activeSurvey = SurveyManager.getInstance().getActiveSurvey();
        if (activeSurvey == null) {
            return;
        }

        tableModel.setRowCount(0);
        Collection<SurveyParticipant> participants = activeSurvey.getParticipants().values();

        int completed = 0;
        int totalQuestions = activeSurvey.getQuestions().size();

        for (SurveyParticipant p : participants) {
            String firstName = p.getCommunityUser().getFirstName() != null ? p.getCommunityUser().getFirstName() : "";
            String lastName = p.getCommunityUser().getLastName() != null ? p.getCommunityUser().getLastName() : "";
            String name = (firstName + " " + lastName).trim();
            if (name.isEmpty()) name = "Unknown";

            int answersCount = p.getAnswers() != null ? p.getAnswers().size() : 0;
            String progress = answersCount + "/" + totalQuestions;
            String status = p.isCompleted() ? "Completed" : "Pending";

            if (p.isCompleted()) completed++;
            tableModel.addRow(new Object[]{name, progress, status});
        }

        totalLabel.setText("Total: " + participants.size());
        completedLabel.setText("Completed: " + completed);
        pendingLabel.setText("Pending: " + (participants.size() - completed));
    }

    private void stopCurrentTimer() {
        if (uiTimer != null && uiTimer.isRunning()) {
            uiTimer.stop();
        }
    }

    private String formatTime(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d", minutes, seconds);
    }

    private static class ProgressCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JPanel panel = new JPanel(new BorderLayout(0, 4));
            panel.setOpaque(false);
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            if (value instanceof String stringValue && stringValue.contains("/")) {
                try {
                    String[] parts = stringValue.split("/");
                    int answered = Integer.parseInt(parts[0]);
                    int total = Integer.parseInt(parts[1]);

                    JLabel textLabel = new JLabel(stringValue, SwingConstants.CENTER);
                    JProgressBar pb = new JProgressBar(0, total);
                    pb.setValue(answered);
                    pb.setPreferredSize(new Dimension(100, 4));
                    pb.setBorder(BorderFactory.createEmptyBorder());

                    if (answered == total) {
                        pb.setForeground(new Color(76, 175, 80));
                    } else {
                        pb.setForeground(UIManager.getColor("Component.accentColor"));
                    }

                    panel.add(textLabel, BorderLayout.CENTER);
                    panel.add(pb, BorderLayout.SOUTH);

                    return panel;
                } catch (NumberFormatException ignored) {
                }
            }

            JLabel errorLabel = new JLabel(value != null ? value.toString() : "", SwingConstants.CENTER);
            panel.add(errorLabel, BorderLayout.CENTER);
            return panel;
        }
    }
}
