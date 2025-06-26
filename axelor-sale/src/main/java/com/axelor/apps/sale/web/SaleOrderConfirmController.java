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
package com.axelor.apps.sale.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderSplitDummyService;
import com.axelor.apps.sale.service.saleorder.SaleOrderSplitService;
import com.axelor.apps.sale.service.saleorder.views.SaleOrderDummyService;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderConfirmController {

  public void showPopUpConfirmWizard(ActionRequest request, ActionResponse response)
      throws AxelorException {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
    SaleOrderSplitService saleOrderSplitService = Beans.get(SaleOrderSplitService.class);
    saleOrderSplitService.checkSolOrderedQty(saleOrder);
    response.setView(
        ActionView.define(I18n.get("Confirm"))
            .model(SaleOrder.class.getName())
            .add("form", "sale-order-confirm-wizard-form")
            .param("popup", "reload")
            .param("show-toolbar", "false")
            .param("show-confirm", "false")
            .param("popup-save", "false")
            .param("forceEdit", "true")
            .context("_showRecord", String.valueOf(saleOrder.getId()))
            .map());
  }

  public void generateConfirmedSaleOrder(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    SaleOrder saleOrder = context.asType(SaleOrder.class);
    SaleOrderSplitService saleOrderSplitService = Beans.get(SaleOrderSplitService.class);

    List<Map<String, Object>> saleOrderLineListContext;
    Map<Long, BigDecimal> qtyToOrderMap = new HashMap<>();
    saleOrderLineListContext =
        (List<Map<String, Object>>) request.getRawContext().get("saleOrderLineList");
    fillMaps(saleOrderLineListContext, qtyToOrderMap);

    saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());

    SaleOrder confirmedSaleOrder =
        saleOrderSplitService.generateConfirmedSaleOrder(saleOrder, qtyToOrderMap);

    if (confirmedSaleOrder != null) {
      response.setCanClose(true);
      response.setView(
          ActionView.define(I18n.get("Sale order generated"))
              .model(SaleOrder.class.getName())
              .add("form", "sale-order-form")
              .add("grid", "sale-order-grid")
              .context("_showRecord", String.valueOf(confirmedSaleOrder.getId()))
              .map());
    }
  }

  private void fillMaps(
      List<Map<String, Object>> saleOrderLineListContext, Map<Long, BigDecimal> qtyToOrderMap) {
    for (Map<String, Object> map : saleOrderLineListContext) {
      if (map.get("qtyToOrder") != null) {
        BigDecimal qtyToOrderItem = new BigDecimal(map.get("qtyToOrder").toString());
        Long soLineId = Long.valueOf((Integer) map.get("solId"));
        qtyToOrderMap.put(soLineId, qtyToOrderItem);
      }
    }
  }

  public void onLoad(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    SaleOrder saleOrder = context.asType(SaleOrder.class);
    SaleOrderDummyService saleOrderDummyService = Beans.get(SaleOrderDummyService.class);
    saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
    response.setValues(saleOrderDummyService.getOnLoadSplitDummies(saleOrder));
    response.setValue(
        "$saleOrderLineList",
        Beans.get(SaleOrderSplitDummyService.class).getSaleOrderLineMapList(saleOrder));
  }

  public void orderAll(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    List<Map<String, Object>> saleOrderLineListContext = new ArrayList<>();
    SaleOrder saleOrder = context.asType(SaleOrder.class);
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    SaleOrderSplitService saleOrderSplitService = Beans.get(SaleOrderSplitService.class);
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      Map<String, Object> map = Mapper.toMap(saleOrderLine);
      map.put("$qtyToOrder", saleOrderSplitService.getQtyToOrderLeft(saleOrderLine));
      saleOrderLineListContext.add(map);
    }
    response.setValue("$saleOrderLineList", saleOrderLineListContext);
  }

  public void computeCurrentlyTotalOrdered(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    SaleOrder saleOrder = context.asType(SaleOrder.class);
    List<Map<String, Object>> saleOrderLineListContext;
    saleOrderLineListContext =
        (List<Map<String, Object>>) request.getRawContext().get("saleOrderLineList");

    saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
    response.setValue(
        "$currentlyTotalOrdered",
        computeCurrentlyTotalOrdered(saleOrder, saleOrderLineListContext));
  }

  protected BigDecimal computeCurrentlyTotalOrdered(
      SaleOrder saleOrder, List<Map<String, Object>> saleOrderLineListContext) {
    BigDecimal currentlyTotalOrdered = BigDecimal.ZERO;
    if (CollectionUtils.isEmpty(saleOrderLineListContext)) {
      return currentlyTotalOrdered;
    }
    for (Map<String, Object> lineContext : saleOrderLineListContext) {
      if (lineContext.get("qtyToOrder") == null) {
        continue;
      }
      SaleOrderLine saleOrderLine =
          Beans.get(SaleOrderLineRepository.class)
              .find(Long.parseLong(lineContext.get("id").toString()));
      BigDecimal price =
          saleOrder.getInAti() ? saleOrderLine.getInTaxPrice() : saleOrderLine.getPrice();
      BigDecimal qtyToOrder =
          BigDecimal.valueOf(Double.parseDouble(lineContext.get("qtyToOrder").toString()));
      currentlyTotalOrdered = currentlyTotalOrdered.add(qtyToOrder.multiply(price));
    }
    return currentlyTotalOrdered;
  }
}
