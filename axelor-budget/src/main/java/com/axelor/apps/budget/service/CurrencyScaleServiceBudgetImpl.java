package com.axelor.apps.budget.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleServiceImpl;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.db.GlobalBudget;
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
  public BigDecimal getCompanyScaledValue(BudgetLevel budgetLevel, BigDecimal amount) {
    return this.getScaledValue(amount, this.getCompanyScale(budgetLevel.getCompany()));
  }

  @Override
  public BigDecimal getCompanyScaledValue(GlobalBudget globalBudget, BigDecimal amount) {
    return this.getScaledValue(amount, this.getCompanyScale(globalBudget.getCompany()));
  }

  @Override
  public BigDecimal getCompanyScaledValue(
      BudgetDistribution budgetDistribution, BigDecimal amount) {
    return this.getCompanyScaledValue(budgetDistribution.getBudget(), amount);
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
  public int getCompanyScale(BudgetLevel budgetLevel) {
    return this.getCompanyCurrencyScale(budgetLevel.getCompany());
  }

  @Override
  public int getCompanyScale(GlobalBudget globalBudget) {
    return this.getCompanyCurrencyScale(globalBudget.getCompany());
  }

  @Override
  public int getCompanyScale(BudgetDistribution budgetDistribution) {
    return this.getCompanyScale(budgetDistribution.getBudget());
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
