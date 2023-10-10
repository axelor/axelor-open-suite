package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import org.apache.commons.collections.CollectionUtils;

public class BudgetResetToolServiceImpl implements BudgetResetToolService {

  private final BudgetLineResetToolService budgetLineResetToolService;

  @Inject
  public BudgetResetToolServiceImpl(BudgetLineResetToolService budgetLineResetToolService) {
    this.budgetLineResetToolService = budgetLineResetToolService;
  }

  @Override
  public Budget resetBudget(Budget entity) {

    entity.setStatusSelect(BudgetRepository.STATUS_DRAFT);
    entity.setArchived(false);

    entity.setTotalAmountExpected(entity.getTotalAmountExpected());
    entity.setTotalAmountCommitted(BigDecimal.ZERO);
    entity.setRealizedWithNoPo(BigDecimal.ZERO);
    entity.setRealizedWithPo(BigDecimal.ZERO);
    entity.setSimulatedAmount(BigDecimal.ZERO);
    entity.setAvailableAmount(entity.getTotalAmountExpected());
    entity.setAvailableAmountWithSimulated(entity.getTotalAmountExpected());
    entity.setTotalAmountRealized(BigDecimal.ZERO);
    entity.setTotalFirmGap(BigDecimal.ZERO);
    entity.setTotalAmountPaid(BigDecimal.ZERO);

    if (!CollectionUtils.isEmpty(entity.getBudgetLineList())) {
      for (BudgetLine child : entity.getBudgetLineList()) {
        child = budgetLineResetToolService.resetBudgetLine(child);
      }
    }
    return entity;
  }
}
