package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.budget.db.BudgetDistribution;
import java.util.List;

public interface MoveLineToolBudgetService {
  List<BudgetDistribution> copyBudgetDistributionList(MoveLine moveLine);
}
