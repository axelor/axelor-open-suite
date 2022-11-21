package com.axelor.apps.base.interfaces;

import com.axelor.apps.account.db.TaxLine;
import java.math.BigDecimal;

public interface OrderLineTax {
  BigDecimal getExTaxBase();

  TaxLine getTaxLine();
}
