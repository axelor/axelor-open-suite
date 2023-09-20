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
package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class MoveLineBudgetServiceImpl implements MoveLineBudgetService {

  protected MoveLineRepository moveLineRepository;
  protected BudgetService budgetService;
  protected BudgetDistributionService budgetDistributionService;

  @Inject
  public MoveLineBudgetServiceImpl(
      MoveLineRepository moveLineRepository,
      BudgetService budgetService,
      BudgetDistributionService budgetDistributionService) {
    this.moveLineRepository = moveLineRepository;
    this.budgetService = budgetService;
    this.budgetDistributionService = budgetDistributionService;
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(Move move, MoveLine moveLine) {
    if (move == null || moveLine == null) {
      return "";
    }
    moveLine.clearBudgetDistributionList();
    String alertMessage =
        budgetDistributionService.createBudgetDistribution(
            moveLine.getAnalyticMoveLineList(),
            moveLine.getAccount(),
            move.getCompany(),
            move.getDate(),
            moveLine.getCredit().add(moveLine.getDebit()),
            moveLine.getName(),
            moveLine);
    return alertMessage;
  }

  @Override
  public void checkAmountForMoveLine(MoveLine moveLine) throws AxelorException {
    if (moveLine.getBudgetDistributionList() != null
        && !moveLine.getBudgetDistributionList().isEmpty()) {
      for (BudgetDistribution budgetDistribution : moveLine.getBudgetDistributionList()) {
        if (budgetDistribution.getAmount().compareTo(moveLine.getCredit().add(moveLine.getDebit()))
            > 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BudgetExceptionMessage.BUDGET_DISTRIBUTION_LINE_SUM_GREATER_MOVE),
              budgetDistribution.getBudget().getCode(),
              moveLine.getAccount().getCode());
        }
      }
    }
  }

  @Override
  public String getBudgetDomain(Move move, MoveLine moveLine) {
    String budget = "self.budgetLevel.parentBudgetLevel.globalBudget";
    String query =
        String.format(
            "self.totalAmountExpected > 0 AND self.statusSelect = %d",
            BudgetRepository.STATUS_VALIDATED);
    if (move != null) {
      query =
          query.concat(
              String.format(
                  " AND %s.company.id = %d",
                  budget, move.getCompany() != null ? move.getCompany().getId() : 0));
      if (move.getDate() != null) {
        query =
            query.concat(
                String.format(
                    " AND self.fromDate <= '%s' AND self.toDate >= '%s'",
                    move.getDate(), move.getDate()));
      }
    }
    if (moveLine != null) {
      if (AccountTypeRepository.TYPE_INCOME.equals(
          moveLine.getAccount().getAccountType().getTechnicalTypeSelect())) {
        query =
            query.concat(
                String.format(
                    " AND %s.budgetTypeSelect = %d ",
                    budget, BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_SALE));
      } else if (AccountTypeRepository.TYPE_CHARGE.equals(
          moveLine.getAccount().getAccountType().getTechnicalTypeSelect())) {
        query =
            query.concat(
                String.format(
                    " AND %s.budgetTypeSelect in ("
                        + BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_PURCHASE
                        + ","
                        + BudgetLevelRepository
                            .BUDGET_LEVEL_BUDGET_TYPE_SELECT_PURCHASE_AND_INVESTMENT
                        + ")",
                    budget));
      } else if (AccountTypeRepository.TYPE_IMMOBILISATION.equals(
          moveLine.getAccount().getAccountType().getTechnicalTypeSelect())) {
        query =
            query.concat(
                String.format(
                    " AND %s.budgetTypeSelect in ("
                        + BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_INVESTMENT
                        + ","
                        + BudgetLevelRepository
                            .BUDGET_LEVEL_BUDGET_TYPE_SELECT_PURCHASE_AND_INVESTMENT
                        + ")",
                    budget));
      }
    }
    return query;
  }
}
