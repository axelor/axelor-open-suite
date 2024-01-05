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
