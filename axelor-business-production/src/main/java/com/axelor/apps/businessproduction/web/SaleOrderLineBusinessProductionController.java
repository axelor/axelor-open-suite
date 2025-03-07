package com.axelor.apps.businessproduction.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.businessproduction.service.SaleOrderProductionSyncBusinessService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class SaleOrderLineBusinessProductionController {
  public void solDetailsListOnChange(ActionRequest request, ActionResponse response)
      throws AxelorException {
    SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);

    Beans.get(SaleOrderProductionSyncBusinessService.class).syncSaleOrderLine(saleOrderLine);
    response.setValues(saleOrderLine);
  }
}
