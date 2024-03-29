package com.axelor.apps.intervention.service.planning;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.WeeklyPlanning;
import java.time.LocalDateTime;

public interface PlanningDateTimeService {
  LocalDateTime add(Company company, WeeklyPlanning planning, LocalDateTime dateTime, Long seconds);

  LocalDateTime sub(Company company, WeeklyPlanning planning, LocalDateTime dateTime, Long seconds);

  Long diff(Company company, WeeklyPlanning planning, LocalDateTime dateTime, LocalDateTime target);
}
