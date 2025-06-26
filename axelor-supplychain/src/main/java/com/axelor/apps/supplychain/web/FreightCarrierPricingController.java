/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
          response.setNotify(message);
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
