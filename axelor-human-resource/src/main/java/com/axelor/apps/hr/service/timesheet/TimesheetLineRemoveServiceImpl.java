package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.project.db.Project;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class TimesheetLineRemoveServiceImpl implements TimesheetLineRemoveService {

  protected TimesheetLineRepository timeSheetLineRepository;

  @Inject
  public TimesheetLineRemoveServiceImpl(TimesheetLineRepository timeSheetLineRepository) {
    this.timeSheetLineRepository = timeSheetLineRepository;
  }

  @Override
  public void removeTimesheetLines(List<Integer> projectTimesheetLineIds) {
    for (Integer id : projectTimesheetLineIds) {
      removeTimesheetLine(timeSheetLineRepository.find(Long.valueOf(id)));
    }
  }

  @Transactional
  protected void removeTimesheetLine(TimesheetLine timesheetLine) {
    if (timesheetLine == null) {
      return;
    }

    if (timesheetLine.getTimesheet() != null) {
      Timesheet timesheet = timesheetLine.getTimesheet();
      timesheetLine.setTimesheet(null);
      timesheet.removeTimesheetLineListItem(timesheetLine);
    }
    if (timesheetLine.getProject() != null) {
      Project project = timesheetLine.getProject();
      timesheetLine.setProject(null);
      project.removeTimesheetLineListItem(timesheetLine);
    }

    timeSheetLineRepository.remove(timesheetLine);
  }
}
