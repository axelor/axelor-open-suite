/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service.timesheet.timer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import java.math.BigDecimal;

public interface TimesheetTimerService {

  String TS_TIMER_UPDATE_START = "start";
  String TS_TIMER_UPDATE_PAUSE = "pause";
  String TS_TIMER_UPDATE_STOP = "stop";
  String TS_TIMER_UPDATE_RESET = "reset";

  void start(TSTimer timer);

  public void pause(TSTimer timer);

  void stopAndGenerateTimesheetLine(TSTimer timer) throws AxelorException;

  void stop(TSTimer timer) throws AxelorException;

  void resetTimer(TSTimer timer);

  public void calculateDuration(TSTimer timer);

  public TimesheetLine generateTimesheetLine(TSTimer timer) throws AxelorException;

  TimesheetLine generateTimesheetLine(TSTimer timer, Timesheet timesheet);

  public TSTimer getCurrentTSTimer();

  public BigDecimal convertSecondDurationInHours(long durationInSeconds);

  void setUpdatedDuration(TSTimer timer, Long duration);
}
