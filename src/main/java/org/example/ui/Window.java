package org.example.ui;

import org.example.manager.GlobalStatusManager;
import org.example.ui.panels.ActiveSurveyPanel;
import org.example.ui.panels.CommunityPanel;
import org.example.ui.panels.CreateSurveyPanel;
import org.example.ui.panels.HistoryPanel;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {
    public Window() {
        setTitle("Telegram Survey Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout());

        CommunityPanel communityPanel = new CommunityPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Create Survey", new CreateSurveyPanel());
        tabbedPane.addTab("Active Survey", new ActiveSurveyPanel());
        tabbedPane.addTab("Survey History", new HistoryPanel());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, communityPanel, tabbedPane);
        splitPane.setDividerLocation(350);
        add(splitPane, BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new BorderLayout(10, 0));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0,
                        UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(6, 15, 6, 15)
        ));

        JLabel globalStatusLabel = new JLabel("Ready");
        globalStatusLabel.setFont(globalStatusLabel.getFont().deriveFont(Font.BOLD));
        statusBar.add(globalStatusLabel, BorderLayout.WEST);
        GlobalStatusManager.getInstance().registerLabel(globalStatusLabel);

        JLabel surveyStateLabel = new JLabel("No Active Survey", SwingConstants.CENTER);
        surveyStateLabel.setFont(surveyStateLabel.getFont().deriveFont(Font.BOLD));
        statusBar.add(surveyStateLabel, BorderLayout.CENTER);
        GlobalStatusManager.getInstance().registerSurveyStateLabel(surveyStateLabel);

        JLabel timerLabel = new JLabel("", SwingConstants.RIGHT);
        timerLabel.setFont(timerLabel.getFont().deriveFont(Font.BOLD));
        statusBar.add(timerLabel, BorderLayout.EAST);
        GlobalStatusManager.getInstance().registerTimerLabel(timerLabel);

        add(statusBar, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
