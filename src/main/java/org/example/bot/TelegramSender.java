package org.example.bot;

import org.example.domain.Question;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramSender {
    void sendMessage(Long chatId, String text);
    Message sendPoll(Long chatId, Question question, Integer closeDateUnix);
    void stopPoll(Long chatId, Integer messageId);
}
