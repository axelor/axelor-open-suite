package com.axelor.apps.contract.web;

import com.axelor.apps.contract.service.pricing.ContractPricingService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PricingController {

  public void setPricingReadonly(ActionRequest request, ActionResponse response) {
    response.setValue(
        "$usedInContract",
        Beans.get(ContractPricingService.class).isReadonly(request.getContext()));
  }
}
