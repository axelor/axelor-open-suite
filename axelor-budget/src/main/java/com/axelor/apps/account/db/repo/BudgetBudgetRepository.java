package com.axelor.apps.budget.db.repo;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.service.BudgetBudgetService;
import com.axelor.inject.Beans;
import java.math.BigDecimal;

public class BudgetBudgetRepository extends BudgetRepository {

  @Override
  public Budget save(Budget entity) {
    entity.setAvailableAmount(
        (entity
                        .getTotalAmountExpected()
                        .subtract(entity.getRealizedWithPo())
                        .subtract(entity.getRealizedWithNoPo()))
                    .compareTo(BigDecimal.ZERO)
                > 0
            ? entity
                .getTotalAmountExpected()
                .subtract(entity.getRealizedWithPo())
                .subtract(entity.getRealizedWithNoPo())
            : BigDecimal.ZERO);

    BudgetBudgetService budgetBudgetService = Beans.get(BudgetBudgetService.class);

    entity = super.save(entity);

    budgetBudgetService.computeBudgetLevelTotals(entity);

    return entity;
  }
}
