package com.axelor.apps.intervention.service.planning;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.google.inject.Inject;
import java.time.LocalDateTime;

public class PlanningDateTimeServiceImpl implements PlanningDateTimeService {
  protected final PlanningDateTimeProcessor planningDateTimeProcessor;

  @Inject
  public PlanningDateTimeServiceImpl(PlanningDateTimeProcessor planningDateTimeProcessor) {
    this.planningDateTimeProcessor = planningDateTimeProcessor;
  }

  @Override
  public LocalDateTime add(
      Company company, WeeklyPlanning planning, LocalDateTime dateTime, Long seconds) {
    if (planning == null || dateTime == null || seconds == null) {
      return null;
    }
    return planningDateTimeProcessor
        .with(planning, company)
        .from(dateTime)
        .processing(Operation.ADD, seconds)
        .compute();
  }

  @Override
  public LocalDateTime sub(
      Company company, WeeklyPlanning planning, LocalDateTime dateTime, Long seconds) {
    if (planning == null || dateTime == null || seconds == null) {
      return null;
    }
    return planningDateTimeProcessor
        .with(planning, company)
        .from(dateTime)
        .processing(Operation.SUB, seconds)
        .compute();
  }

  @Override
  public Long diff(
      Company company, WeeklyPlanning planning, LocalDateTime dateTime, LocalDateTime target) {
    return planningDateTimeProcessor.with(planning, company).from(dateTime).to(target).diff();
  }
}
