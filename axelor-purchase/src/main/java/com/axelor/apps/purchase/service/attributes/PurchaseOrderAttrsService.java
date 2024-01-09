package com.axelor.apps.purchase.service.attributes;

import com.axelor.apps.purchase.db.PurchaseOrder;
import java.util.Map;

public interface PurchaseOrderAttrsService {

  void setPurchaseOrderLineScale(
      PurchaseOrder purchaseOrder, Map<String, Map<String, Object>> attrsMap);

  void setPurchaseOrderLineTaxScale(
      PurchaseOrder purchaseOrder, Map<String, Map<String, Object>> attrsMap);

  Map<String, Map<String, Object>> onChangePurchaseOrderLine(PurchaseOrder purchaseOrder);
}
