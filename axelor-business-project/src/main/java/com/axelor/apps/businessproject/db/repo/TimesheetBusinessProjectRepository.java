package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetHRRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetLineComputeNameService;
import com.axelor.apps.hr.service.timesheet.TimesheetPeriodComputationService;
import com.google.inject.Inject;

public class TimesheetBusinessProjectRepository extends TimesheetHRRepository {

  @Inject
  public TimesheetBusinessProjectRepository(
      TimesheetLineComputeNameService timesheetLineComputeNameService,
      TimesheetPeriodComputationService timesheetPeriodComputationService) {
    super(timesheetLineComputeNameService, timesheetPeriodComputationService);
  }

  @Override
  public Timesheet save(Timesheet timesheet) {
    if (timesheet.getTimesheetLineList() != null) {
      for (TimesheetLine timesheetLine : timesheet.getTimesheetLineList()) {
        if (timesheetLine.getIsExtraHours()) {
          timesheetLine.setDurationForCustomer(timesheetLine.getAdjustedOvertimeDuration());
        }
      }
    }
    return super.save(timesheet);
  }
}
