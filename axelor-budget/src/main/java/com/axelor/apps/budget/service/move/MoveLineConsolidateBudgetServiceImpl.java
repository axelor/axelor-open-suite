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
package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.moveline.MoveLineConsolidateServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;

public class MoveLineConsolidateBudgetServiceImpl extends MoveLineConsolidateServiceImpl {

  @Inject
  public MoveLineConsolidateBudgetServiceImpl(
      MoveLineToolService moveLineToolService, MoveToolService moveToolService) {
    super(moveLineToolService, moveToolService);
  }

  @Override
  public MoveLine consolidateMoveLine(MoveLine moveLine, MoveLine consolidateMoveLine) {
    consolidateMoveLine = super.consolidateMoveLine(moveLine, consolidateMoveLine);

    if (!ObjectUtils.isEmpty(moveLine.getBudgetDistributionList())) {
      for (BudgetDistribution budgetDistribution : moveLine.getBudgetDistributionList()) {
        consolidateMoveLine.addBudgetDistributionListItem(budgetDistribution);
      }
    }

    return consolidateMoveLine;
  }
}
