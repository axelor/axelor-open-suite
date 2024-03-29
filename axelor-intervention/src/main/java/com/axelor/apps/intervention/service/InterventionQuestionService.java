package com.axelor.apps.intervention.service;

import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.db.InterventionQuestion;

public interface InterventionQuestionService {
  void deleteSurvey(Intervention intervention);

  void advancedMonitoringAnswer(InterventionQuestion interventionQuestion);
}
