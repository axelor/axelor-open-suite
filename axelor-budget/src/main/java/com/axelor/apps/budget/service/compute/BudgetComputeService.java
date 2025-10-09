package com.axelor.apps.budget.service.compute;

import com.axelor.apps.budget.db.Budget;

public interface BudgetComputeService {
  void computeBudgetFieldsWithLines(Budget budget);
}
