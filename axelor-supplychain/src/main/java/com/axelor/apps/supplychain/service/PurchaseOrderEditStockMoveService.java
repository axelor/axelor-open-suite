package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;

public interface PurchaseOrderEditStockMoveService {
  void cancelStockMoves(PurchaseOrder purchaseOrder) throws AxelorException;
}
