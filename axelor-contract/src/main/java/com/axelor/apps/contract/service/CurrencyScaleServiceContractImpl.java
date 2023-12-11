package com.axelor.apps.contract.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleServiceImpl;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import java.math.BigDecimal;

public class CurrencyScaleServiceContractImpl extends CurrencyScaleServiceImpl
    implements CurrencyScaleServiceContract {

  @Override
  public BigDecimal getScaledValue(Contract contract, BigDecimal amount) {
    return this.getScaledValue(amount, this.getScale(contract.getCurrency()));
  }

  @Override
  public BigDecimal getCompanyScaledValue(Contract contract, BigDecimal amount) {
    return this.getScaledValue(amount, this.getCompanyScale(contract.getCompany()));
  }

  @Override
  public BigDecimal getScaledValue(ContractLine contractLine, BigDecimal amount) {
    return contractLine.getContractVersion() != null
        ? this.getScaledValue(contractLine.getContractVersion().getContract(), amount)
        : this.getScaledValue(amount);
  }

  @Override
  public BigDecimal getCompanyScaledValue(ContractLine contractLine, BigDecimal amount) {
    return contractLine.getContractVersion() != null
        ? this.getCompanyScaledValue(contractLine.getContractVersion().getContract(), amount)
        : this.getScaledValue(amount);
  }

  @Override
  public int getScale(Contract contract) {
    return this.getCurrencyScale(contract.getCurrency());
  }

  @Override
  public int getCompanyScale(Company company) {
    return this.getCompanyCurrencyScale(company);
  }

  @Override
  public int getScale(Currency currency) {
    return this.getCurrencyScale(currency);
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
