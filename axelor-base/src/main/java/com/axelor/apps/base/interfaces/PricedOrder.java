package com.axelor.apps.base.interfaces;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.PriceList;

public interface PricedOrder {
  Currency getCurrency();

  PriceList getPriceList();

  FiscalPosition getFiscalPosition();
}
