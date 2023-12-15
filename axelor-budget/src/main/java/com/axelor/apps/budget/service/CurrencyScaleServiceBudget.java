package com.axelor.apps.budget.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetScenario;
import java.math.BigDecimal;

public interface CurrencyScaleServiceBudget {

  BigDecimal getCompanyScaledValue(BudgetScenario budgetScenario, BigDecimal amount);

  BigDecimal getCompanyScaledValue(Budget budget, BigDecimal amount);

  BigDecimal getCompanyScaledValue(BudgetLevel budgetLevel, BigDecimal amount);

  int getCompanyScale(BudgetScenario budgetScenario);

  int getCompanyScale(Budget budget);

  int getCompanyScale(BudgetLevel budgetLevel);

  int getCompanyScale(Company company);
}
