package com.axelor.apps.budget.db.repo;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.bankpayment.db.repo.MoveBankPaymentRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.BudgetBudgetService;
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
        for (MoveLine moveLine : move.getMoveLineList()) {
          Beans.get(MoveLineBudgetService.class).checkAmountForMoveLine(moveLine);
        }
      }

    } catch (AxelorException e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }

    super.save(move);
    Beans.get(BudgetBudgetService.class).updateBudgetLinesFromMove(move, false);
    return move;
  }

  @Override
  public Move copy(Move entity, boolean deep) {
    Move copy = super.copy(entity, deep);

    if (!CollectionUtils.isEmpty(copy.getMoveLineList())) {
      for (MoveLine ml : copy.getMoveLineList()) {
        ml.setIsBudgetImputed(false);
      }
    }
    return copy;
  }
}
