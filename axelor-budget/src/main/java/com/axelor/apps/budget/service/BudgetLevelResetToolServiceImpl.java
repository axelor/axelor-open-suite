package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;
import org.apache.commons.collections.CollectionUtils;

public class BudgetLevelResetToolServiceImpl implements BudgetLevelResetToolService {

  private final BudgetService budgetService;

  @Inject
  public BudgetLevelResetToolServiceImpl(BudgetService budgetService) {
    this.budgetService = budgetService;
  }

  @Override
  public void resetBudgetLevel(BudgetLevel budgetLevel) {

    budgetLevel.setStatusSelect(BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_DRAFT);

    budgetLevel.setArchived(false);

    budgetLevel.setTotalAmountCommitted(BigDecimal.ZERO);
    budgetLevel.setTotalAmountAvailable(budgetLevel.getTotalAmountExpected());
    budgetLevel.setAvailableAmountWithSimulated(budgetLevel.getTotalAmountExpected());
    budgetLevel.setRealizedWithNoPo(BigDecimal.ZERO);
    budgetLevel.setRealizedWithPo(BigDecimal.ZERO);
    budgetLevel.setSimulatedAmount(BigDecimal.ZERO);
    budgetLevel.setTotalFirmGap(BigDecimal.ZERO);
    budgetLevel.setTotalAmountPaid(BigDecimal.ZERO);

    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      budgetLevel.getBudgetLevelList().forEach(child -> resetBudgetLevel(child));
    } else if (!CollectionUtils.isEmpty(budgetLevel.getBudgetList())) {
      budgetLevel.getBudgetList().forEach(budgetService::resetBudget);
    }
  }
}
