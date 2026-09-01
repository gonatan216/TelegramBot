package com.avishai.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Survey {
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @Builder.Default
    private Map<Long, SurveyParticipant> participants = new HashMap<>();

    private long startTime;
    private boolean isActive;
}
