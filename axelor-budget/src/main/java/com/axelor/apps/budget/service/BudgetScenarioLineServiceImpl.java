package com.axelor.apps.budget.service;

import com.axelor.apps.base.db.Year;
import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.db.repo.BudgetScenarioLineRepository;
import com.google.inject.Inject;
import java.util.*;

public class BudgetScenarioLineServiceImpl implements BudgetScenarioLineService {
  protected BudgetScenarioLineRepository budgetScenarioLineRepository;

  @Inject
  public BudgetScenarioLineServiceImpl(BudgetScenarioLineRepository budgetScenarioLineRepository) {
    this.budgetScenarioLineRepository = budgetScenarioLineRepository;
  }

  @Override
  public ArrayList<Integer> getFiscalYears(BudgetScenario budgetScenario) {
    ArrayList<Integer> yearIntegers = new ArrayList<>();
    Set<Year> yearSet = budgetScenario.getYearSet();
    for (Year year : yearSet) {
      int yearValue = year.getFromDate().getYear();

      yearIntegers.add(yearValue);
    }
    Collections.sort(yearIntegers);
    return yearIntegers;
  }
}
