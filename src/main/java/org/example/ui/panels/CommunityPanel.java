package org.example.ui.panels;

import org.example.domain.CommunityUser;
import org.example.manager.CommunityListener;
import org.example.manager.CommunityManager;
import org.example.ui.components.BasePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CommunityPanel extends BasePanel implements CommunityListener {
    private DefaultTableModel tableModel;
    private final DateTimeFormatter timeFormatter;

    public CommunityPanel() {
        super("Total Community Members: 0");
        timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());
        CommunityManager.getInstance().addListener(this);
    }

    @Override
    protected JComponent buildContent() {
        String[] columnNames = {"Name", "Username", "Join Time"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        return scrollPane;
    }

    @Override
    protected JPanel buildControlBar() {
        return null;
    }

    @Override
    public void onUserJoined(CommunityUser user, int totalCommunitySize) {
        SwingUtilities.invokeLater(() -> {
            updateHeader("Total Community Members: " + totalCommunitySize);

            String firstName = user.getFirstName() != null ? user.getFirstName() : "";
            String lastName = user.getLastName() != null ? user.getLastName() : "";
            String fullName = (firstName + " " + lastName).trim();

            if (fullName.isEmpty()) {
                fullName = "Unknown User";
            }

            String username = user.getUsername();
            if (username != null && !username.trim().isEmpty()) {
                if (!username.startsWith("@")) {
                    username = "@" + username;
                }
            } else {
                username = "-";
            }

            String joinTime = timeFormatter.format(Instant.ofEpochMilli(user.getTimeAdded()));

            tableModel.addRow(new Object[]{fullName, username, joinTime});
        });
    }
}
