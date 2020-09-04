package com.axelor.apps.production.service.print;

import com.axelor.apps.purchase.service.print.PurchaseOrderPrintServiceImpl;

public class PurchaseOrderPrintServiceProductionImpl extends PurchaseOrderPrintServiceImpl {

  @Override
  public String getPurchaseOrderLineQuerySelectClause() {

    return super.getPurchaseOrderLineQuerySelectClause()
        .concat(", Product.product_standard as product_standard");
  }
}
