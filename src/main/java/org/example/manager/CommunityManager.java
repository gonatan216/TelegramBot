package com.avishai.manager;

import com.avishai.bot.TelegramSender;
import com.avishai.domain.CommunityUser;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommunityManager {
    private static volatile CommunityManager instance;
    private final Map<Long, CommunityUser> users;
    private final List<CommunityListener> listeners;
    @Setter
    private TelegramSender telegramSender;

    private CommunityManager() {
        this.users = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public int getCommunitySize() {
        return this.users.size();
    }

    public Collection<CommunityUser> getAllUsers() {
        return this.users.values();
    }

    public static CommunityManager getInstance() {
        if (instance == null) {
            synchronized (CommunityManager.class) {
                if (instance == null) {
                    instance = new CommunityManager();
                }
            }
        }
        return instance;
    }

    public void addListener(CommunityListener listener) {
        if (listener != null) listeners.add(listener);
    }

    public void tryAddUser(CommunityUser user) {
        if (user == null || user.getChatId() == null) return;

        CommunityUser existingUser = users.putIfAbsent(user.getChatId(), user);
        if (existingUser == null) {
            int currentSize = users.size();

            if (telegramSender != null) {
                String message = "New user " + user.getFirstName() + " joined the community! Total members: " + currentSize;
                for (CommunityUser iteratedUser : users.values()) {
                    if (!iteratedUser.getChatId().equals(user.getChatId())) {
                        telegramSender.sendMessage(iteratedUser.getChatId(), message);
                    }
                }
            }

            for (CommunityListener listener : listeners) {
                listener.onUserJoined(user, currentSize);
            }
        }

    }
}
