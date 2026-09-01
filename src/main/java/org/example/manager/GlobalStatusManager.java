package org.example.manager;

import org.example.ui.components.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;

public class GlobalStatusManager {
    private static volatile GlobalStatusManager instance;
    private final int TEMP_MSG_DURATION_MS = 3_000;
    private JLabel statusLabel;
    private JLabel surveyStateLabel;
    private String stableText = "Ready";
    private Color stableColor = null;
    private Timer resetTimer;
    private JLabel timerLabel;
    private Timer countdownTimer;

    private GlobalStatusManager() {}

    public static GlobalStatusManager getInstance() {
        if (instance == null) {
            synchronized (GlobalStatusManager.class) {
                if (instance == null) {
                    instance = new GlobalStatusManager();
                }
            }
        }
        return instance;
    }

    public void registerLabel(JLabel label) {
        this.statusLabel = label;
        applyStatus(this.stableText, this.stableColor);
    }

    public void registerTimerLabel(JLabel timerLabel) {
        this.timerLabel = timerLabel;
    }

    public void registerSurveyStateLabel(JLabel label) {
        this.surveyStateLabel = label;
    }

    public void setSurveyState(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            if (surveyStateLabel != null) {
                surveyStateLabel.setText(text);
                surveyStateLabel.setForeground(color);
            }
        });
    }

    public void setStableStatus(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            this.stableText = text;
            this.stableColor = color;
            stopResetTimer();
            applyStatus(text, color);
        });
    }

    public void setReady() {
        setStableStatus("Ready", null);
    }

    public void showInfo(String text, Color color, int durationMillis) {
        SwingUtilities.invokeLater(() -> {
            stopResetTimer();
            applyStatus(text, color);
            this.resetTimer = new Timer(
                    durationMillis,
                    e -> applyStatus(this.stableText, this.stableColor)
            );
            this.resetTimer.setRepeats(false);
            this.resetTimer.start();
        });
    }

    public void showError(String text) {
        showInfo(text, Color.RED, TEMP_MSG_DURATION_MS);
    }

    public void showSuccess(String text) {
        showInfo(text, Theme.SUCCESS_GREEN, TEMP_MSG_DURATION_MS);
    }

    public void startGlobalCountdown(long targetTimeMillis, String prefixText) {
        stopGlobalCountdownTimer();
        if (this.timerLabel != null) {
            this.timerLabel.setVisible(true);
        }
        this.countdownTimer = new Timer(
                1_000,
                e -> processCountdownTick(targetTimeMillis, prefixText)
        );
        this.countdownTimer.start();
    }

    public void stopGlobalCountdown() {
        stopGlobalCountdownTimer();
        if (this.timerLabel != null) {
            this.timerLabel.setText("");
            this.timerLabel.setForeground(null);
        }
    }

    private void processCountdownTick(long targetTimeMillis, String prefixText) {
        long remaining = targetTimeMillis - System.currentTimeMillis();
        if (remaining <= 0) {
            stopGlobalCountdownTimer();
            if (this.timerLabel != null) {
                this.timerLabel.setText("00:00");
                this.timerLabel.setForeground(Theme.DANGER_RED);
            }
            return;
        }
        if (this.timerLabel != null) {
            this.timerLabel.setForeground(resolveCountdownColor(remaining));
            this.timerLabel.setText(prefixText + " " + formatTime(remaining));
        }
    }

    private void stopGlobalCountdownTimer() {
        if (this.countdownTimer != null && this.countdownTimer.isRunning()) {
            this.countdownTimer.stop();
        }
    }

    private void stopResetTimer() {
        if (this.resetTimer != null && this.resetTimer.isRunning()) {
            this.resetTimer.stop();
        }
    }

    private Color resolveCountdownColor(long remainingMillis) {
        if (remainingMillis > 60000) {
            return Theme.SUCCESS_GREEN;
        } else if (remainingMillis > 15000) {
            return Theme.WARNING_ORANGE;
        }
        return Theme.DANGER_RED;
    }

    private String formatTime(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void applyStatus(String text, Color color) {
        if (this.statusLabel != null) {
            this.statusLabel.setForeground(color);
            this.statusLabel.setText(text);
            this.statusLabel.setToolTipText(text);
        }
    }
}
