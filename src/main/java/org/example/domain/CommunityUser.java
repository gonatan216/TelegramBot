package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityUser {
    private Long chatId;
    private long timeAdded;
    private String firstName;
    private String lastName;
    private String username;
}
