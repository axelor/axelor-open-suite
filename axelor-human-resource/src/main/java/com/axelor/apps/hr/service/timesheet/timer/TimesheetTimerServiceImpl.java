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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TSTimerRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.date.DurationTool;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimesheetTimerServiceImpl implements TimesheetTimerService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Transactional
  public void pause(TSTimer timer) {
    timer.setStatusSelect(TSTimerRepository.STATUS_PAUSE);
    calculateDuration(timer);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void stop(TSTimer timer) throws AxelorException {
    timer.setStatusSelect(TSTimerRepository.STATUS_STOP);
    calculateDuration(timer);
    if (timer.getDuration() > 59) {
      generateTimesheetLine(timer);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.NO_TIMESHEET_CREATED),
          timer);
    }
  }

  @Transactional
  public void calculateDuration(TSTimer timer) {
    long currentDuration = timer.getDuration();
    Duration duration =
        DurationTool.computeDuration(
            timer.getTimerStartDateT(),
            Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime());
    long secondes = DurationTool.getSecondsDuration(duration) + currentDuration;
    timer.setDuration(secondes);
  }

  @Transactional(rollbackOn = {Exception.class})
  public TimesheetLine generateTimesheetLine(TSTimer timer) throws AxelorException {

    BigDecimal durationHours = this.convertSecondDurationInHours(timer.getDuration());
    Timesheet timesheet = Beans.get(TimesheetService.class).getCurrentOrCreateTimesheet();
    LocalDate startDateTime =
        (timer.getStartDateTime() == null)
            ? Beans.get(AppBaseService.class).getTodayDateTime().toLocalDate()
            : timer.getStartDateTime().toLocalDate();
    TimesheetLine timesheetLine =
        Beans.get(TimesheetLineService.class)
            .createTimesheetLine(
                timer.getProject(),
                timer.getProduct(),
                timer.getEmployee(),
                startDateTime,
                timesheet,
                durationHours,
                timer.getComments());

    Beans.get(TimesheetRepository.class).save(timesheet);
    Beans.get(TimesheetLineRepository.class).save(timesheetLine);
    timer.setTimesheetLine(timesheetLine);

    return timesheetLine;
  }

  public BigDecimal convertSecondDurationInHours(long durationInSeconds) {
    logger.debug("Duration in seconds : {}", durationInSeconds);

    BigDecimal durationHours =
        new BigDecimal(durationInSeconds).divide(new BigDecimal(3600), 4, RoundingMode.HALF_UP);
    logger.debug("Duration in hours : {}", durationHours);

    return durationHours;
  }

  public TSTimer getCurrentTSTimer() {
    return Beans.get(TSTimerRepository.class)
        .all()
        .filter("self.employee.user.id = ?1", AuthUtils.getUser().getId())
        .fetchOne();
  }
}
