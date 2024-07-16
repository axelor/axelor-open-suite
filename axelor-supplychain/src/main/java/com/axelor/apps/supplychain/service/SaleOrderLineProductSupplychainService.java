package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineProductSupplychainService {
  Map<String, Object> getProductionInformation(SaleOrderLine saleOrderLine);

  Map<String, Object> setSupplierPartnerDefault(SaleOrderLine saleOrderLine, SaleOrder saleOrder);
}
