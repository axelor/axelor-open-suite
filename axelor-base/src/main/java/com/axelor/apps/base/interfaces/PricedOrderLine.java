package com.axelor.apps.base.interfaces;

import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import java.math.BigDecimal;

public interface PricedOrderLine {
  TaxLine getTaxLine();

  TaxEquiv getTaxEquiv();

  BigDecimal getExTaxTotal();
}
