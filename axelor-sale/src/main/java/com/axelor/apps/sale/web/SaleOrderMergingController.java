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

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingService.SaleOrderMergingResult;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingViewService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.MapHelper;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderMergingController {

  public void mergeSaleOrder(ActionRequest request, ActionResponse response) {

    String lineToMerge;
    Context context = request.getContext();
    if (context.get("saleQuotationToMerge") != null) {
      lineToMerge = "saleQuotationToMerge";
    } else {
      lineToMerge = "saleOrderToMerge";
    }
    try {
      List<SaleOrder> saleOrdersToMerge =
          MapHelper.getCollection(context, SaleOrder.class, lineToMerge);
      if (CollectionUtils.isNotEmpty(saleOrdersToMerge)) {
        SaleOrderMergingResult result =
            Beans.get(SaleOrderMergingService.class).mergeSaleOrders(saleOrdersToMerge);
        if (result.isConfirmationNeeded()) {
          ActionViewBuilder confirmView =
              Beans.get(SaleOrderMergingViewService.class)
                  .buildConfirmView(result, lineToMerge, saleOrdersToMerge);
          response.setView(confirmView.map());
          return;
        }
        if (result.getSaleOrder() != null) {
          // Open the generated sale order in a new tab
          response.setView(
              ActionView.define(I18n.get("Sale order"))
                  .model(SaleOrder.class.getName())
                  .add("grid", "sale-order-grid")
                  .add("form", "sale-order-form")
                  .param("search-filters", "sale-order-filters")
                  .param("forceEdit", "true")
                  .context("_showRecord", String.valueOf(result.getSaleOrder().getId()))
                  .map());
          response.setCanClose(true);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void mergeSaleOrderFromPopUp(ActionRequest request, ActionResponse response) {
    String lineToMerge;
    Context context = request.getContext();
    if (context.get("saleQuotationToMerge") != null) {
      lineToMerge = "saleQuotationToMerge";
    } else {
      lineToMerge = "saleOrderToMerge";
    }
    try {
      List<SaleOrder> saleOrdersToMerge =
          MapHelper.getCollection(context, SaleOrder.class, lineToMerge);
      if (CollectionUtils.isNotEmpty(saleOrdersToMerge)) {
        SaleOrderMergingResult result =
            Beans.get(SaleOrderMergingService.class)
                .mergeSaleOrdersWithContext(saleOrdersToMerge, context);
        if (result.getSaleOrder() != null) {
          // Open the generated sale order in a new tab
          response.setView(
              ActionView.define(I18n.get("Sale order"))
                  .model(SaleOrder.class.getName())
                  .add("grid", "sale-order-grid")
                  .add("form", "sale-order-form")
                  .param("search-filters", "sale-order-filters")
                  .param("forceEdit", "true")
                  .context("_showRecord", String.valueOf(result.getSaleOrder().getId()))
                  .map());
          // response.setCanClose(true);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void convertSelectedLinesToMergeLines(ActionRequest request, ActionResponse response) {
    try {
      @SuppressWarnings("unchecked")
      List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
      response.setValue(
          "$saleQuotationToMerge",
          Beans.get(SaleOrderMergingService.class).convertSelectedLinesToMergeLines(idList));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
