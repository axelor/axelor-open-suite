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
package com.axelor.apps.production.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class ProductionOrderSaleOrderController {

  public void createProductionOrders(ActionRequest request, ActionResponse response)
      throws AxelorException {

    try {

      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());

      List<Long> productionOrderIdList =
          Beans.get(ProductionOrderSaleOrderService.class).generateProductionOrder(saleOrder);

      if (productionOrderIdList != null && productionOrderIdList.size() == 1) {
        response.setView(
            ActionView.define(I18n.get("Production order"))
                .model(ProductionOrder.class.getName())
                .add("form", "production-order-form")
                .add("grid", "production-order-grid")
                .param("search-filters", "production-order-filters")
                .param("forceEdit", "true")
                .context("_showRecord", String.valueOf(productionOrderIdList.get(0)))
                .map());
      } else if (productionOrderIdList != null && productionOrderIdList.size() > 1) {
        response.setView(
            ActionView.define(I18n.get("Production order"))
                .model(ProductionOrder.class.getName())
                .add("grid", "production-order-grid")
                .add("form", "production-order-form")
                .param("search-filters", "production-order-filters")
                .domain("self.id in (" + Joiner.on(",").join(productionOrderIdList) + ")")
                .map());
      } else {
        response.setInfo(I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_NO_GENERATION));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
