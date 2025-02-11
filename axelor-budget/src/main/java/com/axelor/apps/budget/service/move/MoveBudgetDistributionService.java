package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.MoveLine;

public interface MoveBudgetDistributionService {
  void checkChanges(MoveLine moveLine);
}
