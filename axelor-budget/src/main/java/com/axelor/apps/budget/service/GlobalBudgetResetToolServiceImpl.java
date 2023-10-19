package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class GlobalBudgetResetToolServiceImpl implements GlobalBudgetResetToolService {

  private final BudgetLevelResetToolService budgetLevelResetToolService;

  @Inject
  public GlobalBudgetResetToolServiceImpl(BudgetLevelResetToolService budgetLevelResetToolService) {
    this.budgetLevelResetToolService = budgetLevelResetToolService;
  }

  public void resetGlobalBudget(GlobalBudget globalBudget) {
    globalBudget.setCode(globalBudget.getCode() + " (" + I18n.get("copy") + ")");
    globalBudget.setStatusSelect(GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_DRAFT);
    globalBudget.setArchived(false);

    globalBudget.setTotalAmountCommitted(BigDecimal.ZERO);
    globalBudget.setTotalAmountAvailable(globalBudget.getTotalAmountExpected());
    globalBudget.setAvailableAmountWithSimulated(globalBudget.getTotalAmountExpected());
    globalBudget.setRealizedWithNoPo(BigDecimal.ZERO);
    globalBudget.setRealizedWithPo(BigDecimal.ZERO);
    globalBudget.setSimulatedAmount(BigDecimal.ZERO);
    globalBudget.setTotalFirmGap(BigDecimal.ZERO);
    globalBudget.setTotalAmountPaid(BigDecimal.ZERO);
    globalBudget.setActiveVersion(null);
    globalBudget.clearBudgetVersionList();
    globalBudget.clearBudgetList();
    List<BudgetLevel> budgetLevels = globalBudget.getBudgetLevelList();

    if (ObjectUtils.notEmpty(budgetLevels)) {
      budgetLevels.forEach(budgetLevelResetToolService::resetBudgetLevel);
    }
  }
}
