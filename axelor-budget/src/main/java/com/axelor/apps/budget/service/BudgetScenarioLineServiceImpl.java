package com.axelor.apps.budget.service;

import com.axelor.apps.base.db.Year;
import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.db.BudgetScenarioLine;
import com.axelor.apps.budget.db.repo.BudgetScenarioLineRepository;
import com.axelor.apps.budget.db.repo.BudgetScenarioRepository;
import com.google.inject.Inject;
import java.util.*;
import com.axelor.db.Query;

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
        String yearName = year.getName();
        if (yearName.matches("\\d+")) {
          int yearValue = Integer.parseInt(yearName);
          yearIntegers.add(yearValue);
        }
      }
    Collections.sort(yearIntegers);
    return yearIntegers;
  }

@Override
public boolean countBudgetScenarioLines(BudgetScenario budgetScenario) {
	Query<BudgetScenarioLine> lineQuery =
	        budgetScenarioLineRepository.all().filter("self.budgetScenario = :budgetScenario").bind("budgetScenario", budgetScenario);
 if(lineQuery.count()>0) {
	 return true;
 }
 return false;
}
}
