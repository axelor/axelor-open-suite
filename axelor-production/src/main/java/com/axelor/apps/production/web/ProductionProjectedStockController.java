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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.service.ProjectedStockService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.util.Map;

public class ProductionProjectedStockController {

  @Inject ProductRepository productRepository;
  @Inject ManufOrderService manufOrderService;

  public void showBuildingQuantityOfProduct(ActionRequest request, ActionResponse response) {
    Map<String, Long> mapId =
        Beans.get(ProjectedStockService.class)
            .getProductIdCompanyIdStockLocationIdFromContext(request.getContext());
    if (mapId == null || mapId.get("productId") == 0L) {
      return;
    }
    Long productId = mapId.get("productId");
    Long companyId = mapId.get("companyId");
    Long stockLocationId = mapId.get("stockLocationId");
    String domain =
        manufOrderService.getBuildingQtyForAProduct(productId, companyId, stockLocationId);
    Product product = productRepository.find(mapId.get("productId"));
    response.setView(
        ActionView.define(I18n.get(product.getCode() + " building"))
            .model(StockMoveLine.class.getName())
            .add("grid", "stock-move-line-produced-manuf-order-grid")
            .add("form", "stock-move-line-form")
            .domain(domain)
            .param("forceEdit", "true")
            .param("popup", "true")
            .map());
  }

  public void showConsumeQuantityOfProduct(ActionRequest request, ActionResponse response) {
    Map<String, Long> mapId =
        Beans.get(ProjectedStockService.class)
            .getProductIdCompanyIdStockLocationIdFromContext(request.getContext());
    if (mapId == null || mapId.get("productId") == 0L) {
      return;
    }
    Long productId = mapId.get("productId");
    Long companyId = mapId.get("companyId");
    Long stockLocationId = mapId.get("stockLocationId");
    String domain =
        manufOrderService.getConsumeAndMissingQtyForAProduct(productId, companyId, stockLocationId);

    Product product = productRepository.find(mapId.get("productId"));
    response.setView(
        ActionView.define(I18n.get(product.getCode() + " consume"))
            .model(StockMoveLine.class.getName())
            .add("grid", "stock-move-line-consumed-manuf-order-grid")
            .add("form", "stock-move-line-form")
            .domain(domain)
            .param("forceEdit", "true")
            .param("popup", "true")
            .map());
  }

  public void showMissingQuantityOfProduct(ActionRequest request, ActionResponse response) {
    Map<String, Long> mapId =
        Beans.get(ProjectedStockService.class)
            .getProductIdCompanyIdStockLocationIdFromContext(request.getContext());
    if (mapId == null || mapId.get("productId") == 0L) {
      return;
    }
    Long productId = mapId.get("productId");
    Long companyId = mapId.get("companyId");
    Long stockLocationId = mapId.get("stockLocationId");
    String domain =
        manufOrderService.getConsumeAndMissingQtyForAProduct(productId, companyId, stockLocationId);
    Product product = productRepository.find(mapId.get("productId"));
    response.setView(
        ActionView.define(I18n.get(product.getCode() + " missing"))
            .model(StockMoveLine.class.getName())
            .add("grid", "stock-move-line-consumed-manuf-order-grid")
            .add("form", "stock-move-line-form")
            .domain(domain)
            .param("forceEdit", "true")
            .param("popup", "true")
            .map());
  }
}
