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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.service.ProductStockLocationService;
import com.axelor.apps.supplychain.service.ProjectedStockService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProductController {

  public void setIndicatorsOfProduct(ActionRequest request, ActionResponse response) {
    try {
      Map<String, Long> mapId =
          Beans.get(ProjectedStockService.class)
              .getProductIdCompanyIdStockLocationIdFromContext(request.getContext());
      if (mapId == null) {
        return;
      }
      Context context = request.getContext();
      Long productId = mapId.get("productId");
      Long companyId = mapId.get("companyId");
      Long stockLocationId = mapId.get("stockLocationId");
      if (companyId == 0L && stockLocationId != 0L) {
        stockLocationId = 0L;
      } else if (companyId != 0L && stockLocationId != 0L) {
        StockLocation sl = Beans.get(StockLocationRepository.class).find(stockLocationId);
        if (sl != null
            && sl.getCompany() != null
            && !Objects.equals(sl.getCompany().getId(), companyId)) {
          stockLocationId = 0L;
          response.setValue("stockLocation", null);
          context.put("$stockLocation", null);
        }
      }

      Map<String, Object> map =
          Beans.get(ProductStockLocationService.class)
              .computeIndicators(productId, companyId, stockLocationId);
      response.setValues(map);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.WARNING);
    }
  }

  @SuppressWarnings("unchecked")
  public void findAllSubLocation(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    Long stockLocationId = 0L;
    Long companyId = 0L;

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

    if (companyId != 0L && stockLocationId != 0L) {
      StockLocation stockLocation = Beans.get(StockLocationRepository.class).find(stockLocationId);
      if (stockLocation != null && Objects.equals(stockLocation.getCompany().getId(), companyId)) {
        List<Long> stockLocationIdList =
            Beans.get(StockLocationService.class)
                .getAllLocationAndSubLocationId(stockLocation, false);
        response.setValue("$stockLocationIdList", stockLocationIdList);
        return;
      }
    }
    response.setValue("$stockLocationIdList", Arrays.asList(0L));
  }
}
