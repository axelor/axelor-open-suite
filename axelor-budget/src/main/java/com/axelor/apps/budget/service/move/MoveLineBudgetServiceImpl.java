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
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.budget.db.BudgetDistribution;
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
}
