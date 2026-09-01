package org.example.ui.components;

import javax.swing.*;
import java.awt.*;

public abstract class BasePanel extends JPanel {
    private final JPanel headerContainer;
    private final JLabel titleLabel;

    public BasePanel(String headerTitle) {
        setLayout(new BorderLayout());
        setBorder(Theme.MAIN_PADDING);

        headerContainer = new JPanel(new BorderLayout());
        headerContainer.setOpaque(false);

        titleLabel = Theme.createHeader(headerTitle);
        headerContainer.add(titleLabel, BorderLayout.WEST);

        add(headerContainer, BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        JPanel controlBar = buildControlBar();
        if (controlBar != null) {
            controlBar.setBorder(Theme.getControlBarBorder());
            add(controlBar, BorderLayout.SOUTH);
        }
    }

    protected void addHeaderAction(JComponent component) {
        headerContainer.add(component, BorderLayout.EAST);
    }

    protected void updateHeader(String newText) {
        titleLabel.setText(newText);
    }

    protected abstract JComponent buildContent();

    protected abstract JPanel buildControlBar();
}
