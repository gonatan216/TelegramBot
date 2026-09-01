package org.example.ui.components;

import org.example.domain.Question;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuestionInputPanel extends JPanel {
    private final JTextField questionField;
    private final JTextField[] optionFields;
    private final JLabel titleLabel;
    private final JButton deleteButton;

    public QuestionInputPanel(int questionIndex, Runnable onDelete) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                )
        ));

        JPanel headerPanel = new JPanel(new BorderLayout());
        titleLabel = new JLabel("Question " + questionIndex);
        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> onDelete.run());

        Color origBg = deleteButton.getBackground();
        Color origFg = deleteButton.getForeground();

        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (deleteButton.isEnabled()) {
                    deleteButton.setBackground(Theme.DANGER_RED);
                    deleteButton.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                deleteButton.setBackground(origBg);
                deleteButton.setForeground(origFg);
            }
        });

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(deleteButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 5, 5, 5);

        questionField = new JTextField();
        questionField.putClientProperty("JTextField.placeholderText", "Enter your question here...");
        addValidationResetListener(questionField);
        gbc.gridy = 0;
        contentPanel.add(questionField, gbc);

        optionFields = new JTextField[4];
        String[] placeholders = {
                "Option 1 (Required)",
                "Option 2 (Required)",
                "Option 3 (Optional)",
                "Option 4 (Optional)"
        };

        for (int i = 0; i < optionFields.length; i++) {
            optionFields[i] = new JTextField();
            optionFields[i].putClientProperty("JTextField.placeholderText", placeholders[i]);
            addValidationResetListener(optionFields[i]);
            gbc.gridy = i + 1;
            contentPanel.add(optionFields[i], gbc);
        }

        add(contentPanel, BorderLayout.CENTER);
    }

    private void addValidationResetListener(JTextField field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                resetOutline();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                resetOutline();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                resetOutline();
            }

            private void resetOutline() {
                field.putClientProperty("JComponent.outline", null);
            }
        });
    }

    public void setDeleteEnabled(boolean enabled) {
        deleteButton.setEnabled(enabled);
    }

    public void setQuestionTitle(int questionNumber) {
        titleLabel.setText("Question " + questionNumber);
    }

    public boolean validateCard() {
        boolean isValid = true;
        JTextField firstInvalidField = null;

        if (questionField.getText().trim().isEmpty()) {
            questionField.putClientProperty("JComponent.outline", "error");
            firstInvalidField = questionField;
            isValid = false;
        }

        int filledOptions = 0;
        for (JTextField field : optionFields) {
            if (!field.getText().trim().isEmpty()) {
                filledOptions++;
            }
        }

        if (filledOptions < 2) {
            for (int i = 0; i < 2; i++) {
                if (optionFields[i].getText().trim().isEmpty()) {
                    optionFields[i].putClientProperty("JComponent.outline", "error");
                    if (firstInvalidField == null) {
                        firstInvalidField = optionFields[i];
                    }
                    isValid = false;
                }
            }
        }

        if (firstInvalidField != null) {
            firstInvalidField.requestFocus();
        }

        return isValid;
    }

    public Optional<Question> buildQuestion() {
        String questionText = questionField.getText().trim();
        if (questionText.isEmpty()) {
            return Optional.empty();
        }

        List<String> validOptions = new ArrayList<>();
        for (JTextField field : optionFields) {
            String text = field.getText().trim();
            if (!text.isEmpty()) {
                validOptions.add(text);
            }
        }

        if (validOptions.size() < 2) {
            return Optional.empty();
        }

        return Optional.of(Question.builder()
                .text(questionText)
                .options(validOptions)
                .build());
    }

    public void populateQuestion(Question question) {
        questionField.setText(question.getText());
        List<String> options = question.getOptions();
        if (options != null) {
            for (int i = 0; i < options.size() && i < optionFields.length; i++) {
                optionFields[i].setText(options.get(i));
            }
        }
    }
}
