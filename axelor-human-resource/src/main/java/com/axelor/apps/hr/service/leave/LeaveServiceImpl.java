/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.ical.ICalendarService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveLineRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Message;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import wslite.json.JSONException;

public class LeaveServiceImpl implements LeaveService {

  protected LeaveLineRepository leaveLineRepo;
  protected WeeklyPlanningService weeklyPlanningService;
  protected PublicHolidayHrService publicHolidayHrService;
  protected LeaveRequestRepository leaveRequestRepo;
  protected AppBaseService appBaseService;
  protected HRConfigService hrConfigService;
  protected TemplateMessageService templateMessageService;
  protected ICalendarEventRepository icalEventRepo;
  protected ICalendarService icalendarService;

  @Inject
  public LeaveServiceImpl(
      LeaveLineRepository leaveLineRepo,
      WeeklyPlanningService weeklyPlanningService,
      PublicHolidayHrService publicHolidayHrService,
      LeaveRequestRepository leaveRequestRepo,
      AppBaseService appBaseService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      ICalendarEventRepository icalEventRepo,
      ICalendarService icalendarService) {

    this.leaveLineRepo = leaveLineRepo;
    this.weeklyPlanningService = weeklyPlanningService;
    this.publicHolidayHrService = publicHolidayHrService;
    this.leaveRequestRepo = leaveRequestRepo;
    this.appBaseService = appBaseService;
    this.hrConfigService = hrConfigService;
    this.templateMessageService = templateMessageService;
    this.icalEventRepo = icalEventRepo;
    this.icalendarService = icalendarService;
  }

  /**
   * Compute the duration of a given leave request but restricted inside a period.
   *
   * @param leave
   * @param fromDate the first date of the period
   * @param toDate the last date of the period
   * @return the computed duration in days
   * @throws AxelorException
   */
  @Override
  public BigDecimal computeDuration(LeaveRequest leave, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    LocalDateTime leaveFromDate = leave.getFromDateT();
    LocalDateTime leaveToDate = leave.getToDateT();

    int startOn = leave.getStartOnSelect();
    int endOn = leave.getEndOnSelect();

    LocalDateTime from = leaveFromDate;
    LocalDateTime to = leaveToDate;
    // if the leave starts before the beginning of the period,
    // we use the beginning date of the period.
    if (leaveFromDate.toLocalDate().isBefore(fromDate)) {
      from = fromDate.atStartOfDay();
      startOn = LeaveRequestRepository.SELECT_MORNING;
    }
    // if the leave ends before the end of the period,
    // we use the last date of the period.
    if (leaveToDate.toLocalDate().isAfter(toDate)) {
      to = toDate.atStartOfDay();
      endOn = LeaveRequestRepository.SELECT_AFTERNOON;
    }

    return computeDuration(leave, from, to, startOn, endOn);
  }

  /**
   * Compute the duration of a given leave request.
   *
   * @param leave
   * @return the computed duration in days
   * @throws AxelorException
   */
  @Override
  public BigDecimal computeDuration(LeaveRequest leave) throws AxelorException {
    return computeDuration(
        leave,
        leave.getFromDateT(),
        leave.getToDateT(),
        leave.getStartOnSelect(),
        leave.getEndOnSelect());
  }

  /**
   * Compute the duration of a given leave request. The duration can be in hours or in days,
   * depending of the leave reason of the leave.
   *
   * @param leave
   * @param from, the beginning of the leave request inside the period
   * @param to, the end of the leave request inside the period
   * @param startOn If the period starts in the morning or in the afternoon
   * @param endOn If the period ends in the morning or in the afternoon
   * @return the computed duration in days
   * @throws AxelorException
   */
  @Override
  public BigDecimal computeDuration(
      LeaveRequest leave, LocalDateTime from, LocalDateTime to, int startOn, int endOn)
      throws AxelorException {

    BigDecimal duration = BigDecimal.ZERO;

    if (from != null && to != null && leave.getLeaveReason() != null) {
      Employee employee = leave.getEmployee();

      switch (leave.getLeaveReason().getUnitSelect()) {
        case LeaveReasonRepository.UNIT_SELECT_DAYS:
          LocalDate fromDate = from.toLocalDate();
          LocalDate toDate = to.toLocalDate();
          duration = computeDurationInDays(leave, employee, fromDate, toDate, startOn, endOn);
          break;

        case LeaveReasonRepository.UNIT_SELECT_HOURS:
          duration = computeDurationInHours(leave, employee, from, to);
          break;

        default:
          throw new AxelorException(
              leave.getLeaveReason(),
              TraceBackRepository.CATEGORY_NO_VALUE,
              I18n.get(HumanResourceExceptionMessage.LEAVE_REASON_NO_UNIT),
              leave.getLeaveReason().getName());
      }
    }

    return duration.signum() != -1 ? duration : BigDecimal.ZERO;
  }

  /**
   * Computes the duration in days of a leave, according to the input planning.
   *
   * @param leave
   * @param employee
   * @param fromDate
   * @param toDate
   * @param startOn
   * @param endOn
   * @return
   * @throws AxelorException
   */
  protected BigDecimal computeDurationInDays(
      LeaveRequest leave,
      Employee employee,
      LocalDate fromDate,
      LocalDate toDate,
      int startOn,
      int endOn)
      throws AxelorException {

    BigDecimal duration = BigDecimal.ZERO;
    WeeklyPlanning weeklyPlanning = getWeeklyPlanning(leave, employee);
    EventsPlanning holidayPlanning = getPublicHolidayEventsPlanning(leave, employee);

    // If the leave request is only for 1 day
    if (fromDate.isEqual(toDate)) {
      if (startOn == endOn) {
        if (startOn == LeaveRequestRepository.SELECT_MORNING) {
          duration =
              duration.add(
                  BigDecimal.valueOf(
                      weeklyPlanningService.getWorkingDayValueInDaysWithSelect(
                          weeklyPlanning, fromDate, true, false)));
        } else {
          duration =
              duration.add(
                  BigDecimal.valueOf(
                      weeklyPlanningService.getWorkingDayValueInDaysWithSelect(
                          weeklyPlanning, fromDate, false, true)));
        }
      } else {
        duration =
            duration.add(
                BigDecimal.valueOf(
                    weeklyPlanningService.getWorkingDayValueInDaysWithSelect(
                        weeklyPlanning, fromDate, true, true)));
      }

      // Else if it's on several days
    } else {
      duration =
          duration.add(
              BigDecimal.valueOf(computeStartDateWithSelect(fromDate, startOn, weeklyPlanning)));

      LocalDate itDate = fromDate.plusDays(1);
      while (!itDate.isEqual(toDate) && !itDate.isAfter(toDate)) {
        duration =
            duration.add(
                BigDecimal.valueOf(
                    weeklyPlanningService.getWorkingDayValueInDays(weeklyPlanning, itDate)));
        itDate = itDate.plusDays(1);
      }

      duration =
          duration.add(BigDecimal.valueOf(computeEndDateWithSelect(toDate, endOn, weeklyPlanning)));
    }

    if (holidayPlanning != null) {
      duration =
          duration.subtract(
              publicHolidayHrService.computePublicHolidayDays(
                  fromDate, toDate, weeklyPlanning, holidayPlanning));
    }

    return duration;
  }

  /**
   * Computes the duration in hours of a leave, according to the weekly and the holiday plannings.
   *
   * @param leave
   * @param employee
   * @param fromDateT
   * @param toDateT
   * @return
   * @throws AxelorException
   */
  protected BigDecimal computeDurationInHours(
      LeaveRequest leave, Employee employee, LocalDateTime fromDateT, LocalDateTime toDateT)
      throws AxelorException {

    BigDecimal duration = BigDecimal.ZERO;
    WeeklyPlanning weeklyPlanning = getWeeklyPlanning(leave, employee);
    EventsPlanning holidayPlanning = getPublicHolidayEventsPlanning(leave, employee);
    LocalDate fromDate = fromDateT.toLocalDate();
    LocalDate toDate = toDateT.toLocalDate();

    if (toDate.equals(fromDate)
        && !publicHolidayHrService.checkPublicHolidayDay(fromDate, holidayPlanning)) {
      duration =
          duration.add(
              weeklyPlanningService.getWorkingDayValueInHours(
                  weeklyPlanning, fromDate, fromDateT.toLocalTime(), toDateT.toLocalTime()));

    } else {
      // First day of leave
      if (!publicHolidayHrService.checkPublicHolidayDay(fromDate, holidayPlanning)) {
        duration =
            duration.add(
                weeklyPlanningService.getWorkingDayValueInHours(
                    weeklyPlanning, fromDate, fromDateT.toLocalTime(), null));
      }
      fromDate = fromDate.plusDays(1);

      // Last day of leave
      if (!publicHolidayHrService.checkPublicHolidayDay(toDate, holidayPlanning)) {
        duration =
            duration.add(
                weeklyPlanningService.getWorkingDayValueInHours(
                    weeklyPlanning, toDate, null, toDateT.toLocalTime()));
      }

      // Daily leave duration of the other days between from and to date
      for (LocalDate date = fromDate; date.isBefore(toDate); date = date.plusDays(1)) {
        if (!publicHolidayHrService.checkPublicHolidayDay(date, holidayPlanning)) {
          duration =
              duration.add(
                  weeklyPlanningService.getWorkingDayValueInHours(
                      weeklyPlanning, date, null, null));
        }
      }
    }

    return duration;
  }

  protected WeeklyPlanning getWeeklyPlanning(LeaveRequest leave, Employee employee)
      throws AxelorException {
    WeeklyPlanning weeklyPlanning = employee.getWeeklyPlanning();
    if (weeklyPlanning == null) {
      Company comp = leave.getCompany();
      if (comp != null) {
        HRConfig conf = comp.getHrConfig();
        if (conf != null) {
          weeklyPlanning = conf.getWeeklyPlanning();
        }
      }
    }
    if (weeklyPlanning == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EMPLOYEE_PLANNING),
          employee.getName());
    }
    return weeklyPlanning;
  }

  protected EventsPlanning getPublicHolidayEventsPlanning(LeaveRequest leave, Employee employee) {
    EventsPlanning publicHolidayPlanning = employee.getPublicHolidayEventsPlanning();
    if (publicHolidayPlanning == null
        && leave.getCompany() != null
        && leave.getCompany().getHrConfig() != null) {
      publicHolidayPlanning = leave.getCompany().getHrConfig().getPublicHolidayEventsPlanning();
    }
    return publicHolidayPlanning;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void manageSentLeaves(LeaveRequest leave) throws AxelorException {
    Employee employee = leave.getEmployee();

    LeaveLine leaveLine = getLeaveLine(leave);

    if (leaveLine == null) {
      throw new AxelorException(
          leave,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.LEAVE_LINE),
          employee.getName(),
          leave.getLeaveReason().getName());
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void manageValidateLeaves(LeaveRequest leave) throws AxelorException {
    Employee employee = leave.getEmployee();

    LeaveLine leaveLine = getLeaveLine(leave);

    if (leaveLine == null) {
      throw new AxelorException(
          leave,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.LEAVE_LINE),
          employee.getName(),
          leave.getLeaveReason().getName());
    }

    if (leave.getInjectConsumeSelect() == LeaveRequestRepository.SELECT_CONSUME) {
      leaveLine.setQuantity(leaveLine.getQuantity().subtract(leave.getDuration()));
      if (leaveLine.getQuantity().signum() == -1 && !employee.getNegativeValueLeave()) {
        throw new AxelorException(
            leave,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(HumanResourceExceptionMessage.LEAVE_ALLOW_NEGATIVE_VALUE_EMPLOYEE),
            employee.getName());
      }
      if (leaveLine.getQuantity().signum() == -1
          && !leave.getLeaveReason().getAllowNegativeValue()) {
        throw new AxelorException(
            leave,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(HumanResourceExceptionMessage.LEAVE_ALLOW_NEGATIVE_VALUE_REASON),
            leave.getLeaveReason().getName());
      }
      leaveLine.setDaysValidated(leaveLine.getDaysValidated().add(leave.getDuration()));

    } else {
      leaveLine.setQuantity(leaveLine.getQuantity().add(leave.getDuration()));
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void manageRefuseLeaves(LeaveRequest leave) throws AxelorException {
    Employee employee = leave.getEmployee();

    LeaveLine leaveLine = getLeaveLine(leave);

    if (leaveLine == null) {
      throw new AxelorException(
          leave,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.LEAVE_LINE),
          employee.getName(),
          leave.getLeaveReason().getName());
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void manageCancelLeaves(LeaveRequest leave) throws AxelorException {
    Employee employee = leave.getEmployee();

    LeaveLine leaveLine = getLeaveLine(leave);

    if (leaveLine == null) {
      throw new AxelorException(
          leave,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.LEAVE_LINE),
          employee.getName(),
          leave.getLeaveReason().getName());
    }

    if (leave.getStatusSelect() == LeaveRequestRepository.STATUS_VALIDATED) {
      if (leave.getInjectConsumeSelect() == LeaveRequestRepository.SELECT_CONSUME) {
        leaveLine.setQuantity(leaveLine.getQuantity().add(leave.getDuration()));
      } else {
        leaveLine.setQuantity(leaveLine.getQuantity().subtract(leave.getDuration()));
      }
      leaveLine.setDaysValidated(leaveLine.getDaysValidated().subtract(leave.getDuration()));
    }
  }

  @Override
  public double computeStartDateWithSelect(
      LocalDate date, int select, WeeklyPlanning weeklyPlanning) {
    double value = 0;
    if (select == LeaveRequestRepository.SELECT_MORNING) {
      value = weeklyPlanningService.getWorkingDayValueInDays(weeklyPlanning, date);
    } else {
      DayPlanning dayPlanning = weeklyPlanningService.findDayPlanning(weeklyPlanning, date);
      if (dayPlanning != null
          && dayPlanning.getAfternoonFrom() != null
          && dayPlanning.getAfternoonTo() != null) {
        value = 0.5;
      }
    }
    return value;
  }

  @Override
  public double computeEndDateWithSelect(
      LocalDate date, int select, WeeklyPlanning weeklyPlanning) {
    double value = 0;
    if (select == LeaveRequestRepository.SELECT_AFTERNOON) {
      value = weeklyPlanningService.getWorkingDayValueInDays(weeklyPlanning, date);
    } else {
      DayPlanning dayPlanning = weeklyPlanningService.findDayPlanning(weeklyPlanning, date);
      if (dayPlanning != null
          && dayPlanning.getMorningFrom() != null
          && dayPlanning.getMorningTo() != null) {
        value = 0.5;
      }
    }
    return value;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public LeaveRequest createEvents(LeaveRequest leave) throws AxelorException {
    User user = leave.getEmployee().getUser();
    if (user == null) {
      return null;
    }

    Employee employee = leave.getEmployee();
    WeeklyPlanning weeklyPlanning = employee.getWeeklyPlanning();

    if (weeklyPlanning == null) {
      throw new AxelorException(
          leave,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EMPLOYEE_PLANNING),
          employee.getName());
    }

    LocalDateTime fromDateTime;
    LocalDateTime toDateTime;
    if (leave.getLeaveReason().getUnitSelect() == LeaveReasonRepository.UNIT_SELECT_DAYS) {
      fromDateTime = getDefaultStart(weeklyPlanning, leave);
      toDateTime = getDefaultEnd(weeklyPlanning, leave);
    } else {
      fromDateTime = leave.getFromDateT();
      toDateTime = leave.getToDateT();
    }

    ICalendarEvent event =
        icalendarService.createEvent(
            fromDateTime,
            toDateTime,
            user,
            leave.getComments(),
            4,
            leave.getLeaveReason().getName() + " " + leave.getEmployee().getName());
    icalEventRepo.save(event);
    leave.setIcalendarEvent(event);

    return leave;
  }

  protected LocalDateTime getDefaultStart(WeeklyPlanning weeklyPlanning, LeaveRequest leave) {
    int startTimeHour = 0;
    int startTimeMin = 0;

    DayPlanning startDay =
        weeklyPlanningService.findDayPlanning(weeklyPlanning, leave.getFromDateT().toLocalDate());

    if (leave.getStartOnSelect() == LeaveRequestRepository.SELECT_MORNING) {
      if (startDay != null && startDay.getMorningFrom() != null) {
        startTimeHour = startDay.getMorningFrom().getHour();
        startTimeMin = startDay.getMorningFrom().getMinute();
      } else {
        startTimeHour = 8;
        startTimeMin = 0;
      }
    } else {
      if (startDay != null && startDay.getAfternoonFrom() != null) {
        startTimeHour = startDay.getAfternoonFrom().getHour();
        startTimeMin = startDay.getAfternoonFrom().getMinute();
      } else {
        startTimeHour = 14;
        startTimeMin = 0;
      }
    }
    return LocalDateTime.of(
        leave.getFromDateT().toLocalDate(), LocalTime.of(startTimeHour, startTimeMin));
  }

  protected LocalDateTime getDefaultEnd(WeeklyPlanning weeklyPlanning, LeaveRequest leave) {
    int endTimeHour = 0;
    int endTimeMin = 0;

    DayPlanning endDay =
        weeklyPlanningService.findDayPlanning(weeklyPlanning, leave.getToDateT().toLocalDate());

    if (leave.getEndOnSelect() == LeaveRequestRepository.SELECT_MORNING) {
      if (endDay != null && endDay.getMorningTo() != null) {
        endTimeHour = endDay.getMorningTo().getHour();
        endTimeMin = endDay.getMorningTo().getMinute();
      } else {
        endTimeHour = 12;
        endTimeMin = 0;
      }
    } else {
      if (endDay != null && endDay.getAfternoonTo() != null) {
        endTimeHour = endDay.getAfternoonTo().getHour();
        endTimeMin = endDay.getAfternoonTo().getMinute();
      } else {
        endTimeHour = 18;
        endTimeMin = 0;
      }
    }

    return LocalDateTime.of(
        leave.getToDateT().toLocalDate(), LocalTime.of(endTimeHour, endTimeMin));
  }

  @Override
  public BigDecimal computeLeaveDaysByLeaveRequest(
      LocalDate fromDate, LocalDate toDate, LeaveRequest leaveRequest, Employee employee)
      throws AxelorException {
    BigDecimal leaveDays = BigDecimal.ZERO;
    WeeklyPlanning weeklyPlanning = employee.getWeeklyPlanning();
    LocalDate leaveFrom = leaveRequest.getFromDateT().toLocalDate();
    LocalDate leaveTo = leaveRequest.getToDateT().toLocalDate();

    LocalDate itDate = fromDate;
    if (fromDate.isBefore(leaveFrom) || fromDate.equals(leaveFrom)) {
      itDate = leaveFrom;
    }

    boolean morningHalf = false;
    boolean eveningHalf = false;
    BigDecimal daysToAdd = BigDecimal.ZERO;
    if (leaveTo.equals(leaveFrom)
        && leaveRequest.getStartOnSelect() == leaveRequest.getEndOnSelect()) {
      eveningHalf = leaveRequest.getStartOnSelect() == LeaveRequestRepository.SELECT_AFTERNOON;
      morningHalf = leaveRequest.getStartOnSelect() == LeaveRequestRepository.SELECT_MORNING;
    }

    while (!itDate.isEqual(leaveTo.plusDays(1)) && !itDate.isEqual(toDate.plusDays(1))) {

      if (itDate.equals(leaveFrom) && !morningHalf) {
        daysToAdd =
            BigDecimal.valueOf(
                computeStartDateWithSelect(
                    itDate, leaveRequest.getStartOnSelect(), weeklyPlanning));
      } else if (itDate.equals(leaveTo) && !eveningHalf) {
        daysToAdd =
            BigDecimal.valueOf(
                computeEndDateWithSelect(itDate, leaveRequest.getEndOnSelect(), weeklyPlanning));
      } else {
        daysToAdd =
            BigDecimal.valueOf(
                weeklyPlanningService.getWorkingDayValueInDays(weeklyPlanning, itDate));
      }

      if (!publicHolidayHrService.checkPublicHolidayDay(itDate, employee)) {
        leaveDays = leaveDays.add(daysToAdd);
      }
      itDate = itDate.plusDays(1);
    }

    return leaveDays;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancel(LeaveRequest leaveRequest) throws AxelorException {

    checkCompany(leaveRequest);
    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      manageCancelLeaves(leaveRequest);
    }

    if (leaveRequest.getIcalendarEvent() != null) {
      ICalendarEvent event = leaveRequest.getIcalendarEvent();
      leaveRequest.setIcalendarEvent(null);
      icalEventRepo.remove(icalEventRepo.find(event.getId()));
    }
    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_CANCELED);
    leaveRequestRepo.save(leaveRequest);

    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      updateDaysToValidate(getLeaveLine(leaveRequest));
    }
  }

  @Override
  public Message sendCancellationEmail(LeaveRequest leaveRequest)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(leaveRequest.getCompany());

    if (hrConfig.getLeaveMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          leaveRequest, hrConfigService.getCanceledLeaveTemplate(hrConfig));
    }

    return null;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void confirm(LeaveRequest leaveRequest) throws AxelorException {

    checkCompany(leaveRequest);
    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      manageSentLeaves(leaveRequest);
    }

    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_AWAITING_VALIDATION);
    leaveRequest.setRequestDate(appBaseService.getTodayDate(leaveRequest.getCompany()));

    leaveRequestRepo.save(leaveRequest);

    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      updateDaysToValidate(getLeaveLine(leaveRequest));
    }
  }

  @Override
  public Message sendConfirmationEmail(LeaveRequest leaveRequest)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(leaveRequest.getCompany());

    if (hrConfig.getLeaveMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          leaveRequest, hrConfigService.getSentLeaveTemplate(hrConfig));
    }

    return null;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void validate(LeaveRequest leaveRequest) throws AxelorException {

    checkCompany(leaveRequest);
    if (leaveRequest.getLeaveReason().getUnitSelect() == LeaveReasonRepository.UNIT_SELECT_DAYS) {
      isOverlapped(leaveRequest);
    }
    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      manageValidateLeaves(leaveRequest);
    }

    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_VALIDATED);
    leaveRequest.setValidatedBy(AuthUtils.getUser());
    leaveRequest.setValidationDateTime(
        appBaseService.getTodayDateTime(leaveRequest.getCompany()).toLocalDateTime());

    LeaveLine leaveLine = getLeaveLine(leaveRequest);
    if (leaveLine != null) {
      leaveRequest.setQuantityBeforeValidation(leaveLine.getQuantity());
    }
    leaveRequestRepo.save(leaveRequest);

    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      updateDaysToValidate(leaveLine);
    }
    createEvents(leaveRequest);
  }

  @Override
  public Message sendValidationEmail(LeaveRequest leaveRequest)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(leaveRequest.getCompany());

    if (hrConfig.getLeaveMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          leaveRequest, hrConfigService.getValidatedLeaveTemplate(hrConfig));
    }

    return null;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void refuse(LeaveRequest leaveRequest) throws AxelorException {

    checkCompany(leaveRequest);
    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      manageRefuseLeaves(leaveRequest);
    }

    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_REFUSED);
    leaveRequest.setRefusedBy(AuthUtils.getUser());
    leaveRequest.setRefusalDateTime(
        appBaseService.getTodayDateTime(leaveRequest.getCompany()).toLocalDateTime());

    leaveRequestRepo.save(leaveRequest);
    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      updateDaysToValidate(getLeaveLine(leaveRequest));
    }
  }

  @Override
  public Message sendRefusalEmail(LeaveRequest leaveRequest)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(leaveRequest.getCompany());

    if (hrConfig.getLeaveMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          leaveRequest, hrConfigService.getRefusedLeaveTemplate(hrConfig));
    }

    return null;
  }

  @Override
  public boolean willHaveEnoughDays(LeaveRequest leaveRequest) {

    LocalDateTime todayDate = appBaseService.getTodayDateTime().toLocalDateTime();
    LocalDateTime beginDate = leaveRequest.getFromDateT();

    int interval =
        (beginDate.getYear() - todayDate.getYear()) * 12
            + beginDate.getMonthValue()
            - todayDate.getMonthValue();
    LeaveLine leaveLine =
        leaveLineRepo
            .all()
            .filter("self.leaveReason = :leaveReason AND self.employee = :employee")
            .bind("leaveReason", leaveRequest.getLeaveReason())
            .bind("employee", leaveRequest.getEmployee())
            .fetchOne();
    if (leaveLine == null) {
      if (leaveRequest.getLeaveReason() != null
          && !leaveRequest.getLeaveReason().getManageAccumulation()) {
        return true;
      }

      return false;
    }

    BigDecimal num =
        leaveLine
            .getQuantity()
            .add(
                leaveRequest
                    .getEmployee()
                    .getWeeklyPlanning()
                    .getLeaveCoef()
                    .multiply(leaveRequest.getLeaveReason().getDefaultDayNumberGain())
                    .multiply(BigDecimal.valueOf(interval)));

    return leaveRequest.getDuration().compareTo(num) <= 0;
  }

  protected Optional<LeaveLine> getLeaveReasonToJustify(
      Employee employee, LeaveReason leaveReason) {
    if (employee.getLeaveLineList() != null) {
      return employee.getLeaveLineList().stream()
          .filter(leaveLine -> leaveReason.equals(leaveLine.getLeaveReason()))
          .findAny();
    }
    return Optional.empty();
  }

  protected LeaveLine createLeaveReasonToJustify(Employee employee, LeaveReason leaveReason) {
    LeaveLine leaveLineEmployee = new LeaveLine();
    leaveLineEmployee.setLeaveReason(leaveReason);
    leaveLineEmployee.setEmployee(employee);
    if (leaveReason != null) {
      leaveLineEmployee.setName(leaveReason.getName());
    }

    leaveLineRepo.save(leaveLineEmployee);
    return leaveLineEmployee;
  }

  @Override
  @Transactional
  public LeaveLine addLeaveReasonOrCreateIt(Employee employee, LeaveReason leaveReason) {
    return getLeaveReasonToJustify(employee, leaveReason)
        .orElseGet(() -> createLeaveReasonToJustify(employee, leaveReason));
  }

  @Override
  public boolean isLeaveDay(Employee employee, LocalDate date) {
    return ObjectUtils.notEmpty(getLeaves(employee, date));
  }

  public List<LeaveRequest> getLeaves(Employee employee, LocalDate date) {
    List<LeaveRequest> leavesList = new ArrayList<>();
    List<LeaveRequest> leaves =
        leaveRequestRepo
            .all()
            .filter(
                "self.employee = :employee AND self.statusSelect IN (:awaitingValidation,:validated)")
            .bind("employee", employee)
            .bind("awaitingValidation", LeaveRequestRepository.STATUS_AWAITING_VALIDATION)
            .bind("validated", LeaveRequestRepository.STATUS_VALIDATED)
            .fetch();

    if (ObjectUtils.notEmpty(leaves)) {
      for (LeaveRequest leave : leaves) {
        LocalDate from = leave.getFromDateT().toLocalDate();
        LocalDate to = leave.getToDateT().toLocalDate();
        if ((from.isBefore(date) && to.isAfter(date)) || from.isEqual(date) || to.isEqual(date)) {
          leavesList.add(leave);
        }
      }
    }
    return leavesList;
  }

  protected void isOverlapped(LeaveRequest leaveRequest) throws AxelorException {
    List<LeaveRequest> leaveRequestList =
        leaveRequestRepo
            .all()
            .filter(
                "self.employee = ?1 AND self.statusSelect = ?2",
                leaveRequest.getEmployee(),
                LeaveRequestRepository.STATUS_VALIDATED)
            .fetch();
    for (LeaveRequest leaveRequest2 : leaveRequestList) {
      if (isOverlapped(leaveRequest, leaveRequest2)) {
        throw new AxelorException(
            leaveRequest,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(HumanResourceExceptionMessage.LEAVE_REQUEST_DATES_OVERLAPPED));
      }
    }
  }

  protected boolean isOverlapped(LeaveRequest request1, LeaveRequest request2) {

    if (isDatesNonOverlapped(request1, request2)
        || isSelectsNonOverlapped(request1, request2)
        || isSelectsNonOverlapped(request2, request1)) {
      return false;
    }

    return true;
  }

  protected boolean isDatesNonOverlapped(LeaveRequest request1, LeaveRequest request2) {
    return request2.getToDateT().isBefore(request1.getFromDateT())
        || request1.getToDateT().isBefore(request2.getFromDateT())
        || request1.getToDateT().isBefore(request1.getFromDateT())
        || request2.getToDateT().isBefore(request2.getFromDateT());
  }

  protected boolean isSelectsNonOverlapped(LeaveRequest request1, LeaveRequest request2) {
    return request1.getEndOnSelect() == LeaveRequestRepository.SELECT_MORNING
        && request2.getStartOnSelect() == LeaveRequestRepository.SELECT_AFTERNOON
        && request1.getToDateT().isEqual(request2.getFromDateT());
  }

  @Override
  @Transactional
  public void updateDaysToValidate(LeaveLine leaveLine) {

    List<LeaveRequest> leaveRequests =
        leaveRequestRepo
            .all()
            .filter(
                "self.statusSelect = :statusSelect AND self.leaveReason = :leaveReason AND self.employee = :employee")
            .bind("statusSelect", LeaveRequestRepository.STATUS_AWAITING_VALIDATION)
            .bind("leaveReason", leaveLine.getLeaveReason())
            .bind("employee", leaveLine.getEmployee())
            .fetch();

    BigDecimal daysToValidate = BigDecimal.ZERO;

    for (LeaveRequest request : leaveRequests) {
      if (request.getInjectConsumeSelect() == LeaveRequestRepository.SELECT_CONSUME) {
        daysToValidate = daysToValidate.add(request.getDuration());
      } else {
        daysToValidate = daysToValidate.subtract(request.getDuration());
      }
    }

    leaveLine.setDaysToValidate(daysToValidate);
  }

  @Override
  public String getLeaveCalendarDomain(User user) {

    StringBuilder domain = new StringBuilder("self.statusSelect = 3");
    Employee employee = user.getEmployee();

    if (employee == null || !employee.getHrManager()) {
      domain.append(
          " AND (self.employee.managerUser.id = :userId OR self.employee.user.id = :userId)");
    }

    return domain.toString();
  }

  @Override
  public LeaveLine getLeaveLine(LeaveRequest leaveRequest) {

    if (leaveRequest.getEmployee() == null) {
      return null;
    }

    return leaveLineRepo
        .all()
        .filter(
            "self.employee = ?1 AND self.leaveReason = ?2",
            leaveRequest.getEmployee(),
            leaveRequest.getLeaveReason())
        .fetchOne();
  }

  protected void checkCompany(LeaveRequest leaveRequest) throws AxelorException {

    if (ObjectUtils.isEmpty(leaveRequest.getCompany())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(HumanResourceExceptionMessage.LEAVE_REQUEST_NO_COMPANY));
    }
  }
}
