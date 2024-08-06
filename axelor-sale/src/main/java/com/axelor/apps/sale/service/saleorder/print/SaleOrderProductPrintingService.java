package com.axelor.apps.sale.service.saleorder.print;

import com.axelor.apps.sale.db.SaleOrder;
import java.util.Map;

public interface SaleOrderProductPrintingService {
  Map<String, Object> getGroupProductsOnPrintings(SaleOrder saleOrder);
}
