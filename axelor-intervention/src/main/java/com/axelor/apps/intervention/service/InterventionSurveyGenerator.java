package com.axelor.apps.intervention.service;

import com.axelor.apps.intervention.db.Intervention;
import java.util.concurrent.Callable;

public interface InterventionSurveyGenerator extends Callable<Integer> {
  @Override
  Integer call() throws Exception;

  void configure(Intervention intervention);
}
