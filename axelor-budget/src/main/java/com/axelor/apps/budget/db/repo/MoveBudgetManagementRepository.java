/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.db.repo;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.bankpayment.db.repo.MoveBankPaymentRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.move.MoveLineBudgetService;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

public class MoveBudgetManagementRepository extends MoveBankPaymentRepository {

  protected AppBaseService appBaseService;
  protected MoveLineBudgetService moveLineBudgetService;
  protected BudgetDistributionService budgetDistributionService;
  protected BudgetService budgetService;

  @Inject
  public MoveBudgetManagementRepository(
      AppBaseService appBaseService,
      MoveLineBudgetService moveLineBudgetService,
      BudgetDistributionService budgetDistributionService,
      BudgetService budgetService) {
    this.appBaseService = appBaseService;
    this.moveLineBudgetService = moveLineBudgetService;
    this.budgetDistributionService = budgetDistributionService;
    this.budgetService = budgetService;
  }

  @Override
  public Move save(Move move) {
    if (appBaseService.isApp("budget")) {
      try {
        if (!CollectionUtils.isEmpty(move.getMoveLineList())
            && move.getStatusSelect() != MoveRepository.STATUS_CANCELED) {

          moveLineBudgetService.manageMonoBudget(move);

          for (MoveLine moveLine : move.getMoveLineList()) {
            moveLineBudgetService.checkAmountForMoveLine(moveLine);
            moveLineBudgetService.negateAmount(moveLine, move);
          }
        }

        budgetService.updateBudgetLinesFromMove(move, false);

      } catch (AxelorException e) {
        throw new PersistenceException(e.getLocalizedMessage());
      }
    }

    super.save(move);

    return move;
  }

  @Override
  public Move copy(Move entity, boolean deep) {
    Move copy = super.copy(entity, deep);

    if (!CollectionUtils.isEmpty(copy.getMoveLineList()) && appBaseService.isApp("budget")) {

      for (MoveLine ml : copy.getMoveLineList()) {
        ml.setIsBudgetImputed(false);
        if (!CollectionUtils.isEmpty(ml.getBudgetDistributionList())) {
          for (BudgetDistribution bd : ml.getBudgetDistributionList()) {
            budgetDistributionService.computeBudgetDistributionSumAmount(bd, copy.getDate());
          }
        }
      }
    }
    return copy;
  }
}
