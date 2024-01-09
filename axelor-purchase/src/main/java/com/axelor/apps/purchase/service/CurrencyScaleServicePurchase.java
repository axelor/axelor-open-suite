package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import java.math.BigDecimal;

public interface CurrencyScaleServicePurchase {

  BigDecimal getScaledValue(PurchaseOrder purchaseOrder, BigDecimal amount);

  BigDecimal getCompanyScaledValue(PurchaseOrder purchaseOrder, BigDecimal amount);

  BigDecimal getScaledValue(PurchaseOrderLine purchaseOrderLine, BigDecimal amount);

  BigDecimal getCompanyScaledValue(PurchaseOrderLine purchaseOrderLine, BigDecimal amount);

  int getScale(PurchaseOrder purchaseOrder);

  int getCompanyScale(PurchaseOrder purchaseOrder);

  int getScale(PurchaseOrderLine purchaseOrderLine);

  int getCompanyScale(PurchaseOrderLine purchaseOrderLine);

  int getScale(Currency currency);

  int getCompanyScale(Company company);
}
