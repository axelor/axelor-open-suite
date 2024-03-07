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
import com.axelor.apps.hr.service.timesheet.TimesheetFetchService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineCreateService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.project.db.Project;
import com.axelor.auth.AuthUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.date.DurationHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimesheetTimerServiceImpl implements TimesheetTimerService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppBaseService appBaseService;
  protected TSTimerRepository tsTimerRepository;
  protected TimesheetFetchService timesheetFetchService;
  protected TimesheetLineService timesheetLineService;
  protected TimesheetRepository timesheetRepository;
  protected TimesheetLineRepository timesheetLineRepository;
  protected TimesheetLineCreateService timesheetLineCreateService;

  @Inject
  public TimesheetTimerServiceImpl(
      TimesheetFetchService timesheetFetchService,
      AppBaseService appBaseService,
      TimesheetLineService timesheetLineService,
      TimesheetRepository timesheetRepository,
      TimesheetLineRepository timesheetLineRepository,
      TSTimerRepository tsTimerRepository,
      TimesheetLineCreateService timesheetLineCreateService) {
    this.timesheetFetchService = timesheetFetchService;
    this.appBaseService = appBaseService;
    this.timesheetLineService = timesheetLineService;
    this.timesheetRepository = timesheetRepository;
    this.timesheetLineRepository = timesheetLineRepository;
    this.tsTimerRepository = tsTimerRepository;
    this.timesheetLineCreateService = timesheetLineCreateService;
  }

  @Transactional
  @Override
  public void start(TSTimer timer) {
    LocalDateTime todayDateTime = appBaseService.getTodayDateTime().toLocalDateTime();
    timer.setStatusSelect(TSTimerRepository.STATUS_START);
    timer.setTimerStartDateT(todayDateTime);
    if (timer.getStartDateTime() == null) {
      timer.setStartDateTime(todayDateTime);
    }
  }

  @Transactional
  public void pause(TSTimer timer) {
    timer.setStatusSelect(TSTimerRepository.STATUS_PAUSE);
    calculateDuration(timer);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void stopAndGenerateTimesheetLine(TSTimer timer) throws AxelorException {
    stop(timer);
    calculateDuration(timer);
    Long duration = getDuration(timer);
    if (duration > 59) {
      generateTimesheetLine(timer);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.NO_TIMESHEET_CREATED),
          timer);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void stop(TSTimer timer) throws AxelorException {
    timer.setStatusSelect(TSTimerRepository.STATUS_STOP);
  }

  @Transactional
  public void resetTimer(TSTimer timer) {
    timer.setStatusSelect(TSTimerRepository.STATUS_DRAFT);
    timer.setTimesheetLine(null);
    timer.setStartDateTime(null);
    timer.setDuration(0L);
    timer.setComments(null);
    timer.setUpdatedDuration(null);
    timer.setTimerStartDateT(null);
  }

  @Transactional
  public void calculateDuration(TSTimer timer) {
    long currentDuration = timer.getDuration();
    Duration duration =
        DurationHelper.computeDuration(
            timer.getTimerStartDateT(), appBaseService.getTodayDateTime().toLocalDateTime());
    long secondes = DurationHelper.getSecondsDuration(duration) + currentDuration;
    timer.setDuration(secondes);
  }

  @Transactional(rollbackOn = {Exception.class})
  public TimesheetLine generateTimesheetLine(TSTimer timer) throws AxelorException {
    return generateTimesheetLine(timer, timesheetFetchService.getCurrentOrCreateTimesheet());
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public TimesheetLine generateTimesheetLine(TSTimer timer, Timesheet timesheet) {
    Long duration = getDuration(timer);
    BigDecimal durationHours = this.convertSecondDurationInHours(duration);
    LocalDate startDateTime =
        (timer.getStartDateTime() == null)
            ? appBaseService.getTodayDateTime().toLocalDate()
            : timer.getStartDateTime().toLocalDate();
    TimesheetLine timesheetLine =
        timesheetLineCreateService.createTimesheetLine(
            timer.getProject(),
            timer.getProjectTask(),
            timer.getProduct(),
            timer.getEmployee(),
            startDateTime,
            timesheet,
            durationHours,
            timer.getComments(),
            timer);

    timesheetRepository.save(timesheet);
    timesheetLineRepository.save(timesheetLine);
    timer.setTimesheetLine(timesheetLine);
    timer.setName(computeName(timer));

    return timesheetLine;
  }

  protected String computeName(TSTimer timer) {
    StringBuilder name = new StringBuilder();
    Project project = timer.getProject();
    LocalDateTime startDateTime = timer.getStartDateTime();

    if (project != null) {
      String code = timer.getProject().getCode();
      if (StringUtils.notEmpty(code)) {
        name.append(timer.getProject().getCode());
        name.append(" - ");
      }
    }

    name.append(timer.getProduct().getName());
    name.append(" - ");
    name.append(startDateTime);
    return name.toString();
  }

  public BigDecimal convertSecondDurationInHours(long durationInSeconds) {
    logger.debug("Duration in seconds : {}", durationInSeconds);

    BigDecimal durationHours =
        new BigDecimal(durationInSeconds).divide(new BigDecimal(3600), 4, RoundingMode.HALF_UP);
    logger.debug("Duration in hours : {}", durationHours);

    return durationHours;
  }

  @Transactional
  @Override
  public void setUpdatedDuration(TSTimer timer, Long duration) {
    timer.setUpdatedDuration(duration);
    tsTimerRepository.save(timer);
  }

  public TSTimer getCurrentTSTimer() {
    return tsTimerRepository
        .all()
        .filter("self.employee.user.id = ?1", AuthUtils.getUser().getId())
        .order("-createdOn")
        .fetchOne();
  }

  protected Long getDuration(TSTimer timer) {
    Long updatedDuration = timer.getUpdatedDuration();
    return updatedDuration == null || updatedDuration == 0 ? timer.getDuration() : updatedDuration;
  }
}
