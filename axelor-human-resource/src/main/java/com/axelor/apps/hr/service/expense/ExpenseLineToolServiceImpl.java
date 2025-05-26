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
package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricAllowParam;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.KilometricExpenseService;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.math.BigDecimal;
import org.apache.commons.collections.CollectionUtils;

public class ExpenseLineToolServiceImpl implements ExpenseLineToolService {
  protected AppHumanResourceService appHumanResourceService;
  protected KilometricService kilometricService;
  protected final KilometricExpenseService kilometricExpenseService;

  @Inject
  public ExpenseLineToolServiceImpl(
      AppHumanResourceService appHumanResourceService,
      KilometricService kilometricService,
      KilometricExpenseService kilometricExpenseService) {
    this.appHumanResourceService = appHumanResourceService;
    this.kilometricService = kilometricService;
    this.kilometricExpenseService = kilometricExpenseService;
  }

  @Override
  public void computeAmount(Employee employee, ExpenseLine expenseLine) throws AxelorException {
    BigDecimal amount = kilometricExpenseService.computeKilometricExpense(expenseLine, employee);
    expenseLine.setTotalAmount(amount);
    expenseLine.setUntaxedAmount(amount);
  }

  @Override
  public void computeDistance(BigDecimal distance, ExpenseLine expenseLine) throws AxelorException {
    if (distance == null) {
      expenseLine.setDistance(BigDecimal.ZERO);
    }

    expenseLine.setDistance(distance);
    if (appHumanResourceService.getAppExpense().getComputeDistanceWithWebService()
        && distance == null) {
      expenseLine.setDistance(kilometricService.computeDistance(expenseLine));
    }
  }

  protected void setAmountAndTax(
      Product expenseProduct,
      BigDecimal totalAmount,
      BigDecimal totalTax,
      ExpenseLine expenseLine) {
    if (totalAmount != null) {
      expenseLine.setTotalAmount(totalAmount);
    }

    if (totalTax != null) {
      expenseLine.setTotalTax(totalTax);

      if (totalAmount != null) {
        expenseLine.setUntaxedAmount(totalAmount.subtract(totalTax));
      }

      if (expenseProduct.getBlockExpenseTax()) {
        expenseLine.setTotalTax(BigDecimal.ZERO);
        expenseLine.setUntaxedAmount(totalAmount);
      }
    }
  }

  protected void checkExpenseProduct(Product expenseProduct) throws AxelorException {
    User user = AuthUtils.getUser();
    if (user != null) {
      Employee userEmployee = user.getEmployee();
      if (userEmployee != null
          && !userEmployee.getHrManager()
          && expenseProduct.getUnavailableToUsers()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_EXPENSE_TYPE_NOT_ALLOWED));
      }
    }
  }

  @Override
  public void setGeneralExpenseLineInfo(
      Product expenseProduct,
      BigDecimal totalAmount,
      BigDecimal totalTax,
      MetaFile justificationMetaFile,
      ExpenseLine expenseLine)
      throws AxelorException {

    if (expenseProduct != null) {
      checkExpenseProduct(expenseProduct);
      if (expenseProduct.getDeductLunchVoucher()) {
        expenseLine.setIsAloneMeal(
            CollectionUtils.isEmpty(expenseLine.getInvitedCollaboratorSet()));
      }
      expenseLine.setExpenseProduct(expenseProduct);
    }

    if (justificationMetaFile != null) {
      expenseLine.setJustificationMetaFile(justificationMetaFile);
    }

    setAmountAndTax(expenseProduct, totalAmount, totalTax, expenseLine);
  }

  @Override
  public boolean isKilometricExpenseLine(ExpenseLine expenseLine) {
    Product expenseProduct = expenseLine.getExpenseProduct();
    KilometricAllowParam kilometricAllowParam = expenseLine.getKilometricAllowParam();
    return expenseProduct != null && kilometricAllowParam != null;
  }
}
