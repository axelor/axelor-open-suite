package com.axelor.apps.intervention.service;

import com.axelor.db.Query;
import com.axelor.studio.db.AppIntervention;

public class AppInterventionServiceImpl implements AppInterventionService {

  @Override
  public AppIntervention getAppIntervention() {
    return Query.of(AppIntervention.class).fetchOne();
  }
}
