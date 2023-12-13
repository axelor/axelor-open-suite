package com.axelor.apps.budget.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleServiceImpl;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetScenario;
import java.math.BigDecimal;

public class CurrencyScaleServiceBudgetImpl extends CurrencyScaleServiceImpl
    implements CurrencyScaleServiceBudget {

  @Override
  public BigDecimal getCompanyScaledValue(BudgetScenario budgetScenario, BigDecimal amount) {
    return this.getScaledValue(amount, this.getCompanyScale(budgetScenario.getCompany()));
  }

  @Override
  public BigDecimal getCompanyScaledValue(Budget budget, BigDecimal amount) {
    return this.getScaledValue(amount, this.getCompanyScale(budget.getCompany()));
  }

  @Override
  public int getCompanyScale(BudgetScenario budgetScenario) {
    return this.getCompanyScale(budgetScenario.getCompany());
  }

  @Override
  public int getCompanyScale(Budget budget) {
    return this.getCompanyScale(budget.getCompany());
  }

  @Override
  public int getCompanyScale(Company company) {
    return this.getCompanyCurrencyScale(company);
  }

  protected int getCompanyCurrencyScale(Company company) {
    return company != null && company.getCurrency() != null
        ? this.getCurrencyScale(company.getCurrency())
        : this.getScale();
  }

  protected int getCurrencyScale(Currency currency) {
    return currency != null ? currency.getNumberOfDecimals() : this.getScale();
  }
}
