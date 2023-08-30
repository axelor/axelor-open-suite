package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveRequestComputeDurationServiceImpl implements LeaveRequestComputeDurationService {

  protected WeeklyPlanningService weeklyPlanningService;
  protected PublicHolidayHrService publicHolidayHrService;

  @Inject
  public LeaveRequestComputeDurationServiceImpl(
      WeeklyPlanningService weeklyPlanningService, PublicHolidayHrService publicHolidayHrService) {
    this.weeklyPlanningService = weeklyPlanningService;
    this.publicHolidayHrService = publicHolidayHrService;
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
}
