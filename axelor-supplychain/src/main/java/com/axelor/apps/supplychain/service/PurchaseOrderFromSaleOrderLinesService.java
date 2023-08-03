package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;
import java.util.Map;

public interface PurchaseOrderFromSaleOrderLinesService {
  Map<String, Object> generatePurchaseOrdersFromSOLines(
      SaleOrder saleOrder,
      List<SaleOrderLine> saleOrderLines,
      Partner supplierPartner,
      String saleOrderLinesIdStr)
      throws AxelorException;

  Boolean isDirectOrderLocation(SaleOrder saleOrder);
}
