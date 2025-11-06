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
package com.axelor.apps.hr.service.lunch.voucher;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LunchVoucherAdvance;
import com.axelor.apps.hr.db.LunchVoucherMgt;
import com.axelor.apps.hr.db.LunchVoucherMgtLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.db.repo.LunchVoucherAdvanceRepository;
import com.axelor.apps.hr.db.repo.LunchVoucherMgtLineRepository;
import com.axelor.apps.hr.service.EmployeeComputeDaysLeaveLunchVoucherService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public class LunchVoucherMgtLineServiceImpl implements LunchVoucherMgtLineService {

  protected EmployeeComputeDaysLeaveLunchVoucherService employeeComputeDaysLeaveLunchVoucherService;
  protected ExpenseLineRepository expenseLineRepository;
  protected LunchVoucherMgtLineRepository lunchVoucherMgtLineRepository;

  @Inject
  public LunchVoucherMgtLineServiceImpl(
      EmployeeComputeDaysLeaveLunchVoucherService employeeComputeDaysLeaveLunchVoucherService,
      ExpenseLineRepository expenseLineRepository,
      LunchVoucherMgtLineRepository lunchVoucherMgtLineRepository) {
    this.employeeComputeDaysLeaveLunchVoucherService = employeeComputeDaysLeaveLunchVoucherService;
    this.expenseLineRepository = expenseLineRepository;
    this.lunchVoucherMgtLineRepository = lunchVoucherMgtLineRepository;
  }

  /*
   * Create a new line from employee and lunchVoucherMgt
   */
  @Override
  public LunchVoucherMgtLine create(Employee employee, LunchVoucherMgt lunchVoucherMgt)
      throws AxelorException {
    LunchVoucherMgtLine lunchVoucherMgtLine = new LunchVoucherMgtLine();
    lunchVoucherMgtLine.setEmployee(employee);
    computeAllAttrs(employee, lunchVoucherMgt, lunchVoucherMgtLine);
    return lunchVoucherMgtLine;
  }

  /*
   * Try to set the line attributes: if an exception occurs, the line status
   * is anomaly.
   */
  @Override
  public void computeAllAttrs(
      Employee employee, LunchVoucherMgt lunchVoucherMgt, LunchVoucherMgtLine lunchVoucherMgtLine) {
    Integer lineStatus = LunchVoucherMgtLineRepository.STATUS_CALCULATED;
    try {
      Period payPeriod = lunchVoucherMgt.getPayPeriod();
      lunchVoucherMgtLine.setRestaurant(computeRestaurant(employee, payPeriod));
      lunchVoucherMgtLine.setInvitation(computeInvitation(employee, payPeriod));
      lunchVoucherMgtLine.setInAdvanceNbr(computeEmployeeLunchVoucherAdvance(employee));
      lunchVoucherMgtLine.setDaysWorkedNbr(
          employeeComputeDaysLeaveLunchVoucherService
              .getDaysWorkedInPeriod(
                  employee,
                  lunchVoucherMgt.getLeavePeriod().getFromDate(),
                  lunchVoucherMgt.getLeavePeriod().getToDate())
              .setScale(0, RoundingMode.HALF_UP)
              .intValue());
      compute(lunchVoucherMgtLine);
      fillLunchVoucherFormat(employee, lunchVoucherMgt, lunchVoucherMgtLine);
    } catch (Exception e) {
      TraceBackService.trace(e);
      lineStatus = LunchVoucherMgtLineRepository.STATUS_ANOMALY;
    }
    lunchVoucherMgtLine.setStatusSelect(lineStatus);
  }

  protected Integer computeEmployeeLunchVoucherAdvance(Employee employee) {
    int number = 0;
    List<LunchVoucherAdvance> list =
        Beans.get(LunchVoucherAdvanceRepository.class)
            .all()
            .filter(
                "self.employee.id = ?1 AND self.nbrLunchVouchersUsed < self.nbrLunchVouchers",
                employee.getId())
            .fetch();

    for (LunchVoucherAdvance item : list) {
      number += item.getNbrLunchVouchers() - item.getNbrLunchVouchersUsed();
    }

    return number;
  }

  @Override
  public void fillLunchVoucherFormat(
      Employee employee, LunchVoucherMgt lunchVoucherMgt, LunchVoucherMgtLine lunchVoucherMgtLine)
      throws AxelorException {
    int employeeFormat = employee.getLunchVoucherFormatSelect();
    if (employeeFormat != 0) {
      lunchVoucherMgtLine.setLunchVoucherFormatSelect(employeeFormat);
    } else {
      Company company = lunchVoucherMgt.getCompany();
      HRConfig hrConfig = Beans.get(HRConfigService.class).getHRConfig(company);
      lunchVoucherMgtLine.setLunchVoucherFormatSelect(hrConfig.getLunchVoucherFormatSelect());
    }
  }

  @Override
  public void compute(LunchVoucherMgtLine lunchVoucherMgtLine) throws AxelorException {
    int lunchVoucherNumber =
        lunchVoucherMgtLine.getDaysWorkedNbr()
            - (lunchVoucherMgtLine.getCanteenEntries()
                + lunchVoucherMgtLine.getRestaurant()
                + lunchVoucherMgtLine.getDaysOverseas()
                + lunchVoucherMgtLine.getInAdvanceNbr()
                + lunchVoucherMgtLine.getInvitation());
    lunchVoucherMgtLine.setLunchVoucherNumber(Integer.max(lunchVoucherNumber, 0));
    computeFormatRepartition(lunchVoucherMgtLine, lunchVoucherNumber);
  }

  protected void computeFormatRepartition(
      LunchVoucherMgtLine lunchVoucherMgtLine, int lunchVoucherNumber) {
    Employee employee = lunchVoucherMgtLine.getEmployee();

    switch (employee.getLunchVoucherFormatSelect()) {
      default:
      case EmployeeRepository.LUNCH_VOUCHER_MGT_FORMAT_PAPER:
        lunchVoucherMgtLine.setPaperFormatNumber(lunchVoucherNumber);
        lunchVoucherMgtLine.setCardFormatNumber(0);
        break;
      case EmployeeRepository.LUNCH_VOUCHER_MGT_FORMAT_CARD:
        lunchVoucherMgtLine.setCardFormatNumber(lunchVoucherNumber);
        lunchVoucherMgtLine.setPaperFormatNumber(0);
        break;
      case EmployeeRepository.LUNCH_VOUCHER_MGT_FORMAT_BOTH:
        computeFormatRepartitionForBoth(lunchVoucherMgtLine, lunchVoucherNumber, employee);
        break;
    }
  }

  protected void computeFormatRepartitionForBoth(
      LunchVoucherMgtLine lunchVoucherMgtLine, Integer lunchVoucherNumber, Employee employee) {
    BigDecimal distribution = BigDecimal.valueOf(employee.getLunchVoucherDistribution());
    int paperNumber =
        BigDecimal.valueOf(lunchVoucherNumber)
            .multiply(distribution)
            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
            .intValue();
    int cardNumber = lunchVoucherNumber - paperNumber;

    lunchVoucherMgtLine.setPaperFormatNumber(paperNumber);
    lunchVoucherMgtLine.setCardFormatNumber(cardNumber);
  }

  @Override
  public int computeRestaurant(Employee employee, Period payPeriod) {
    return countExpenseLinesForEmployee(
        employee, payPeriod, "self.expense.employee = :employee", null);
  }

  @Override
  public int computeInvitation(Employee employee, Period payPeriod) {
    return countExpenseLinesForEmployee(
        employee, payPeriod, ":employee MEMBER OF self.invitedCollaboratorSet", null);
  }

  protected int countExpenseLinesForEmployee(
      Employee employee, Period payPeriod, String extraFilter, Map<String, Object> extraBindings) {
    String filter =
        "self.expenseProduct.deductLunchVoucher = true "
            + "AND (self.expense.statusSelect IN :statusSelects OR self.expense.ventilated IS TRUE) "
            + "AND self.expense.period.fromDate >= :fromDate "
            + "AND self.expense.period.toDate <= :toDate "
            + "AND "
            + extraFilter;
    List<Integer> statusSelects =
        List.of(ExpenseRepository.STATUS_VALIDATED, ExpenseRepository.STATUS_REIMBURSED);
    Query<ExpenseLine> query = expenseLineRepository.all().filter(filter);

    query.bind("employee", employee);
    query.bind("statusSelects", statusSelects);
    query.bind("fromDate", payPeriod.getFromDate());
    query.bind("toDate", payPeriod.getToDate());

    if (extraBindings != null) {
      extraBindings.forEach(query::bind);
    }

    return (int) query.count();
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void setStatusToCalculate(LunchVoucherMgtLine lunchVoucherMgtLine) {
    lunchVoucherMgtLine.setStatusSelect(LunchVoucherMgtLineRepository.STATUS_TO_CALCULATE);
    lunchVoucherMgtLineRepository.save(lunchVoucherMgtLine);
  }
}
