package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.db.FreightCarrierPricing;
import com.axelor.apps.supplychain.service.pricing.FreightCarrierApplyPricingService;
import com.axelor.apps.supplychain.service.pricing.FreightCarrierPricingService;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FreightCarrierPricingController {

  public void setFreightCarrierPricing(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    if (context.get("_shipmentMode") == null || context.get("_id") == null) {
      return;
    }
    Map shipmentModeMap = (Map<String, Object>) context.get("_shipmentMode");

    Set<FreightCarrierPricing> freightCarrierPricings =
        Beans.get(FreightCarrierPricingService.class)
            .getFreightCarrierPricingSet(
                Long.parseLong(shipmentModeMap.get("id").toString()),
                Long.valueOf(context.get("_id").toString()));
    response.setValue("$freightCarrierPricingsSet", freightCarrierPricings);
  }

  public void selectFreightCarrierPricing(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    try {
      List<FreightCarrierPricing> freightCarrierPricingList =
          ((List<Map<String, Object>>) context.get("freightCarrierPricingsSet"))
              .stream()
                  .map(o -> Mapper.toBean(FreightCarrierPricing.class, o))
                  .filter(FreightCarrierPricing::isSelected)
                  .collect(Collectors.toList());

      if (context.get("_id") != null) {
        String message =
            Beans.get(FreightCarrierPricingService.class)
                .computeFreightCarrierPricing(
                    freightCarrierPricingList, Long.valueOf(context.get("_id").toString()));
        if (message != null) {
          response.setInfo(message);
        }
        response.setCanClose(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computePricings(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();

      Set<FreightCarrierPricing> freightCarrierPricingsSet =
          (Set<FreightCarrierPricing>) context.get("freightCarrierPricingsSet");
      Beans.get(FreightCarrierApplyPricingService.class).applyPricing(freightCarrierPricingsSet);

      response.setValue("freightCarrierPricingsSet", freightCarrierPricingsSet);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
