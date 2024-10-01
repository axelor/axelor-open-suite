package com.axelor.apps.hr.service.timesheet;

import java.util.List;

public interface TimesheetLineRemoveService {
  void removeTimesheetLines(List<Integer> projectTimesheetLineIds);
}
