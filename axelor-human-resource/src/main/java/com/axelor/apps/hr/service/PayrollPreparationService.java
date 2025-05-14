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
package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmployeeBonusMgtLine;
import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExtraHoursLine;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.LunchVoucherMgtLine;
import com.axelor.apps.hr.db.PayrollLeave;
import com.axelor.apps.hr.db.PayrollPreparation;
import com.axelor.apps.hr.db.repo.EmployeeBonusMgtLineRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.db.repo.ExtraHoursLineRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.db.repo.LunchVoucherMgtLineRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.leave.compute.LeaveRequestComputeLeaveDaysService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class PayrollPreparationService {

  protected LeaveRequestComputeLeaveDaysService leaveRequestComputeLeaveDaysService;
  protected LeaveRequestRepository leaveRequestRepo;
  protected WeeklyPlanningService weeklyPlanningService;

  @Inject
  public PayrollPreparationService(
      LeaveRequestComputeLeaveDaysService leaveRequestComputeLeaveDaysService,
      LeaveRequestRepository leaveRequestRepo,
      WeeklyPlanningService weeklyPlanningService) {

    this.leaveRequestComputeLeaveDaysService = leaveRequestComputeLeaveDaysService;
    this.leaveRequestRepo = leaveRequestRepo;
    this.weeklyPlanningService = weeklyPlanningService;
  }

  public PayrollPreparation generateFromEmploymentContract(
      PayrollPreparation payrollPreparation, EmploymentContract employmentContract) {
    if (payrollPreparation.getEmployee() == null) {
      payrollPreparation.setEmployee(employmentContract.getEmployee());
    }
    if (payrollPreparation.getCompany() == null) {
      payrollPreparation.setCompany(employmentContract.getPayCompany());
    }
    if (payrollPreparation.getEmploymentContract() == null) {
      payrollPreparation.setEmploymentContract(employmentContract);
    }

    payrollPreparation.setOtherCostsEmployeeSet(employmentContract.getOtherCostsEmployeeSet());
    payrollPreparation.setAnnualGrossSalary(employmentContract.getAnnualGrossSalary());
    return payrollPreparation;
  }

  public List<PayrollLeave> fillInPayrollPreparation(PayrollPreparation payrollPreparation)
      throws AxelorException {

    List<PayrollLeave> payrollLeaveList = fillInLeaves(payrollPreparation);

    payrollPreparation.setDuration(
        this.computeWorkingDaysNumber(payrollPreparation, payrollLeaveList));

    payrollPreparation.setExpenseAmount(this.computeExpenseAmount(payrollPreparation));
    payrollPreparation.setLunchVoucherNumber(this.computeLunchVoucherNumber(payrollPreparation));
    payrollPreparation.setEmployeeBonusAmount(computeEmployeeBonusAmount(payrollPreparation));
    payrollPreparation.setExtraHoursNumber(computeExtraHoursNumber(payrollPreparation));

    return payrollLeaveList;
  }

  public List<PayrollLeave> fillInLeaves(PayrollPreparation payrollPreparation)
      throws AxelorException {

    List<PayrollLeave> payrollLeaveList = new ArrayList<>();
    LocalDate fromDate = payrollPreparation.getPeriod().getFromDate();
    LocalDate toDate = payrollPreparation.getPeriod().getToDate();
    Employee employee = payrollPreparation.getEmployee();

    if (employee.getWeeklyPlanning() == null) {
      throw new AxelorException(
          payrollPreparation,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EMPLOYEE_PLANNING),
          employee.getName());
    }

    List<LeaveRequest> leaveRequestList =
        leaveRequestRepo
            .all()
            .filter(
                "self.statusSelect = ?4 AND self.employee = ?3 AND ((self.fromDateT BETWEEN ?2 AND ?1 OR self.toDateT BETWEEN ?2 AND ?1) OR (?1 BETWEEN self.fromDateT AND self.toDateT OR ?2 BETWEEN self.fromDateT AND self.toDateT))",
                toDate,
                fromDate,
                employee,
                LeaveRequestRepository.STATUS_VALIDATED)
            .fetch();

    for (LeaveRequest leaveRequest : leaveRequestList) {

      PayrollLeave payrollLeave = new PayrollLeave();

      if (leaveRequest.getFromDateT().toLocalDate().isBefore(fromDate)) {
        payrollLeave.setFromDate(fromDate);
      } else {
        payrollLeave.setFromDate(leaveRequest.getFromDateT().toLocalDate());
      }

      if (leaveRequest.getToDateT().toLocalDate().isAfter(toDate)) {
        payrollLeave.setToDate(toDate);
      } else {
        payrollLeave.setToDate(leaveRequest.getToDateT().toLocalDate());
      }

      payrollLeave.setDuration(
          leaveRequestComputeLeaveDaysService.computeLeaveDaysByLeaveRequest(
              fromDate, toDate, leaveRequest, employee));
      payrollLeave.setLeaveReason(leaveRequest.getLeaveReason());
      payrollLeave.setLeaveRequest(leaveRequest);
      payrollLeaveList.add(payrollLeave);
    }
    return payrollLeaveList;
  }

  public BigDecimal computeWorkingDaysNumber(
      PayrollPreparation payrollPreparation, List<PayrollLeave> payrollLeaveList) {
    LocalDate fromDate = payrollPreparation.getPeriod().getFromDate();
    LocalDate toDate = payrollPreparation.getPeriod().getToDate();
    LocalDate itDate = LocalDate.parse(fromDate.toString(), DateTimeFormatter.ISO_DATE);
    BigDecimal workingDays = BigDecimal.ZERO;
    BigDecimal leaveDays = BigDecimal.ZERO;
    while (!itDate.isAfter(toDate)) {
      workingDays =
          workingDays.add(
              BigDecimal.valueOf(
                  weeklyPlanningService.getWorkingDayValueInDays(
                      payrollPreparation.getEmployee().getWeeklyPlanning(), itDate)));
      itDate = itDate.plusDays(1);
    }
    if (payrollLeaveList != null) {
      for (PayrollLeave payrollLeave : payrollLeaveList) {
        workingDays = workingDays.subtract(payrollLeave.getDuration());
        leaveDays = leaveDays.add(payrollLeave.getDuration());
      }
    }
    payrollPreparation.setLeaveDuration(leaveDays);
    return workingDays;
  }

  public BigDecimal computeExtraHoursNumber(PayrollPreparation payrollPreparation) {
    LocalDate fromDate = payrollPreparation.getPeriod().getFromDate();
    LocalDate toDate = payrollPreparation.getPeriod().getToDate();
    BigDecimal extraHoursNumber = BigDecimal.ZERO;
    if (!CollectionUtils.isEmpty(payrollPreparation.getExtraHoursLineList())) {
      payrollPreparation.getExtraHoursLineList().clear();
    }
    for (ExtraHoursLine extraHoursLine :
        Beans.get(ExtraHoursLineRepository.class)
            .all()
            .filter(
                "self.employee = ?1 AND self.extraHours.statusSelect = 3 AND self.date BETWEEN ?2 AND ?3 AND (self.payrollPreparation = null OR self.payrollPreparation.id = ?4)",
                payrollPreparation.getEmployee(),
                fromDate,
                toDate,
                payrollPreparation.getId())
            .fetch()) {
      payrollPreparation.addExtraHoursLineListItem(extraHoursLine);
      extraHoursNumber = extraHoursNumber.add(extraHoursLine.getQty());
    }
    return extraHoursNumber;
  }

  public BigDecimal computeExpenseAmount(PayrollPreparation payrollPreparation) {
    BigDecimal expenseAmount = BigDecimal.ZERO;
    if (!CollectionUtils.isEmpty(payrollPreparation.getExpenseList())) {
      payrollPreparation.getExpenseList().clear();
    }
    List<Expense> expenseList =
        Beans.get(ExpenseRepository.class)
            .all()
            .filter(
                "self.employee = ?1 "
                    + "AND self.statusSelect = ?2 "
                    + "AND (self.payrollPreparation IS NULL OR self.payrollPreparation.id = ?3) "
                    + "AND self.companyCbSelect = ?4 "
                    + "AND self.validationDateTime BETWEEN ?5 AND ?6",
                payrollPreparation.getEmployee(),
                ExpenseRepository.STATUS_VALIDATED,
                payrollPreparation.getId(),
                ExpenseRepository.COMPANY_CB_PAYMENT_NO,
                payrollPreparation.getPeriod().getFromDate(),
                payrollPreparation.getPeriod().getToDate())
            .fetch();
    for (Expense expense : expenseList) {
      expenseAmount = expenseAmount.add(expense.getInTaxTotal());
      payrollPreparation.addExpenseListItem(expense);
    }
    return expenseAmount;
  }

  public BigDecimal computeLunchVoucherNumber(PayrollPreparation payrollPreparation) {
    BigDecimal lunchVoucherNumber = BigDecimal.ZERO;
    if (!CollectionUtils.isEmpty(payrollPreparation.getLunchVoucherMgtLineList())) {
      payrollPreparation.getLunchVoucherMgtLineList().clear();
    }
    List<LunchVoucherMgtLine> lunchVoucherList =
        Beans.get(LunchVoucherMgtLineRepository.class)
            .all()
            .filter(
                "self.employee = ?1 AND self.lunchVoucherMgt.statusSelect = 3 AND (self.payrollPreparation = null OR self.payrollPreparation.id = ?2) AND self.lunchVoucherMgt.payPeriod = ?3",
                payrollPreparation.getEmployee(),
                payrollPreparation.getId(),
                payrollPreparation.getPeriod())
            .fetch();
    for (LunchVoucherMgtLine lunchVoucherMgtLine : lunchVoucherList) {
      lunchVoucherNumber =
          lunchVoucherNumber.add(new BigDecimal(lunchVoucherMgtLine.getLunchVoucherNumber()));
      lunchVoucherNumber =
          lunchVoucherNumber.add(new BigDecimal(lunchVoucherMgtLine.getInAdvanceNbr()));
      payrollPreparation.addLunchVoucherMgtLineListItem(lunchVoucherMgtLine);
    }
    return lunchVoucherNumber;
  }

  public BigDecimal computeEmployeeBonusAmount(PayrollPreparation payrollPreparation) {
    BigDecimal employeeBonusAmount = BigDecimal.ZERO;
    if (!CollectionUtils.isEmpty(payrollPreparation.getEmployeeBonusMgtLineList())) {
      payrollPreparation.getEmployeeBonusMgtLineList().clear();
    }
    List<EmployeeBonusMgtLine> employeeBonusList =
        Beans.get(EmployeeBonusMgtLineRepository.class)
            .all()
            .filter(
                "self.employee = ?1"
                    + " AND self.statusSelect = ?4"
                    + " AND (self.payrollPreparation = null"
                    + " OR self.payrollPreparation.id = ?2)"
                    + " AND self.employeeBonusMgt.payPeriod = ?3",
                payrollPreparation.getEmployee(),
                payrollPreparation.getId(),
                payrollPreparation.getPeriod(),
                EmployeeBonusMgtLineRepository.STATUS_CALCULATED)
            .fetch();
    for (EmployeeBonusMgtLine employeeBonusMgtLine : employeeBonusList) {
      payrollPreparation.addEmployeeBonusMgtLineListItem(employeeBonusMgtLine);
      employeeBonusAmount = employeeBonusAmount.add(employeeBonusMgtLine.getAmount());
    }
    return employeeBonusAmount;
  }
}
