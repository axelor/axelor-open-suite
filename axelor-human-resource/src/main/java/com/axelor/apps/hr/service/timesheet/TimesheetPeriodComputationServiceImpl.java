package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import java.math.BigDecimal;
import java.util.List;

public class TimesheetPeriodComputationServiceImpl implements TimesheetPeriodComputationService {

  @Override
  public BigDecimal computePeriodTotal(Timesheet timesheet) {
    BigDecimal periodTotal = BigDecimal.ZERO;

    List<TimesheetLine> timesheetLines = timesheet.getTimesheetLineList();

    if (timesheetLines != null) {
      BigDecimal periodTotalTemp;
      for (TimesheetLine timesheetLine : timesheetLines) {
        if (timesheetLine != null) {
          periodTotalTemp = timesheetLine.getHoursDuration();
          if (periodTotalTemp != null) {
            periodTotal = periodTotal.add(periodTotalTemp);
          }
        }
      }
    }

    return periodTotal;
  }
}
