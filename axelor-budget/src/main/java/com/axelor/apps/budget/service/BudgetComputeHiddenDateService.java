package com.axelor.apps.budget.service;

import com.axelor.apps.base.interfaces.LocalDateInterval;

public interface BudgetComputeHiddenDateService {
  boolean isHidden(LocalDateInterval budgetDateInterval);
}
