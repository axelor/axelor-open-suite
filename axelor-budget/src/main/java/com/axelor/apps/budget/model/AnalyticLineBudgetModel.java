package com.axelor.apps.budget.model;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.model.AnalyticLineModel;

public class AnalyticLineBudgetModel extends AnalyticLineModel {

  public AnalyticLineBudgetModel(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    super(saleOrderLine, saleOrder);

    this.account = saleOrderLine.getAccount();
  }

  public AnalyticLineBudgetModel(PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) {
    super(purchaseOrderLine, purchaseOrder);

    this.account = purchaseOrderLine.getAccount();
  }
}
