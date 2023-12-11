/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.pricing.PricingGenericService;
import com.axelor.apps.base.service.pricing.PricingGroupService;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.List;

public class PricingController {

  public void historizePricing(ActionRequest request, ActionResponse response) {
    try {
      Pricing pricing = request.getContext().asType(Pricing.class);
      pricing = Beans.get(PricingRepository.class).find(pricing.getId());
      Beans.get(PricingService.class).historizePricing(pricing);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void checkDates(ActionRequest request, ActionResponse response) {
    Pricing pricing = request.getContext().asType(Pricing.class);
    try {
      Beans.get(PricingService.class).checkDates(pricing);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void recoverPricing(ActionRequest request, ActionResponse response) {
    Pricing pricing = request.getContext().asType(Pricing.class);
    Boolean isHistorizeCurrentPricing =
        (Boolean) request.getContext().get("isHistorizeCurrentPricing");

    try {
      pricing = Beans.get(PricingService.class).recoverPricing(pricing, isHistorizeCurrentPricing);
      response.setCanClose(true);
      response.setSignal("refresh-tab", pricing);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setFormulaField(ActionRequest request, ActionResponse response) {
    Pricing pricing = request.getContext().asType(Pricing.class);
    String formula = "";

    if (pricing != null) {
      formula =
          Beans.get(PricingGroupService.class)
              .computeFormulaField(pricing.getProduct(), pricing.getProductCategory());
    }

    response.setValue("formula", formula);
  }

  public void clearFieldsRelatedToFormula(ActionRequest request, ActionResponse response) {
    Pricing pricing = request.getContext().asType(Pricing.class);

    response.setValues(Beans.get(PricingGroupService.class).clearFieldsRelatedToFormula(pricing));
  }

  public void usePricings(ActionRequest request, ActionResponse response) {

    try {
      String modelName = request.getModel();
      Context context = request.getContext();
      Long recordId = Long.valueOf(context.get("id").toString());
      Company company = (Company) request.getContext().get("company");
      if (modelName != null && recordId != null) {
        Beans.get(PricingGenericService.class)
            .usePricings(company, Class.forName(modelName), recordId);
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void usePricingsList(ActionRequest request, ActionResponse response) {

    try {
      List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
      if (ObjectUtils.isEmpty(idList)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.NO_RECORD_SELECTED_TO_PRINT));
      }

      String modelName = request.getModel();
      Company company = (Company) request.getContext().get("company");

      if (modelName != null) {
        Beans.get(PricingGenericService.class)
            .usePricings(company, Class.forName(modelName), idList);
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
