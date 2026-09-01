package org.example;

import org.example.bot.Bot;
import org.example.config.Config;
import org.example.manager.CommunityManager;
import org.example.manager.SurveyManager;
import org.example.ui.Window;
import org.example.ui.components.Theme;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.swing.*;

@Slf4j
public class Main {
    public static void main(String[] args) {
        Theme.init();
        SwingUtilities.invokeLater(Window::new);

        Bot bot = new Bot(Config.BOT_USERNAME, Config.BOT_TOKEN);
        CommunityManager.getInstance().setTelegramSender(bot);
        SurveyManager.getInstance().setTelegramSender(bot);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            log.info("Bot is alive and listening");
        } catch (TelegramApiException e) {
            log.error("Critical error starting bot: {}", e.getMessage());
        }
    }
}
