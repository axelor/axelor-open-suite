package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;

public interface CurrencyScaleServiceSale {

  BigDecimal getScaledValue(SaleOrder saleOrder, BigDecimal amount);

  BigDecimal getCompanyScaledValue(SaleOrder saleOrder, BigDecimal amount);

  BigDecimal getScaledValue(SaleOrderLine saleOrderLine, BigDecimal amount);

  BigDecimal getCompanyScaledValue(SaleOrderLine saleOrderLine, BigDecimal amount);

  int getScale(SaleOrder saleOrder);

  int getCompanyScale(SaleOrder saleOrder);

  int getScale(SaleOrderLine saleOrderLine);

  int getCompanyScale(SaleOrderLine saleOrderLine);

  int getScale(Currency currency);

  int getCompanyScale(Company company);
}
