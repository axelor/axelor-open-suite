/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.contract.web;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.service.pricing.ContractPricingService;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class PricingController {

  public void setPricingReadonly(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    Pricing pricing = context.asType(Pricing.class);
    Context parent = context.getParent();
    ContractLine contractLine = null;
    if (parent != null && parent.getContextClass().equals(ContractLine.class)) {
      contractLine = parent.asType(ContractLine.class);
    }
    response.setValue(
        "$usedInContract",
        Beans.get(ContractPricingService.class).isReadonly(pricing, contractLine));
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
