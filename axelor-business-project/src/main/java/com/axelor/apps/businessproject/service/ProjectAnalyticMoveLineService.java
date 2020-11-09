package com.axelor.apps.businessproject.service;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;

public interface ProjectAnalyticMoveLineService {

  PurchaseOrder updateLines(PurchaseOrder purchaseOrder);

  SaleOrder updateLines(SaleOrder saleOrder);
}
