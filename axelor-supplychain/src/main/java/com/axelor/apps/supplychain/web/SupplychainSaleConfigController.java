package com.axelor.apps.supplychain.web;

import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.supplychain.service.SupplychainSaleConfigService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class SupplychainSaleConfigController {
  public void updateCustomerCredit(ActionRequest request, ActionResponse response) {
    try {
      SaleConfig saleConfig = request.getContext().asType(SaleConfig.class);
      Beans.get(SupplychainSaleConfigService.class).updateCustomerCredit(saleConfig);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
