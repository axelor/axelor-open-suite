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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.supplychain.service.ProductStockLocationService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.MoreObjects;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProductController {

  @SuppressWarnings("unchecked")
  public void setIndicatorsOfProduct(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Long productId = 0L;
      Long companyId = 0L;
      Long stockLocationId = 0L;

      LinkedHashMap<String, Object> productHashMap =
          (LinkedHashMap<String, Object>) MoreObjects.firstNonNull(context.get("product"), context.get("$product"));
      if (productHashMap != null) {
        productId = Long.valueOf(productHashMap.get("id").toString());
      } else {
        return;
      }
      LinkedHashMap<String, Object> companyHashMap =
          (LinkedHashMap<String, Object>) context.get("company");
      if (companyHashMap != null) {
        companyId = Long.valueOf(companyHashMap.get("id").toString());
      }
      LinkedHashMap<String, Object> stockLocationHashMap =
          (LinkedHashMap<String, Object>) context.get("stockLocation");
      if (stockLocationHashMap != null) {
        stockLocationId = Long.valueOf(stockLocationHashMap.get("id").toString());
      }
      Map<String, Object> map =
          Beans.get(ProductStockLocationService.class)
              .computeIndicators(productId, companyId, stockLocationId);
      response.setValues(map);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
