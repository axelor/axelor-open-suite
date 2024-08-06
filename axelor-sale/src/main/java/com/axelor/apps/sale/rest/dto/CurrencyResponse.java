package com.axelor.apps.sale.rest.dto;

import com.axelor.apps.base.db.Company;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseStructure;

public class CurrencyResponse extends ResponseStructure {
  private Long currencyId;
  private String code;
  private String name;

  private String symbol;

  public CurrencyResponse(Company company) {
    super(ObjectFinder.NO_VERSION);
    this.currencyId = company.getCurrency().getId();
    this.code = company.getCurrency().getCode();
    this.name = company.getCurrency().getName();
    this.symbol = company.getCurrency().getSymbol();
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
