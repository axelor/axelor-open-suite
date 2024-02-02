package com.axelor.apps.hr.service.timesheet.timer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.hr.db.Timesheet;
import java.util.List;

public interface TimerTimesheetGenerationService {
  Timesheet addTimersToTimesheet(List<TSTimer> timerList, Timesheet timesheet)
      throws AxelorException;
}
