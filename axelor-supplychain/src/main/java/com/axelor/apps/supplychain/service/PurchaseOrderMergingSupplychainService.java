package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.stock.db.StockMove;

public interface PurchaseOrderMergingSupplychainService {
  PurchaseOrder getDummyMergedPurchaseOrder(StockMove stockMove) throws AxelorException;
}
