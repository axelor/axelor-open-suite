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

import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.supplychain.service.ProjectedStockService;
import com.axelor.apps.supplychain.service.StockLocationLineReservationService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Map;

public class StockLocationLineController {

  /**
   * Called from stock location line form view, on allocateAll button click. Call {@link
   * StockLocationLineReservationService#allocateAll(StockLocationLine)}
   *
   * @param request
   * @param response
   */
  public void allocateAll(ActionRequest request, ActionResponse response) {
    try {
      StockLocationLine stockLocationLine = request.getContext().asType(StockLocationLine.class);
      stockLocationLine =
          Beans.get(StockLocationLineRepository.class).find(stockLocationLine.getId());
      Beans.get(StockLocationLineReservationService.class).allocateAll(stockLocationLine);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from stock location line form view, on deallocateAll button click. Call {@link
   * StockLocationLineReservationService#deallocateAll(StockLocationLine)}
   *
   * @param request
   * @param response
   */
  public void deallocateAll(ActionRequest request, ActionResponse response) {
    try {
      StockLocationLine stockLocationLine = request.getContext().asType(StockLocationLine.class);
      stockLocationLine =
          Beans.get(StockLocationLineRepository.class).find(stockLocationLine.getId());
      Beans.get(StockLocationLineReservationService.class).deallocateAll(stockLocationLine);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from projected stock form view.
   *
   * @param request
   * @param response
   */
  public void searchStockLocationLineByProduct(ActionRequest request, ActionResponse response) {
    Map<String, Long> mapId =
        Beans.get(ProjectedStockService.class)
            .getProductIdCompanyIdStockLocationIdFromContext(request.getContext());
    if (mapId == null) {
      return;
    }
    Long productId = mapId.get("productId");
    Long companyId = mapId.get("companyId");
    Long stockLocationId = mapId.get("stockLocationId");
    String domain =
        Beans.get(StockLocationLineService.class)
            .getStockLocationLineListForAProduct(productId, companyId, stockLocationId);
    ActionViewBuilder actionViewBuilder =
        ActionView.define(I18n.get("Stock location lines by product"));
    actionViewBuilder.model(StockLocationLine.class.getName());
    actionViewBuilder.add("grid", "supplychain-stock-location-line-grid");
    actionViewBuilder.add("form", "stock-location-line-form");
    actionViewBuilder.domain(domain);

    response.setView(actionViewBuilder.map());
  }
}
