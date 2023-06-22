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
