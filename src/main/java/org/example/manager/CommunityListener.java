package org.example.manager;

import org.example.domain.CommunityUser;

public interface CommunityListener {
    void onUserJoined(CommunityUser user, int totalCommunitySize);
}
