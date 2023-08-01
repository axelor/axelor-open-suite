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
package com.axelor.apps.sale.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.crm.translation.ITranslation;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.saleorder.OpportunitySaleOrderService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class OpportunitySaleOrderController {

  public void generateSaleOrder(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Opportunity opportunity = request.getContext().asType(Opportunity.class);
    opportunity = Beans.get(OpportunityRepository.class).find(opportunity.getId());
    SaleOrder saleOrder =
        Beans.get(OpportunitySaleOrderService.class).createSaleOrderFromOpportunity(opportunity);
    response.setReload(true);
    response.setView(
        ActionView.define(I18n.get(ITranslation.SALE_QUOTATION))
            .model(SaleOrder.class.getName())
            .add("form", "sale-order-form")
            .param("forceEdit", "true")
            .param("forceTitle", "true")
            .context("_showRecord", String.valueOf(saleOrder.getId()))
            .map());
  }

  public void cancelSaleOrders(ActionRequest request, ActionResponse response) {
    try {
      Opportunity opportunity = request.getContext().asType(Opportunity.class);
      Beans.get(OpportunitySaleOrderService.class).cancelSaleOrders(opportunity);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
