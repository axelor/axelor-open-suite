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
package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.ExtraHoursLine;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.ExtraHoursLineRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmployeeDashboardServiceImpl implements EmployeeDashboardService {
  protected static final String LEAVE_VALID = "valid";
  protected static final String LEAVE_BALANCE = "balance";
  protected static final String LEAVE_TAKEN = "taken";
  protected static final String NOT_AT_WORK = "notAtWork";
  protected static final String AT_WORK = "atWork";

  protected AppBaseService appBaseService;
  protected EmployeeService employeeService;
  protected LeaveRequestRepository leaveRepo;
  protected ExpenseLineRepository expenseRepo;
  protected ExtraHoursLineRepository extraHoursRepo;
  protected TimesheetLineRepository timesheetLineRepo;
  protected PeriodService periodService;
  protected EmployeeComputeDaysLeaveBonusService leaveBonusService;

  @Inject
  public EmployeeDashboardServiceImpl(
      AppBaseService appBaseService,
      EmployeeService employeeService,
      LeaveRequestRepository leaveRepo,
      ExpenseLineRepository expenseRepo,
      ExtraHoursLineRepository extraHoursRepo,
      TimesheetLineRepository timesheetLineRepo,
      PeriodService periodService,
      EmployeeComputeDaysLeaveBonusService leaveBonusService) {
    this.appBaseService = appBaseService;
    this.employeeService = employeeService;
    this.leaveRepo = leaveRepo;
    this.expenseRepo = expenseRepo;
    this.extraHoursRepo = extraHoursRepo;
    this.timesheetLineRepo = timesheetLineRepo;
    this.periodService = periodService;
    this.leaveBonusService = leaveBonusService;
  }

  @Override
  public List<Map<String, Object>> getLeaveData() throws AxelorException {
    Employee employee = employeeService.getConnectedEmployee();
    Period period = getCurrentPeriod();

    List<LeaveRequest> leaves =
        leaveRepo
            .all()
            .filter(
                "self.employee = :employee AND self.statusSelect != :statusSelect"
                    + " AND ((self.fromDateT >= :fromDateT AND self.toDateT <= :endDateT) OR (self.fromDateT <= :fromDateT AND self.toDateT >= :fromDateT AND self.toDateT <= :endDateT) OR (self.fromDateT >= :fromDateT AND self.fromDateT <= :endDateT AND self.toDateT >= :endDateT))")
            .bind("employee", employee)
            .bind("statusSelect", LeaveRequestRepository.STATUS_CANCELED)
            .bind("fromDateT", period.getFromDate().atStartOfDay())
            .bind("endDateT", period.getToDate().atTime(LocalTime.MAX))
            .fetch();

    Map<Integer, Long> leaveCounts =
        leaves.stream()
            .collect(Collectors.groupingBy(LeaveRequest::getStatusSelect, Collectors.counting()));

    Map<String, Object> data = new HashMap<>();

    Map<String, Object> statusCountData = Map.of("statusCount", getLeaveCountByStatus(leaveCounts));
    Map<String, Object> leaveCountData = Map.of("leaveCount", getLeaveCountSums(employee, period));

    data.putAll(statusCountData);
    data.putAll(leaveCountData);

    return List.of(data);
  }

  protected HashMap<Object, Object> getLeaveCountSums(Employee employee, Period period) {
    HashMap<Object, Object> data = new HashMap<>();
    BigDecimal valid = BigDecimal.ZERO;
    BigDecimal balance = BigDecimal.ZERO;
    BigDecimal taken = BigDecimal.ZERO;
    for (LeaveLine leaveLine : employee.getLeaveLineList()) {

      BigDecimal totalQuantity = leaveLine.getTotalQuantity();
      BigDecimal daysValidated = leaveLine.getDaysValidated();

      valid = valid.add(totalQuantity);
      balance = balance.add(totalQuantity.subtract(daysValidated));
      taken = taken.add(daysValidated);
    }
    data.put(LEAVE_VALID, valid.setScale(2, RoundingMode.HALF_UP));
    data.put(LEAVE_BALANCE, balance.setScale(2, RoundingMode.HALF_UP));
    data.put(LEAVE_TAKEN, taken.setScale(2, RoundingMode.HALF_UP));
    return data;
  }

  protected Map<String, Object> getLeaveCountByStatus(Map<Integer, Long> leaveCounts) {
    Map<String, Object> countData = new HashMap<>();

    List<Integer> statusList =
        List.of(
            LeaveRequestRepository.STATUS_DRAFT,
            LeaveRequestRepository.STATUS_AWAITING_VALIDATION,
            LeaveRequestRepository.STATUS_VALIDATED,
            LeaveRequestRepository.STATUS_REFUSED);

    for (Integer status : statusList) {
      countData.put(status.toString(), leaveCounts.getOrDefault(status, 0l));
    }
    return countData;
  }

  @Override
  public List<Map<String, Object>> getExpenseData() throws AxelorException {
    Employee employee = employeeService.getConnectedEmployee();
    Period period = getCurrentPeriod();

    List<ExpenseLine> expenseList =
        expenseRepo
            .all()
            .filter(
                "self.expense.employee = :employee AND self.expenseDate >= :fromDate AND self.expenseDate <= :endDate")
            .bind("employee", employee)
            .bind("fromDate", period.getFromDate())
            .bind("endDate", period.getToDate())
            .order("-expenseDate")
            .fetch();

    List<Map<String, Object>> expenseData = new ArrayList<>();

    for (ExpenseLine expenseLine : expenseList) {
      Map<String, Object> map = new HashMap<>();
      map.put("expenseId", expenseLine.getExpense().getId());
      map.put("date", expenseLine.getExpenseDate());
      map.put("inTaxTotal", expenseLine.getTotalAmount());
      map.put("fullName", expenseLine.getComments());
      map.put("status", expenseLine.getExpense().getStatusSelect());
      expenseData.add(map);
    }

    return expenseData;
  }

  @Override
  public List<Map<String, Object>> getTimesheetData() throws AxelorException {
    Employee employee = employeeService.getConnectedEmployee();
    Period period = getCurrentPeriod();

    Map<LocalDate, BigDecimal> timeSpentPerDayMap = getTimeSpentPerDay(employee, period);
    int notAtWork = 0;
    int atWork = 0;

    LocalDate fromDate = period.getFromDate();
    LocalDate toDate = period.getToDate();
    BigDecimal dailyWorkHours = employee.getDailyWorkHours();

    for (LocalDate date = fromDate; date.isBefore(toDate.plusDays(1)); date = date.plusDays(1)) {
      BigDecimal spentHour = timeSpentPerDayMap.getOrDefault(date, BigDecimal.ZERO);
      BigDecimal spentTimeInDay = spentHour.divide(dailyWorkHours, 2, RoundingMode.HALF_UP);

      BigDecimal expectedWorkingDuration =
          leaveBonusService.getDaysWorkedInPeriod(employee, date, date);

      if (BigDecimal.ZERO.compareTo(expectedWorkingDuration) == 0) {
        continue;
      }
      if (spentTimeInDay.compareTo(expectedWorkingDuration) >= 0) {
        atWork++;
      } else {
        notAtWork++;
      }
    }

    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put(AT_WORK, atWork);
    dataMap.put(NOT_AT_WORK, notAtWork);

    return List.of(dataMap);
  }

  @Override
  public List<Map<String, Object>> getExtraHrsData() throws AxelorException {
    Employee employee = employeeService.getConnectedEmployee();
    Period period = getCurrentPeriod();

    List<ExtraHoursLine> extraHoursList =
        extraHoursRepo
            .all()
            .filter(
                "self.extraHours.employee = :employee AND self.date >= :fromDate AND self.date <= :endDate")
            .bind("employee", employee)
            .bind("fromDate", period.getFromDate())
            .bind("endDate", period.getToDate())
            .fetch();
    List<Map<String, Object>> extraHrsData = new ArrayList<>();
    for (ExtraHoursLine extraHrsLine : extraHoursList) {
      Map<String, Object> map = new HashMap<>();
      map.put("extraHrId", extraHrsLine.getExtraHours().getId());
      map.put("date", extraHrsLine.getDate());
      map.put("duration", extraHrsLine.getQty());
      map.put("description", extraHrsLine.getDescription());
      extraHrsData.add(map);
    }
    return extraHrsData;
  }

  protected Map<LocalDate, BigDecimal> getTimeSpentPerDay(Employee employee, Period period) {
    List<TimesheetLine> timesheeLineList =
        timesheetLineRepo
            .all()
            .filter(
                "self.timesheet.employee = :employee AND self.timesheet.statusSelect = :statusSelect AND self.date >= :fromDate AND self.date <= :endDate")
            .bind("employee", employee)
            .bind("statusSelect", TimesheetRepository.STATUS_VALIDATED)
            .bind("fromDate", period.getFromDate())
            .bind("endDate", period.getToDate())
            .fetch();

    return timesheeLineList.stream()
        .collect(
            Collectors.groupingBy(
                TimesheetLine::getDate,
                Collectors.reducing(
                    BigDecimal.ZERO, TimesheetLine::getHoursDuration, BigDecimal::add)));
  }

  @Override
  public Period getCurrentPeriod() throws AxelorException {
    Company company = AuthUtils.getUser().getActiveCompany();
    return periodService.getActivePeriod(
        appBaseService.getTodayDate(company), company, YearRepository.TYPE_PAYROLL);
  }
}
