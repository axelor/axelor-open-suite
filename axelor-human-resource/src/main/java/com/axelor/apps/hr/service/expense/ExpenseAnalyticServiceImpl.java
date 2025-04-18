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

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticAxisService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class ExpenseAnalyticServiceImpl implements ExpenseAnalyticService {

  protected AppAccountService appAccountService;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected AnalyticAxisService analyticAxisService;

  @Inject
  public ExpenseAnalyticServiceImpl(
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AnalyticAxisService analyticAxisService) {
    this.appAccountService = appAccountService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.analyticAxisService = analyticAxisService;
  }

  @Override
  public ExpenseLine computeAnalyticDistribution(ExpenseLine expenseLine) {

    List<AnalyticMoveLine> analyticMoveLineList = expenseLine.getAnalyticMoveLineList();

    if ((analyticMoveLineList == null || analyticMoveLineList.isEmpty())) {
      createAnalyticDistributionWithTemplate(expenseLine);
    }
    if (analyticMoveLineList != null) {
      LocalDate date =
          appAccountService.getTodayDate(
              expenseLine.getExpense() != null
                  ? expenseLine.getExpense().getCompany()
                  : Optional.ofNullable(AuthUtils.getUser())
                      .map(User::getActiveCompany)
                      .orElse(null));
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        analyticMoveLineService.updateAnalyticMoveLine(
            analyticMoveLine, expenseLine.getUntaxedAmount(), date);
      }
    }
    return expenseLine;
  }

  @Override
  public ExpenseLine createAnalyticDistributionWithTemplate(ExpenseLine expenseLine) {

    LocalDate date =
        Optional.ofNullable(expenseLine.getExpenseDate())
            .orElse(
                appAccountService.getTodayDate(
                    expenseLine.getExpense() != null
                        ? expenseLine.getExpense().getCompany()
                        : Optional.ofNullable(AuthUtils.getUser())
                            .map(User::getActiveCompany)
                            .orElse(null)));
    List<AnalyticMoveLine> analyticMoveLineList =
        analyticMoveLineService.generateLines(
            expenseLine.getAnalyticDistributionTemplate(),
            expenseLine.getUntaxedAmount(),
            AnalyticMoveLineRepository.STATUS_FORECAST_INVOICE,
            date);

    expenseLine.setAnalyticMoveLineList(analyticMoveLineList);
    return expenseLine;
  }

  @Override
  public void checkAnalyticAxisByCompany(Expense expense) throws AxelorException {
    if (expense == null || ObjectUtils.isEmpty(expense.getGeneralExpenseLineList())) {
      return;
    }

    for (ExpenseLine expenseLine : expense.getGeneralExpenseLineList()) {
      if (!ObjectUtils.isEmpty(expenseLine.getAnalyticMoveLineList())) {
        analyticAxisService.checkRequiredAxisByCompany(
            expense.getCompany(),
            expenseLine.getAnalyticMoveLineList().stream()
                .map(AnalyticMoveLine::getAnalyticAxis)
                .collect(Collectors.toList()));
      }
    }
  }
}
