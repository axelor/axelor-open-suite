package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class TimesheetPeriodComputationServiceImpl implements TimesheetPeriodComputationService {
  protected TimesheetRepository timesheetRepository;

  @Inject
  public TimesheetPeriodComputationServiceImpl(TimesheetRepository timesheetRepository) {
    this.timesheetRepository = timesheetRepository;
  }

  @Transactional
  @Override
  public void setComputedPeriodTotal(Timesheet timesheet) {
    timesheet.setPeriodTotal(computePeriodTotal(timesheet));
    timesheetRepository.save(timesheet);
  }

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
