package com.axelor.apps.budget.db.repo;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import org.apache.commons.collections.CollectionUtils;

public class BudgetLevelManagementRepository extends BudgetLevelRepository {

  @Override
  public BudgetLevel copy(BudgetLevel entity, boolean deep) {
    BudgetLevel copy = super.copy(entity, deep);

    copy.setCode(entity.getCode() + " (" + I18n.get("copy") + ")");

    copy.setStatusSelect(BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_DRAFT);
    copy.setArchived(false);

    copy.setTotalAmountExpected(entity.getTotalAmountExpected());
    copy.setTotalAmountCommitted(BigDecimal.ZERO);
    copy.setRealizedWithNoPo(BigDecimal.ZERO);
    copy.setRealizedWithPo(BigDecimal.ZERO);
    copy.setSimulatedAmount(BigDecimal.ZERO);
    copy.setTotalAmountAvailable(entity.getTotalAmountExpected());
    copy.setAvailableAmountWithSimulated(entity.getTotalAmountExpected());
    copy.setTotalFirmGap(BigDecimal.ZERO);
    copy.setTotalAmountPaid(BigDecimal.ZERO);

    if (!CollectionUtils.isEmpty(copy.getBudgetLevelList())) {
      for (BudgetLevel child : copy.getBudgetLevelList()) {
        copy.removeBudgetLevelListItem(child);
        child = this.copy(child, deep);
        copy.addBudgetLevelListItem(child);
      }
    } else if (!CollectionUtils.isEmpty(copy.getBudgetList())) {
      for (Budget budget : copy.getBudgetList()) {
        budget = Beans.get(BudgetService.class).resetBudget(budget);
      }
    }

    return copy;
  }
}
