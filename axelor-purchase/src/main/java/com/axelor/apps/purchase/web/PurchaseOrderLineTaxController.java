package com.axelor.apps.purchase.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.tax.OrderLineTaxService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PurchaseOrderLineTaxController {

  @ErrorException
  public void recomputeAmounts(ActionRequest request, ActionResponse response)
      throws AxelorException {
    PurchaseOrderLineTax purchaseOrderLineTax =
        request.getContext().asType(PurchaseOrderLineTax.class);
    if (!Beans.get(OrderLineTaxService.class).isManageByAmount(purchaseOrderLineTax)) {
      return;
    }

    PurchaseOrder purchaseOrder = purchaseOrderLineTax.getPurchaseOrder();
    if (purchaseOrder == null) {
      purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);
    }

    response.setValue(
        "inTaxTotal",
        Beans.get(OrderLineTaxService.class)
            .computeInTaxTotal(purchaseOrderLineTax, purchaseOrder.getCurrency()));
  }
}
