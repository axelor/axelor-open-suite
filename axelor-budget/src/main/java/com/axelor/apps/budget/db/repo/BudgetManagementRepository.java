package com.axelor.apps.budget.db.repo;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.service.BudgetLevelService;
import com.axelor.inject.Beans;
import java.math.BigDecimal;

public class BudgetManagementRepository extends BudgetRepository {

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

    BudgetLevelService budgetLevelService = Beans.get(BudgetLevelService.class);

    entity = super.save(entity);

    budgetLevelService.computeBudgetLevelTotals(entity);

    return entity;
  }
}
