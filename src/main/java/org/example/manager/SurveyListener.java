package org.example.manager;

import org.example.domain.Survey;

public interface SurveyListener {
    void onSurveyScheduled(long executeTimeMillis);
    void onSurveyStarted();
    void onParticipantUpdated();
    void onSurveyEnded(Survey completedSurvey);
}
