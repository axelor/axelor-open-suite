/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalTime;

public class TimesheetProjectServiceImpl implements TimesheetProjectService {
  protected TimesheetLineService timesheetLineService;

  @Inject
  public TimesheetProjectServiceImpl(TimesheetLineService timesheetLineService) {
    this.timesheetLineService = timesheetLineService;
  }

  @Override
  public BigDecimal computeDurationForCustomer(TimesheetLine timesheetLine) throws AxelorException {
    return timesheetLineService.computeHoursDuration(
        timesheetLine.getTimesheet(), timesheetLine.getDurationForCustomer(), true);
  }

  @Override
  public BigDecimal computeDuration(TimesheetLine timesheetLine) {
    LocalTime startTime = timesheetLine.getStartTime();
    LocalTime endTime = timesheetLine.getEndTime();
    BigDecimal duration = BigDecimal.ZERO;
    if (startTime != null && endTime != null) {
      duration = this.computeDuration(startTime, endTime);
    }
    return duration;
  }

  public BigDecimal computeDuration(LocalTime startTime, LocalTime endTime) {
    long minutes = Duration.between(startTime, endTime).toMinutes();
    if (minutes < 0) {
      // after midnight -> add 24h in minutes to prevent negative duration
      minutes += 24 * 60;
    }
    return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
  }
}
