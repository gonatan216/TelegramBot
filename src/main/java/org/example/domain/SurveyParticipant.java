package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyParticipant {
    private CommunityUser communityUser;

    @Builder.Default
    private Map<Question, String> answers = new HashMap<>();

    private boolean isCompleted;
}
