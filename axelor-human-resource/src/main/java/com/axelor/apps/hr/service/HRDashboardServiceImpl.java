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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.ExtraHours;
import com.axelor.apps.hr.db.ExtraHoursLine;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.ExtraHoursLineRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaStore;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class HRDashboardServiceImpl implements HRDashboardService {

  protected static final String LEAVE_VALID = "valid";
  protected static final String LEAVE_BALANCE = "balance";
  protected static final String LEAVE_TAKEN = "taken";
  protected static final String NOT_AT_WORK = "notAtWork";
  protected static final String AT_WORK = "atWork";
  protected static final String LEAVE_STATUS_SELECT = "hrs.leave.request.status.select";
  protected static final String EXPENSE_STATUS_SELECT = "hrs.expenses.status.select";

  protected AppBaseService appBaseService;
  protected LeaveRequestRepository leaveRepo;
  protected ExpenseLineRepository expenseRepo;
  protected ExtraHoursLineRepository extraHoursRepo;
  protected TimesheetLineRepository timesheetLineRepo;
  protected PeriodService periodService;
  protected EmployeeComputeDaysLeaveBonusService leaveBonusService;
  protected PartnerService partnerService;

  @Inject
  public HRDashboardServiceImpl(
      AppBaseService appBaseService,
      LeaveRequestRepository leaveRepo,
      ExpenseLineRepository expenseRepo,
      ExtraHoursLineRepository extraHoursRepo,
      TimesheetLineRepository timesheetLineRepo,
      PeriodService periodService,
      EmployeeComputeDaysLeaveBonusService leaveBonusService,
      PartnerService partnerService) {
    this.appBaseService = appBaseService;
    this.leaveRepo = leaveRepo;
    this.expenseRepo = expenseRepo;
    this.extraHoursRepo = extraHoursRepo;
    this.timesheetLineRepo = timesheetLineRepo;
    this.periodService = periodService;
    this.leaveBonusService = leaveBonusService;
    this.partnerService = partnerService;
  }

  @Override
  public List<Map<String, Object>> getConnectedEmployeeLeaveData(Employee employee, Period period)
      throws AxelorException {
    List<LeaveRequest> leaves = getLeaveList(employee, period);
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

  protected List<LeaveRequest> getLeaveList(Employee employee, Period period) {
    StringBuilder filter = new StringBuilder("self.statusSelect != :statusSelect");
    Map<String, Object> params = new HashMap<>();

    if (employee != null) {
      filter.append(" AND self.employee = :employee");
      params.put("employee", employee);
    }
    if (period != null) {
      filter.append(
          " AND ((self.fromDateT >= :fromDateT AND self.toDateT <= :endDateT) OR (self.fromDateT <= :fromDateT AND self.toDateT >= :fromDateT AND self.toDateT <= :endDateT) OR (self.fromDateT >= :fromDateT AND self.fromDateT <= :endDateT AND self.toDateT >= :endDateT))");
      params.put("fromDateT", period.getFromDate().atStartOfDay());
      params.put("endDateT", period.getToDate().atTime(LocalTime.MAX));
    }
    params.put("statusSelect", LeaveRequestRepository.STATUS_CANCELED);
    return leaveRepo.all().filter(filter.toString()).bind(params).fetch();
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
  public List<Map<String, Object>> getExpenseData(Employee employee, Period period)
      throws AxelorException {
    List<ExpenseLine> expenseList = getExpenseList(employee, period);
    return getExpenseData(expenseList);
  }

  protected List<ExpenseLine> getExpenseList(Employee employee, Period period) {
    StringBuilder filter = new StringBuilder("self.expense.id IS NOT NULL");
    Map<String, Object> params = new HashMap<>();

    if (employee != null) {
      filter.append(" AND self.expense.employee = :employee");
      params.put("employee", employee);
    }
    if (period != null) {
      filter.append(" AND self.expenseDate >= :fromDate AND self.expenseDate <= :endDate");
      params.put("fromDate", period.getFromDate());
      params.put("endDate", period.getToDate());
    }
    return expenseRepo.all().filter(filter.toString()).bind(params).fetch();
  }

  protected List<Map<String, Object>> getExpenseData(List<ExpenseLine> expenseList) {
    List<Map<String, Object>> expenseData = new ArrayList<>();

    for (ExpenseLine expenseLine : expenseList) {
      Map<String, Object> map = new HashMap<>();
      map.put("expenseId", expenseLine.getExpense().getId());
      map.put("date", expenseLine.getExpenseDate());
      map.put("inTaxTotal", expenseLine.getTotalAmount());
      map.put("status", expenseLine.getExpense().getStatusSelect());
      map.put(
          "fullName",
          Optional.ofNullable(expenseLine.getExpense())
              .map(Expense::getEmployee)
              .map(Employee::getContactPartner)
              .map(partner -> partnerService.computeSimpleFullName(partner))
              .orElse(null));
      map.put(
          "statusSelect",
          I18n.get(
              MetaStore.getSelectionItem(
                      EXPENSE_STATUS_SELECT, expenseLine.getExpense().getStatusSelect().toString())
                  .getTitle()));
      map.put(
          "expenseType",
          Optional.ofNullable(expenseLine.getExpenseProduct()).map(Product::getName).orElse(null));
      expenseData.add(map);
    }
    return expenseData;
  }

  @Override
  public List<Map<String, Object>> getTimesheetData(Employee employee, Period period)
      throws AxelorException {
    Map<LocalDate, BigDecimal> timeSpentPerDayMap = getTimeSpentPerDay(employee, period);
    return getTimesheetData(employee, period, timeSpentPerDayMap);
  }

  protected List<Map<String, Object>> getTimesheetData(
      Employee employee, Period period, Map<LocalDate, BigDecimal> timeSpentPerDayMap)
      throws AxelorException {
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
  public List<Map<String, Object>> getExtraHrsData(Employee employee, Period period)
      throws AxelorException {
    List<ExtraHoursLine> extraHoursList = getExtraHoursList(employee, period);
    return getExtraHrsData(extraHoursList);
  }

  protected List<ExtraHoursLine> getExtraHoursList(Employee employee, Period period) {
    StringBuilder filter = new StringBuilder("self.extraHours.id IS NOT NULL");
    Map<String, Object> params = new HashMap<>();

    if (employee != null) {
      filter.append(" AND self.extraHours.employee = :employee");
      params.put("employee", employee);
    }
    if (period != null) {
      filter.append(" AND self.date >= :fromDate AND self.date <= :endDate");
      params.put("fromDate", period.getFromDate());
      params.put("endDate", period.getToDate());
    }
    return extraHoursRepo.all().filter(filter.toString()).bind(params).fetch();
  }

  protected List<Map<String, Object>> getExtraHrsData(List<ExtraHoursLine> extraHoursList) {
    List<Map<String, Object>> extraHrsData = new ArrayList<>();

    for (ExtraHoursLine extraHrsLine : extraHoursList) {
      Map<String, Object> map = new HashMap<>();
      map.put("extraHrId", extraHrsLine.getExtraHours().getId());
      map.put("date", extraHrsLine.getDate());
      map.put("duration", extraHrsLine.getQty());
      map.put("description", extraHrsLine.getDescription());
      map.put(
          "fullName",
          Optional.ofNullable(extraHrsLine.getExtraHours())
              .map(ExtraHours::getEmployee)
              .map(Employee::getContactPartner)
              .map(partner -> partnerService.computeSimpleFullName(partner))
              .orElse(null));
      extraHrsData.add(map);
    }
    return extraHrsData;
  }

  protected Map<LocalDate, BigDecimal> getTimeSpentPerDay(Employee employee, Period period) {
    StringBuilder filter =
        new StringBuilder(
            "self.timesheet.id IS NOT NULL AND self.timesheet.statusSelect = :statusSelect");
    Map<String, Object> params = new HashMap<>();

    if (employee != null) {
      filter.append(" AND self.timesheet.employee = :employee");
      params.put("employee", employee);
    }
    if (period != null) {
      filter.append(" AND self.date >= :fromDate AND self.date <= :endDate");
      params.put("fromDate", period.getFromDate());
      params.put("endDate", period.getToDate());
    }
    params.put("statusSelect", TimesheetRepository.STATUS_VALIDATED);

    List<TimesheetLine> timesheeLineList =
        timesheetLineRepo.all().filter(filter.toString()).bind(params).fetch();

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

  @Override
  public List<Map<String, Object>> getEmployeeLeaveData(Employee employee, Period period) {
    List<LeaveRequest> leaves = getLeaveList(employee, period);
    return getLeaveData(leaves);
  }

  protected List<Map<String, Object>> getLeaveData(List<LeaveRequest> leaves) {
    List<Map<String, Object>> leaveData = new ArrayList<>();

    for (LeaveRequest leaveRequest : leaves) {
      Map<String, Object> map = new HashMap<>();
      map.put("leaveRequestId", leaveRequest.getId());
      map.put(
          "fullName",
          Optional.ofNullable(leaveRequest.getEmployee())
              .map(Employee::getContactPartner)
              .map(partner -> partnerService.computeSimpleFullName(partner))
              .orElse(null));
      map.put(
          "fromDate",
          leaveRequest.getFromDateT() != null ? leaveRequest.getFromDateT().toLocalDate() : null);
      map.put(
          "toDate",
          leaveRequest.getToDateT() != null ? leaveRequest.getToDateT().toLocalDate() : null);
      map.put(
          "statusSelect",
          I18n.get(
              MetaStore.getSelectionItem(
                      LEAVE_STATUS_SELECT, leaveRequest.getStatusSelect().toString())
                  .getTitle()));
      map.put(
          "leaveReason",
          Optional.ofNullable(leaveRequest.getLeaveReason())
              .map(LeaveReason::getName)
              .orElse(null));
      leaveData.add(map);
    }
    return leaveData;
  }
}
