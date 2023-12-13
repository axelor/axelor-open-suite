package com.axelor.apps.budget.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.budget.db.BudgetScenario;
import java.math.BigDecimal;

public interface CurrencyScaleServiceBudget {

  BigDecimal getCompanyScaledValue(BudgetScenario budgetScenario, BigDecimal amount);

  int getCompanyScale(BudgetScenario budgetScenario);

  int getCompanyScale(Company company);
}
