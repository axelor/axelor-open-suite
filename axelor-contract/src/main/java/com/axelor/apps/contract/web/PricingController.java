package com.axelor.apps.contract.web;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.contract.service.pricing.ContractPricingService;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class PricingController {

  public void setPricingReadonly(ActionRequest request, ActionResponse response) {
    response.setValue(
        "$usedInContract",
        Beans.get(ContractPricingService.class).isReadonly(request.getContext()));
  }

  public void copyPricing(ActionRequest request, ActionResponse response) {
    Context parentContext = request.getContext().getParent();
    if (parentContext != null) {
      Pricing parentPricing = (Pricing) parentContext.get("pricing");
      if (parentPricing != null) {
        PricingRepository pricingRepository = Beans.get(PricingRepository.class);
        parentPricing = pricingRepository.find(parentPricing.getId());
        response.setValues(Mapper.toMap(pricingRepository.copy(parentPricing, true)));
        response.setValue("name", parentPricing.getName() + " - " + I18n.get("Copy"));
      }
    }
  }
}
