package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.BudgetLine;
import java.math.BigDecimal;

public class BudgetLineResetToolServiceImpl implements BudgetLineResetToolService {

  @Override
  public BudgetLine resetBudgetLine(BudgetLine entity) {

    entity.setArchived(false);
    entity.setAmountExpected(entity.getAmountExpected());
    entity.setAmountCommitted(BigDecimal.ZERO);
    entity.setRealizedWithNoPo(BigDecimal.ZERO);
    entity.setRealizedWithPo(BigDecimal.ZERO);
    entity.setAvailableAmount(entity.getAmountExpected());
    entity.setAmountRealized(BigDecimal.ZERO);
    entity.setFirmGap(BigDecimal.ZERO);
    entity.setAmountPaid(BigDecimal.ZERO);
    entity.setToBeCommittedAmount(BigDecimal.ZERO);

    return entity;
  }
}
