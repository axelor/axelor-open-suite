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
package com.axelor.apps.budget.db.repo;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.bankpayment.db.repo.MoveBankPaymentRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.move.MoveLineBudgetService;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

public class MoveBudgetManagementRepository extends MoveBankPaymentRepository {

  @Override
  public Move save(Move move) {
    try {
      if (!CollectionUtils.isEmpty(move.getMoveLineList())
          && move.getStatusSelect() != MoveRepository.STATUS_NEW
          && move.getStatusSelect() != MoveRepository.STATUS_CANCELED) {
        MoveLineBudgetService moveLineBudgetService = Beans.get(MoveLineBudgetService.class);
        for (MoveLine moveLine : move.getMoveLineList()) {
          moveLineBudgetService.checkAmountForMoveLine(moveLine);
        }
      }

    } catch (AxelorException e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }

    super.save(move);
    Beans.get(BudgetService.class).updateBudgetLinesFromMove(move, false);
    return move;
  }

  @Override
  public Move copy(Move entity, boolean deep) {
    Move copy = super.copy(entity, deep);

    if (!CollectionUtils.isEmpty(copy.getMoveLineList())) {
      BudgetDistributionService budgetDistributionService =
          Beans.get(BudgetDistributionService.class);
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
