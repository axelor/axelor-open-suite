package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveManagement;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.LeaveManagementRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.leave.management.LeaveManagementService;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.axelor.auth.AuthUtils;
import com.axelor.studio.db.repo.AppLeaveRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class IncrementLeaveServiceImpl implements IncrementLeaveService {
  protected LeaveReasonRepository leaveReasonRepository;
  protected EmployeeRepository employeeRepository;
  protected AppBaseService appBaseService;
  protected AppHumanResourceService appHumanResourceService;
  protected LeaveManagementRepository leaveManagementRepository;
  protected LeaveManagementService leaveManagementService;
  protected PublicHolidayHrService publicHolidayHrService;
  protected LeaveLineService leaveLineService;
  protected LeaveValueProrataService leaveValueProrataService;

  @Inject
  public IncrementLeaveServiceImpl(
      LeaveReasonRepository leaveReasonRepository,
      EmployeeRepository employeeRepository,
      AppBaseService appBaseService,
      AppHumanResourceService appHumanResourceService,
      LeaveManagementRepository leaveManagementRepository,
      LeaveManagementService leaveManagementService,
      PublicHolidayHrService publicHolidayHrService,
      LeaveLineService leaveLineService,
      LeaveValueProrataService leaveValueProrataService) {
    this.leaveReasonRepository = leaveReasonRepository;
    this.employeeRepository = employeeRepository;
    this.appBaseService = appBaseService;
    this.appHumanResourceService = appHumanResourceService;
    this.leaveManagementRepository = leaveManagementRepository;
    this.leaveManagementService = leaveManagementService;
    this.publicHolidayHrService = publicHolidayHrService;
    this.leaveLineService = leaveLineService;
    this.leaveValueProrataService = leaveValueProrataService;
  }

  @Transactional
  @Override
  public void updateEmployeeLeaveLines(LeaveReason leaveReason, Employee employee)
      throws AxelorException {
    List<LeaveLine> leaveLineList = employee.getLeaveLineList();
    LocalDate todayDate = appBaseService.getTodayDate(null);
    int todayMonth = todayDate.getMonthValue();
    int firstLeaveDayPeriod = appHumanResourceService.getAppLeave().getFirstLeaveDayPeriod();
    LocalDate fromDate = LocalDate.of(todayDate.getYear(), todayMonth, firstLeaveDayPeriod);
    LocalDate toDate = computeToDate(todayDate, todayMonth, firstLeaveDayPeriod);
    BigDecimal value = getLeaveManagementValue(leaveReason, employee, fromDate, toDate);

    if (value.signum() == 0) {
      return;
    }

    updateEmployeeLeaveLine(leaveReason, employee, leaveLineList, fromDate, toDate, value);
  }

  protected void updateEmployeeLeaveLine(
      LeaveReason leaveReason,
      Employee employee,
      List<LeaveLine> leaveLineList,
      LocalDate fromDate,
      LocalDate toDate,
      BigDecimal value) {
    LeaveLine leaveLine =
        leaveLineList.stream()
            .filter(leaveLine1 -> leaveLine1.getLeaveReason().equals(leaveReason))
            .findFirst()
            .orElse(null);
    LeaveManagement leaveManagement = createLeaveManagement(fromDate, toDate, value);
    updateLeaveLines(leaveReason, employee, leaveLine, leaveManagement);
  }

  protected void updateLeaveLines(
      LeaveReason leaveReason,
      Employee employee,
      LeaveLine leaveLine,
      LeaveManagement leaveManagement) {
    if (leaveLine == null) {
      LeaveLine newLeaveLine = leaveLineService.createNewLeaveLine(leaveReason, leaveManagement);
      leaveManagementService.computeQuantityAvailable(newLeaveLine);
      employee.addLeaveLineListItem(newLeaveLine);
    } else {
      leaveLine.addLeaveManagementListItem(leaveManagement);
      leaveManagementService.computeQuantityAvailable(leaveLine);
    }
  }

  protected LeaveManagement createLeaveManagement(
      LocalDate fromDate, LocalDate toDate, BigDecimal value) {
    LeaveManagement leaveManagement =
        leaveManagementService.createLeaveManagement(
            AuthUtils.getUser(), "", appBaseService.getTodayDate(null), fromDate, toDate, value);

    return leaveManagementRepository.save(leaveManagement);
  }

  protected BigDecimal getLeaveManagementValue(
      LeaveReason leaveReason, Employee employee, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    BigDecimal value = leaveReason.getDefaultDayNumberGain();
    WeeklyPlanning weeklyPlanning = employee.getWeeklyPlanning();
    Integer useWeeklyPlanningCoefficientSelect =
        appHumanResourceService.getAppLeave().getUseWeeklyPlanningCoefficientSelect();
    BigDecimal imposedDayNumber =
        new BigDecimal(publicHolidayHrService.getImposedDayNumber(employee, fromDate, toDate));
    BigDecimal totalDaysWithPlanningCoef =
        computeTotalDays(value, weeklyPlanning, imposedDayNumber);
    BigDecimal totalDaysWithoutPlanningCoef = value.subtract(imposedDayNumber);

    switch (useWeeklyPlanningCoefficientSelect) {
      case AppLeaveRepository.USE_WEEKLY_PLANNING_COEFFICIENT_TYPE_ALWAYS:
        return leaveValueProrataService.getProratedValue(
            totalDaysWithPlanningCoef, leaveReason, employee, fromDate, toDate);
      case AppLeaveRepository.USE_WEEKLY_PLANNING_COEFFICIENT_TYPE_NEVER:
        return leaveValueProrataService.getProratedValue(
            totalDaysWithoutPlanningCoef, leaveReason, employee, fromDate, toDate);
      default:
      case AppLeaveRepository.USE_WEEKLY_PLANNING_COEFFICIENT_TYPE_CONFIGURABLE:
        if (leaveReason.getUseWeeklyPlanningCoef()) {
          value = totalDaysWithPlanningCoef;
        } else {
          value = totalDaysWithoutPlanningCoef;
        }
        break;
    }
    return leaveValueProrataService.getProratedValue(
        value, leaveReason, employee, fromDate, toDate);
  }

  protected BigDecimal computeTotalDays(
      BigDecimal value, WeeklyPlanning weeklyPlanning, BigDecimal imposedDayNumber) {
    BigDecimal computedValue;
    BigDecimal totalDays = BigDecimal.ZERO;

    if (weeklyPlanning != null) {
      computedValue = value.multiply(weeklyPlanning.getLeaveCoef());
      totalDays = computedValue.subtract(imposedDayNumber);
    }
    return totalDays;
  }

  protected LocalDate computeToDate(LocalDate todayDate, int todayMonth, int firstLeaveDayPeriod) {
    LocalDate toDate;
    if (firstLeaveDayPeriod == 1) {
      toDate = LocalDate.of(todayDate.getYear(), todayMonth, todayDate.lengthOfMonth());
    } else {
      toDate = LocalDate.of(todayDate.getYear(), todayMonth + 1, firstLeaveDayPeriod - 1);
    }
    return toDate;
  }
}
