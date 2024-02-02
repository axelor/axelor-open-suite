package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.hr.db.Timesheet;
import java.math.BigDecimal;

public interface TimesheetPeriodComputationService {

  void setComputedPeriodTotal(Timesheet timesheet);

  BigDecimal computePeriodTotal(Timesheet timesheet);
}
