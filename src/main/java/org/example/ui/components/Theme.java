package org.example.ui.components;

import org.example.domain.Question;
import com.formdev.flatlaf.FlatDarculaLaf;
import lombok.experimental.UtilityClass;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;

@UtilityClass
public final class Theme {
    public Color PRIMARY_BLUE = new Color(71, 105, 150);
    public Color SUCCESS_GREEN = new Color(73, 147, 73);
    public Color WARNING_ORANGE = new Color(255, 152, 0);
    public Color DANGER_RED = new Color(199, 84, 80);
    public Border MAIN_PADDING = BorderFactory.createEmptyBorder(15, 4, 15, 4);

    public void init() {
        FlatDarculaLaf.setup();
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("TabbedPane.tabSeparatorsFullHeight", true);
        UIManager.put("TabbedPane.selectedBackground", new Color(70, 73, 75));
    }

    public JPanel createQuestionCard(Question question) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 10, 10)
        ));

        JTextArea questionText = new JTextArea(question.getText());
        questionText.setEditable(false);
        questionText.setLineWrap(true);
        questionText.setWrapStyleWord(true);
        questionText.setOpaque(false);
        questionText.setFont(questionText.getFont().deriveFont(Font.BOLD, 16f));

        card.add(questionText);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        return card;
    }

    public JScrollPane createVerticalScrollPane(List<JPanel> cards) {
        JPanel container = new JPanel(new GridBagLayout());
        for (int i = 0; i < cards.size(); i++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = i;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);
            container.add(cards.get(i), gbc);
        }

        GridBagConstraints fillerGbc = new GridBagConstraints();
        fillerGbc.gridy = cards.size();
        fillerGbc.weighty = 1.0;
        container.add(new JPanel(), fillerGbc);
        JScrollPane scrollPane = new JScrollPane(
                container,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    public Border getControlBarBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0,
                        UIManager.getColor("Component.borderColor")
                ),
                BorderFactory.createEmptyBorder(10, 0, 0, 0)
        );
    }

    public JLabel createHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return label;
    }
}
