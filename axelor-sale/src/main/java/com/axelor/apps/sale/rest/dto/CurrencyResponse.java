package com.axelor.apps.sale.rest.dto;

import com.axelor.apps.base.db.Currency;

public class CurrencyResponse {
  private Long currencyId;
  private String code;
  private String name;

  private String symbol;

  public CurrencyResponse(Currency currency) {
    this.currencyId = currency.getId();
    this.code = currency.getCode();
    this.name = currency.getName();
    this.symbol = currency.getSymbol();
  }

  public Long getCurrencyId() {
    return currencyId;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public String getSymbol() {
    return symbol;
  }
}
