package com.axelor.apps.bankpayment.db.repo;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveManagementRepository;
import java.math.BigDecimal;
import java.util.List;

public class MoveBankPaymentRepository extends MoveManagementRepository {

  @Override
  public Move copy(Move entity, boolean deep) {
    Move copy = super.copy(entity, deep);

    List<MoveLine> moveLineList = copy.getMoveLineList();

    if (moveLineList != null) {
      moveLineList.forEach(moveLine -> moveLine.setBankReconciledAmount(BigDecimal.ZERO));
    }

    return copy;
  }
}
