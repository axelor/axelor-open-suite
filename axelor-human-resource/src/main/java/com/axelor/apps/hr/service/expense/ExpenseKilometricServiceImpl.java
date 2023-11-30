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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.EmployeeVehicle;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.KilometricAllowParam;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ExpenseKilometricServiceImpl implements ExpenseKilometricService {

  protected HRConfigService hrConfigService;
  protected ExpenseRepository expenseRepository;

  @Inject
  public ExpenseKilometricServiceImpl(
      HRConfigService hrConfigService, ExpenseRepository expenseRepository) {
    this.hrConfigService = hrConfigService;
    this.expenseRepository = expenseRepository;
  }

  @Override
  public Product getKilometricExpenseProduct(Expense expense) throws AxelorException {
    HRConfig hrConfig = hrConfigService.getHRConfig(expense.getCompany());
    return hrConfigService.getKilometricExpenseProduct(hrConfig);
  }

  @Override
  public List<KilometricAllowParam> getListOfKilometricAllowParamVehicleFilter(
      ExpenseLine expenseLine) throws AxelorException {

    List<KilometricAllowParam> kilometricAllowParamList = new ArrayList<>();

    Expense expense = expenseLine.getExpense();

    if (expense == null) {
      return kilometricAllowParamList;
    }

    if (expense.getId() != null) {
      expense = expenseRepository.find(expense.getId());
    }

    LocalDate expenseDate = expenseLine.getExpenseDate();
    if (expense.getEmployee() == null || expenseDate == null) {
      return kilometricAllowParamList;
    }

    List<EmployeeVehicle> vehicleList = expense.getEmployee().getEmployeeVehicleList();

    for (EmployeeVehicle vehicle : vehicleList) {
      LocalDate startDate = vehicle.getStartDate();
      LocalDate endDate = vehicle.getEndDate();
      if (startDate == null) {
        if (endDate == null || !expenseDate.isAfter(endDate)) {
          kilometricAllowParamList.add(vehicle.getKilometricAllowParam());
        }
      } else if (endDate == null) {
        if (!expenseDate.isBefore(startDate)) {
          kilometricAllowParamList.add(vehicle.getKilometricAllowParam());
        }
      } else if (!expenseDate.isBefore(startDate) && !expenseDate.isAfter(endDate)) {
        kilometricAllowParamList.add(vehicle.getKilometricAllowParam());
      }
    }
    return kilometricAllowParamList;
  }

  @Override
  public List<KilometricAllowParam> getListOfKilometricAllowParamVehicleFilter(
      ExpenseLine expenseLine, Expense expense) throws AxelorException {

    List<KilometricAllowParam> kilometricAllowParamList = new ArrayList<>();

    if (expense == null) {
      return kilometricAllowParamList;
    }

    if (expense.getId() != null) {
      expense = expenseRepository.find(expense.getId());
    }

    if (expense.getEmployee() != null) {
      return kilometricAllowParamList;
    }

    List<EmployeeVehicle> vehicleList = expense.getEmployee().getEmployeeVehicleList();
    LocalDate expenseDate = expenseLine.getExpenseDate();

    if (expenseDate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.KILOMETRIC_ALLOWANCE_NO_DATE_SELECTED));
    }

    for (EmployeeVehicle vehicle : vehicleList) {
      if (vehicle.getKilometricAllowParam() == null) {
        break;
      }
      LocalDate startDate = vehicle.getStartDate();
      LocalDate endDate = vehicle.getEndDate();
      if ((startDate == null && (endDate == null || !expenseDate.isAfter(endDate)))
          || (endDate == null
              && (!expenseDate.isBefore(startDate)
                  || (!expenseDate.isBefore(startDate) && !expenseDate.isAfter(endDate))))) {
        kilometricAllowParamList.add(vehicle.getKilometricAllowParam());
      }
    }
    return kilometricAllowParamList;
  }
}
