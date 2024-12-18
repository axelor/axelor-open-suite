package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;

public interface PurchaseOrderLineWarningService {
  boolean checkLineIssue(PurchaseOrderLine purchaseOrderLine) throws AxelorException;

  boolean checkSupplierCatalogUnit(PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder)
      throws AxelorException;
}
