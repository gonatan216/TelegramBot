package com.avishai.manager;

import com.avishai.domain.Survey;

public interface SurveyListener {
    void onSurveyScheduled(long executeTimeMillis);
    void onSurveyStarted();
    void onParticipantUpdated();
    void onSurveyEnded(Survey completedSurvey);
}
