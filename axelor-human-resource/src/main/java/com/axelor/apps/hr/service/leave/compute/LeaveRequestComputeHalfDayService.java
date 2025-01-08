package com.axelor.apps.hr.service.leave.compute;

import com.axelor.apps.base.db.WeeklyPlanning;
import java.time.LocalDate;

public interface LeaveRequestComputeHalfDayService {

  double computeStartDateWithSelect(LocalDate date, int select, WeeklyPlanning weeklyPlanning);

  double computeEndDateWithSelect(LocalDate date, int select, WeeklyPlanning weeklyPlanning);
}
