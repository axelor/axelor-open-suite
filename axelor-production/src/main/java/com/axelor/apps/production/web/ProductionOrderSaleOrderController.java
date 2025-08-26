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
package com.axelor.apps.production.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.SaleOrderBlockingProductionService;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.common.StringUtils;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ProductionOrderSaleOrderController {

  public void checkBlocking(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

    if (Beans.get(SaleOrderBlockingProductionService.class).hasOnGoingBlocking(saleOrder)) {
      response.setAlert(I18n.get(ProductionExceptionMessage.SALE_ORDER_LINES_CANNOT_PRODUCT));
    }
  }

  public void createProductionOrders(ActionRequest request, ActionResponse response)
      throws AxelorException {

    try {

      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

      List<SaleOrderLine> selectedSaleOrderLines =
          saleOrder.getSaleOrderLineList().stream()
              .filter(Model::isSelected)
              .collect(Collectors.toList());
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());

      ProductionOrderSaleOrderService productionOrderSaleOrderService =
          Beans.get(ProductionOrderSaleOrderService.class);

      String infoMessage =
          productionOrderSaleOrderService.generateProductionOrder(
              saleOrder, selectedSaleOrderLines);

      List<Long> productionOrderIds =
          productionOrderSaleOrderService.getLinkedProductionOrders(saleOrder).stream()
              .map(ProductionOrder::getId)
              .collect(Collectors.toList());

      if (productionOrderIds != null && productionOrderIds.size() == 1) {
        response.setView(
            ActionView.define(I18n.get("Production order"))
                .model(ProductionOrder.class.getName())
                .add("form", "production-order-form")
                .add("grid", "production-order-grid")
                .param("search-filters", "production-order-filters")
                .param("forceEdit", "true")
                .context(
                    "_showRecord",
                    String.valueOf(productionOrderIds.stream().findFirst().orElse(0L)))
                .map());
      } else if (productionOrderIds != null && productionOrderIds.size() > 1) {
        response.setView(
            ActionView.define(I18n.get("Production order"))
                .model(ProductionOrder.class.getName())
                .add("grid", "production-order-grid")
                .add("form", "production-order-form")
                .param("search-filters", "production-order-filters")
                .domain("self.id in (" + Joiner.on(",").join(productionOrderIds) + ")")
                .map());
      } else if (!productionOrderSaleOrderService.areAllBlocked(selectedSaleOrderLines)) {
        response.setInfo(I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_NO_GENERATION));
      }

      if (StringUtils.notEmpty(infoMessage)) {
        response.setInfo(infoMessage);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
