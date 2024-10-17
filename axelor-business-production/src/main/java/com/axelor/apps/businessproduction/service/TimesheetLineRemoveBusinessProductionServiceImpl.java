package com.axelor.apps.businessproduction.service;

import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetLineRemoveServiceImpl;
import com.axelor.apps.production.db.OperationOrder;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class TimesheetLineRemoveBusinessProductionServiceImpl
    extends TimesheetLineRemoveServiceImpl {

  @Inject
  public TimesheetLineRemoveBusinessProductionServiceImpl(
      TimesheetLineRepository timeSheetLineRepository) {
    super(timeSheetLineRepository);
  }

  @Override
  @Transactional
  protected void removeTimesheetLine(TimesheetLine timesheetLine) {
    if (timesheetLine == null) {
      return;
    }

    if (timesheetLine.getOperationOrder() != null) {
      OperationOrder operationOrder = timesheetLine.getOperationOrder();
      timesheetLine.setTimesheet(null);
      operationOrder.removeTimesheetLineListItem(timesheetLine);
    }

    super.removeTimesheetLine(timesheetLine);
  }
}
