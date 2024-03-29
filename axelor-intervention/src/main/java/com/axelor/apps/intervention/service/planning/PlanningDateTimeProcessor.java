package com.axelor.apps.intervention.service.planning;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.WeeklyPlanning;
import java.time.LocalDateTime;

public interface PlanningDateTimeProcessor {
  PlanningDateTimeProcessor from(LocalDateTime from);

  PlanningDateTimeProcessor to(LocalDateTime to);

  PlanningDateTimeProcessor with(WeeklyPlanning planning, Company company);

  PlanningDateTimeProcessor processing(Operation op, Long seconds);

  LocalDateTime compute();

  Long diff();
}
