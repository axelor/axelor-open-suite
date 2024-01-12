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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class ExpenseLineServiceImpl implements ExpenseLineService {

  protected ExpenseLineRepository expenseLineRepository;
  protected HRConfigService hrConfigService;

  @Inject
  public ExpenseLineServiceImpl(
      ExpenseLineRepository expenseLineRepository, HRConfigService hrConfigService) {
    this.expenseLineRepository = expenseLineRepository;
    this.hrConfigService = hrConfigService;
  }

  @Override
  public List<ExpenseLine> getExpenseLineList(Expense expense) {
    List<ExpenseLine> expenseLineList = new ArrayList<>();
    if (expense.getGeneralExpenseLineList() != null) {
      expenseLineList.addAll(expense.getGeneralExpenseLineList());
    }
    if (expense.getKilometricExpenseLineList() != null) {
      expenseLineList.addAll(expense.getKilometricExpenseLineList());
    }
    return expenseLineList;
  }

  @Override
  public void completeExpenseLines(Expense expense) {
    List<ExpenseLine> expenseLineList =
        expenseLineRepository
            .all()
            .filter("self.expense.id = :_expenseId")
            .bind("_expenseId", expense.getId())
            .fetch();
    List<ExpenseLine> kilometricExpenseLineList = expense.getKilometricExpenseLineList();
    List<ExpenseLine> generalExpenseLineList = expense.getGeneralExpenseLineList();

    // removing expense from one O2M also remove the link
    for (ExpenseLine expenseLine : expenseLineList) {
      if ((CollectionUtils.isEmpty(kilometricExpenseLineList)
              || !kilometricExpenseLineList.contains(expenseLine))
          && (CollectionUtils.isEmpty(generalExpenseLineList)
              || !generalExpenseLineList.contains(expenseLine))) {
        expenseLine.setExpense(null);
        expenseLineRepository.remove(expenseLine);
      }
    }

    // adding expense in one O2M also add the link
    if (kilometricExpenseLineList != null) {
      for (ExpenseLine kilometricLine : kilometricExpenseLineList) {
        if (!expenseLineList.contains(kilometricLine)) {
          kilometricLine.setExpense(expense);
        }
      }
    }
    if (generalExpenseLineList != null) {
      for (ExpenseLine generalExpenseLine : generalExpenseLineList) {
        if (!expenseLineList.contains(generalExpenseLine)) {
          generalExpenseLine.setExpense(expense);
        }
      }
    }
  }

  @Override
  public boolean isThereOverAmountLimit(Expense expense) {
    return expense.getGeneralExpenseLineList().stream()
        .anyMatch(
            line -> {
              BigDecimal amountLimit = line.getExpenseProduct().getAmountLimit();
              return amountLimit.compareTo(BigDecimal.ZERO) != 0
                  && amountLimit.compareTo(line.getTotalAmount()) < 0;
            });
  }

  @Override
  public boolean isFilePdfOrImage(ExpenseLine expenseLine) {
    MetaFile metaFile = expenseLine.getJustificationMetaFile();
    if (metaFile == null) {
      return false;
    }
    String fileType = metaFile.getFileType();
    return isFilePdf(expenseLine) || fileType.startsWith("image");
  }

  @Override
  public boolean isFilePdf(ExpenseLine expenseLine) {
    MetaFile metaFile = expenseLine.getJustificationMetaFile();
    if (metaFile == null) {
      return false;
    }
    String fileType = metaFile.getFileType();
    return "application/pdf".equals(fileType);
  }

  @Override
  public Product getExpenseProduct(ExpenseLine expenseLine) throws AxelorException {
    boolean isKilometricLine = expenseLine.getIsKilometricLine();
    if (!isKilometricLine) {
      return null;
    }

    User user = AuthUtils.getUser();
    if (user != null) {
      Company activeCompany = user.getActiveCompany();
      if (activeCompany != null) {
        return hrConfigService.getHRConfig(activeCompany).getKilometricExpenseProduct();
      }
    }
    return null;
  }
}
