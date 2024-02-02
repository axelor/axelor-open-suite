package com.axelor.apps.budget.service;

import com.axelor.apps.base.interfaces.LocalDateInterval;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import java.util.Objects;

public class BudgetComputeHiddenDateServiceImpl implements BudgetComputeHiddenDateService {

  @Override
  public boolean isHidden(LocalDateInterval budgetDateInterval) {
    boolean isHidden = true;
    if (budgetDateInterval.getId() == null) {
      isHidden = false;
    } else if (budgetDateInterval.getFromDate() != null && budgetDateInterval.getToDate() != null) {
      LocalDateInterval savedGlobalBudget =
          JPA.em()
              .find(EntityHelper.getEntityClass(budgetDateInterval), budgetDateInterval.getId());
      boolean datesMatch =
          Objects.equals(savedGlobalBudget.getFromDate(), budgetDateInterval.getFromDate())
              && Objects.equals(savedGlobalBudget.getToDate(), budgetDateInterval.getToDate());
      if (!datesMatch) {
        isHidden = false;
      }
    }
    return isHidden;
  }
}
