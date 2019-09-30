/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.web;

import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class ProductionOrderSaleOrderController {

  @Inject ProductionOrderSaleOrderService productionOrderSaleOrderService;
  @Inject SaleOrderRepository saleOrderRepository;

  public void createProductionOrders(ActionRequest request, ActionResponse response)
      throws AxelorException {

    try {

      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      saleOrder = saleOrderRepository.find(saleOrder.getId());

      List<Long> productionOrderIdList =
          productionOrderSaleOrderService.generateProductionOrder(saleOrder);

      if (productionOrderIdList != null && productionOrderIdList.size() == 1) {
        response.setView(
            ActionView.define(I18n.get("Production order"))
                .model(ProductionOrder.class.getName())
                .add("form", "production-order-form")
                .add("grid", "production-order-grid")
                .param("forceEdit", "true")
                .context("_showRecord", String.valueOf(productionOrderIdList.get(0)))
                .map());
      } else if (productionOrderIdList != null && productionOrderIdList.size() > 1) {
        response.setView(
            ActionView.define(I18n.get("Production order"))
                .model(ProductionOrder.class.getName())
                .add("grid", "production-order-grid")
                .add("form", "production-order-form")
                .domain("self.id in (" + Joiner.on(",").join(productionOrderIdList) + ")")
                .map());
      } else {
        response.setFlash(I18n.get(IExceptionMessage.PRODUCTION_ORDER_NO_GENERATION));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
