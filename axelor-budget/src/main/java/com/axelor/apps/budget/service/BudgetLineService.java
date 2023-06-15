package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.BudgetLine;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetLineService {

  /**
   * Filter budget line list with date to find the budget line associed to this date and return it
   *
   * @param budgetLineList, date
   * @return BudgetLine
   */
  Optional<BudgetLine> findBudgetLineAtDate(List<BudgetLine> budgetLineList, LocalDate date);

  /**
   * Reset budget line amounts when the budget line is a copy
   *
   * @param entity
   * @return BudgetLine
   */
  BudgetLine resetBudgetLine(BudgetLine entity);
}
