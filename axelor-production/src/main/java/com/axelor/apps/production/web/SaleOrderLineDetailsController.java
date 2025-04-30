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
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.service.SaleOrderLineDetailsPriceService;
import com.axelor.apps.production.service.SaleOrderLineDetailsService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.ContextHelper;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineDetailsController {

  public void productOnChange(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    SaleOrderLineDetails saleOrderLineDetails = context.asType(SaleOrderLineDetails.class);
    SaleOrderLineDetailsService saleOrderLineDetailsService =
        Beans.get(SaleOrderLineDetailsService.class);
    SaleOrder saleOrder = getSaleOrder(context);
    response.setValues(
        saleOrderLineDetailsService.productOnChange(saleOrderLineDetails, saleOrder));
  }

  public void priceOnChange(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    SaleOrderLineDetails saleOrderLineDetails = context.asType(SaleOrderLineDetails.class);
    SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService =
        Beans.get(SaleOrderLineDetailsPriceService.class);
    SaleOrder saleOrder = getSaleOrder(context);
    Map<String, Object> values = new HashMap<>();
    values.putAll(
        saleOrderLineDetailsPriceService.computeTotalPrice(saleOrderLineDetails, saleOrder));
    values.putAll(saleOrderLineDetailsPriceService.computeMarginCoef(saleOrderLineDetails));
    response.setValues(values);
  }

  public void computePrices(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    SaleOrderLineDetails saleOrderLineDetails = context.asType(SaleOrderLineDetails.class);
    SaleOrder saleOrder = getSaleOrder(context);
    response.setValues(
        Beans.get(SaleOrderLineDetailsPriceService.class)
            .computePrices(saleOrderLineDetails, saleOrder));
  }

  public void marginCoefOnChange(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    SaleOrderLineDetails saleOrderLineDetails = context.asType(SaleOrderLineDetails.class);
    SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService =
        Beans.get(SaleOrderLineDetailsPriceService.class);
    SaleOrder saleOrder = getSaleOrder(context);
    Map<String, Object> values = new HashMap<>();
    values.putAll(saleOrderLineDetailsPriceService.computePrice(saleOrderLineDetails));
    values.putAll(
        saleOrderLineDetailsPriceService.computeTotalPrice(saleOrderLineDetails, saleOrder));
    response.setValues(values);
  }

  public void qtyOnChange(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    SaleOrderLineDetails saleOrderLineDetails = context.asType(SaleOrderLineDetails.class);
    SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService =
        Beans.get(SaleOrderLineDetailsPriceService.class);
    SaleOrder saleOrder = getSaleOrder(context);
    Map<String, Object> values = new HashMap<>();
    values.putAll(
        saleOrderLineDetailsPriceService.computeTotalPrice(saleOrderLineDetails, saleOrder));
    response.setValues(values);
  }

  protected SaleOrder getSaleOrder(Context context) {
    SaleOrder saleOrder = ContextHelper.getOriginParent(context, SaleOrder.class);
    if (saleOrder == null) {
      saleOrder =
          SaleOrderLineUtils.getParentSol(context.getParent().asType(SaleOrderLine.class))
              .getSaleOrder();
    }
    return saleOrder;
  }
}
