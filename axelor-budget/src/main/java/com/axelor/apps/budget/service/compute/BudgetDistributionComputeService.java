package com.axelor.apps.budget.service.compute;

import com.axelor.apps.budget.db.BudgetDistribution;
import java.math.BigDecimal;
import java.util.List;

public interface BudgetDistributionComputeService {
  void updateMonoBudgetAmounts(
      List<BudgetDistribution> budgetDistributionList, BigDecimal newAmount);
}
