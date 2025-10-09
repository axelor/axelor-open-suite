package com.axelor.apps.budget.service.compute;

import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.common.ObjectUtils;
import com.axelor.studio.db.AppBudget;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class BudgetDistributionComputeServiceImpl implements BudgetDistributionComputeService {

  protected AppBudgetService appBudgetService;

  @Inject
  public BudgetDistributionComputeServiceImpl(AppBudgetService appBudgetService) {
    this.appBudgetService = appBudgetService;
  }

  @Override
  public void updateMonoBudgetAmounts(
      List<BudgetDistribution> budgetDistributionList, BigDecimal newAmount) {
    if (Optional.ofNullable(appBudgetService.getAppBudget())
            .map(AppBudget::getManageMultiBudget)
            .orElse(true)
        || ObjectUtils.isEmpty(budgetDistributionList)
        || budgetDistributionList.size() != 1) {
      return;
    }

    BudgetDistribution budgetDistribution = budgetDistributionList.get(0);
    budgetDistribution.setAmount(newAmount);
  }
}
