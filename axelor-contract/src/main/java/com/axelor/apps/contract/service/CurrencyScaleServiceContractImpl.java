package com.axelor.apps.contract.service;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleServiceImpl;
import com.axelor.apps.contract.db.Contract;
import java.math.BigDecimal;

public class CurrencyScaleServiceContractImpl extends CurrencyScaleServiceImpl
    implements CurrencyScaleServiceContract {

  @Override
  public BigDecimal getScaledValue() {
    return null;
  }

  @Override
  public int getScale(Contract contract) {
    return this.getCurrencyScale(contract.getCurrency());
  }

  protected int getCurrencyScale(Currency currency) {
    return currency != null ? currency.getNumberOfDecimals() : this.getScale();
  }
}
