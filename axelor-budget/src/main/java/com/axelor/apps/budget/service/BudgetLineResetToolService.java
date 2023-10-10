package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.BudgetLine;

public interface BudgetLineResetToolService {

  /**
   * Reset budget line amounts when the budget line is a copy
   *
   * @param entity
   * @return BudgetLine
   */
  BudgetLine resetBudgetLine(BudgetLine entity);
}
