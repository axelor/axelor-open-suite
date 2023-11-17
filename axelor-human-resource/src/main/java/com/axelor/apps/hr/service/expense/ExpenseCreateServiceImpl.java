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
package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class ExpenseCreateServiceImpl implements ExpenseCreateService {

  protected PeriodRepository periodRepository;
  protected AppBaseService appBaseService;
  protected ExpenseToolService expenseToolService;
  protected ExpenseRepository expenseRepository;
  protected ExpenseComputationService expenseComputationService;

  @Inject
  public ExpenseCreateServiceImpl(
      PeriodRepository periodRepository,
      AppBaseService appBaseService,
      ExpenseToolService expenseToolService,
      ExpenseRepository expenseRepository,
      ExpenseComputationService expenseComputationService) {
    this.periodRepository = periodRepository;
    this.appBaseService = appBaseService;
    this.expenseToolService = expenseToolService;
    this.expenseRepository = expenseRepository;
    this.expenseComputationService = expenseComputationService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Expense createExpense(
      Company company,
      Employee employee,
      Currency currency,
      BankDetails bankDetails,
      Period period,
      Integer companyCbSelect,
      List<ExpenseLine> expenseLineList)
      throws AxelorException {
    Expense expense = new Expense();
    setExpenseInfo(company, employee, currency, bankDetails, period, companyCbSelect, expense);
    expenseToolService.addExpenseLinesToExpense(expense, expenseLineList);
    expenseComputationService.compute(expense);

    return expenseRepository.save(expense);
  }

  protected void setExpenseInfo(
      Company company,
      Employee employee,
      Currency currency,
      BankDetails bankDetails,
      Period period,
      Integer companyCbSelect,
      Expense expense) {
    expense.setCompany(company);
    expense.setEmployee(employee);
    expense.setCompanyCbSelect(companyCbSelect);
    setCurrency(company, currency, expense);
    setBankDetails(employee, bankDetails, expense);
    setPeriod(company, period, expense);
  }

  protected void setPeriod(Company company, Period period, Expense expense) {
    if (period != null) {
      expense.setPeriod(period);
    } else {
      Period companyPeriod = getCompanyPeriod(company);
      expense.setPeriod(companyPeriod);
    }
  }

  protected Period getCompanyPeriod(Company company) {
    String filter =
        "self.fromDate <= :todayDate "
            + "AND self.toDate >= :todayDate "
            + "AND self.allowExpenseCreation IS TRUE "
            + "AND self.year.company = :company "
            + "AND self.year.typeSelect = :fiscalType";
    return periodRepository
        .all()
        .filter(filter)
        .bind("todayDate", appBaseService.getTodayDate(company))
        .bind("company", company)
        .bind("fiscalType", YearRepository.TYPE_FISCAL)
        .fetchOne();
  }

  protected void setBankDetails(Employee employee, BankDetails bankDetails, Expense expense) {
    BankDetails payCompanyBankDetails =
        employee.getMainEmploymentContract().getPayCompany().getDefaultBankDetails();
    BankDetails activeCompanyBankDetails =
        employee.getUser().getActiveCompany().getDefaultBankDetails();

    if (bankDetails != null) {
      expense.setBankDetails(bankDetails);
    } else {
      if (payCompanyBankDetails != null) {
        expense.setBankDetails(payCompanyBankDetails);
      } else {
        expense.setBankDetails(activeCompanyBankDetails);
      }
    }
  }

  protected void setCurrency(Company company, Currency currency, Expense expense) {
    if (currency != null) {
      expense.setCurrency(currency);
    } else {
      expense.setCurrency(company.getCurrency());
    }
  }
}
