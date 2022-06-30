package com.axelor.apps.base.interfaces;

import com.axelor.apps.base.db.Product;
import java.math.BigDecimal;

public interface OrderLine {
  Product getProduct();

  Long getId();

  BigDecimal getExTaxTotal();
}
