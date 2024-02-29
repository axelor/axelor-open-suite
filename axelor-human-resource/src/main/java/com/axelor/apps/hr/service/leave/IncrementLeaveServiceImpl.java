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
package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.CompanyDateService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmploymentContract;
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
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppLeave;
import com.axelor.studio.db.repo.AppLeaveRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
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
  protected CompanyDateService companyDateService;

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
      LeaveValueProrataService leaveValueProrataService,
      CompanyDateService companyDateService) {
    this.leaveReasonRepository = leaveReasonRepository;
    this.employeeRepository = employeeRepository;
    this.appBaseService = appBaseService;
    this.appHumanResourceService = appHumanResourceService;
    this.leaveManagementRepository = leaveManagementRepository;
    this.leaveManagementService = leaveManagementService;
    this.publicHolidayHrService = publicHolidayHrService;
    this.leaveLineService = leaveLineService;
    this.leaveValueProrataService = leaveValueProrataService;
    this.companyDateService = companyDateService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void updateEmployeeLeaveLines(LeaveReason leaveReason, Employee employee)
      throws AxelorException {
    List<LeaveLine> leaveLineList = employee.getLeaveLineList();
    LocalDate todayDate = appBaseService.getTodayDate(null);
    AppLeave appLeave = appHumanResourceService.getAppLeave();
    int typeSelect = leaveReason.getLeaveReasonTypeSelect();
    int todayMonth = todayDate.getMonthValue();
    int todayYear = todayDate.getYear();
    int firstLeaveDayPeriod = appLeave.getFirstLeaveDayPeriod();
    int firstLeaveMonthPeriod = appLeave.getFirstLeaveMonthPeriod();
    LocalDate hireDate = employee.getHireDate();
    LocalDate fromDate = null;
    LocalDate toDate = null;
    BigDecimal value = null;

    if (skipIncrement(hireDate, todayDate, typeSelect, todayYear, todayMonth)) {
      return;
    }

    if (typeSelect == LeaveReasonRepository.TYPE_SELECT_EVERY_MONTH) {
      fromDate = LocalDate.of(todayDate.getYear(), todayMonth, firstLeaveDayPeriod);
      toDate =
          computeToDate(
              leaveReason, todayDate, todayMonth, firstLeaveDayPeriod, firstLeaveMonthPeriod);
      value = getLeaveManagementValue(leaveReason, employee, fromDate, toDate);
    }

    if (typeSelect == LeaveReasonRepository.TYPE_SELECT_EVERY_YEAR) {
      fromDate = LocalDate.of(todayDate.getYear(), firstLeaveMonthPeriod, firstLeaveDayPeriod);
      toDate =
          computeToDate(
              leaveReason,
              todayDate,
              firstLeaveMonthPeriod,
              firstLeaveDayPeriod,
              firstLeaveMonthPeriod);
      value = getLeaveManagementValue(leaveReason, employee, fromDate, toDate);
    }

    if (value.signum() == 0) {
      return;
    }

    updateEmployeeLeaveLine(leaveReason, employee, leaveLineList, fromDate, toDate, value);
  }

  protected boolean skipIncrement(
      LocalDate hireDate, LocalDate todayDate, int typeSelect, int todayYear, int todayMonth) {

    return (hireDate != null && hireDate.isAfter(todayDate))
        && ((typeSelect == LeaveReasonRepository.TYPE_SELECT_EVERY_YEAR
                && hireDate.getYear() > todayYear)
            || (typeSelect == LeaveReasonRepository.TYPE_SELECT_EVERY_MONTH
                && hireDate.getMonthValue() > todayMonth));
  }

  protected void updateEmployeeLeaveLine(
      LeaveReason leaveReason,
      Employee employee,
      List<LeaveLine> leaveLineList,
      LocalDate fromDate,
      LocalDate toDate,
      BigDecimal value)
      throws AxelorException {
    LeaveLine leaveLine =
        leaveLineList.stream()
            .filter(leaveLine1 -> leaveLine1.getLeaveReason().equals(leaveReason))
            .findFirst()
            .orElse(null);
    EmploymentContract employmentContract = employee.getMainEmploymentContract();
    Company company = employmentContract == null ? null : employmentContract.getPayCompany();
    LeaveManagement leaveManagement = createLeaveManagement(fromDate, toDate, value, company);
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
      LocalDate fromDate, LocalDate toDate, BigDecimal value, Company company)
      throws AxelorException {

    LocalDateTime localDateTime = appBaseService.getTodayDateTime().toLocalDateTime();
    DateTimeFormatter dateTimeFormatter = getDateTimeFormatter(company);

    String comment =
        I18n.get("Created automatically by increment leave batch on")
            + " "
            + dateTimeFormatter.format(localDateTime);

    LeaveManagement leaveManagement =
        leaveManagementService.createLeaveManagement(
            AuthUtils.getUser(),
            comment,
            appBaseService.getTodayDate(null),
            fromDate,
            toDate,
            value);

    return leaveManagementRepository.save(leaveManagement);
  }

  protected DateTimeFormatter getDateTimeFormatter(Company company) throws AxelorException {
    return company == null
        ? DateTimeFormatter.ISO_DATE_TIME
        : companyDateService.getDateTimeFormat(company);
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

  protected LocalDate computeToDate(
      LeaveReason leaveReason,
      LocalDate todayDate,
      int todayMonth,
      int firstLeaveDayPeriod,
      int firstLeaveMonthPeriod) {
    LocalDate toDate = null;
    int typeSelect = leaveReason.getLeaveReasonTypeSelect();
    int year = todayDate.getYear();
    switch (typeSelect) {
      case LeaveReasonRepository.TYPE_SELECT_EVERY_MONTH:
        if (firstLeaveDayPeriod == 1) {
          toDate = LocalDate.of(year, todayMonth, todayDate.lengthOfMonth());
        } else {
          toDate = LocalDate.of(year, todayMonth + 1, firstLeaveDayPeriod - 1);
        }
        break;
      case LeaveReasonRepository.TYPE_SELECT_EVERY_YEAR:
        toDate =
            LocalDate.of(
                year + 1,
                firstLeaveMonthPeriod - 1,
                Month.of(firstLeaveMonthPeriod - 1).length(Year.isLeap(year)));
        break;
      default:
        break;
    }
    return toDate;
  }
}
