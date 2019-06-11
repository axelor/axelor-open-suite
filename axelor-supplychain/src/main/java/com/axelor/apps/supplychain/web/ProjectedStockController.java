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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.repo.MrpLineRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.service.ProjectedStockService;
import com.axelor.apps.supplychain.service.PurchaseOrderStockService;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChain;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectedStockController {

  @Inject ProductRepository productRepository;

  public void showStockAvailableProduct(ActionRequest request, ActionResponse response) {
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
        Beans.get(StockLocationLineService.class)
            .getAvailableStockForAProduct(productId, companyId, stockLocationId);

    Product product = productRepository.find(mapId.get("productId"));
    response.setView(
        ActionView.define(I18n.get(product.getCode() + " stock location"))
            .model(StockLocationLine.class.getName())
            .add("grid", "stock-location-line-grid")
            .add("form", "stock-location-line-form")
            .domain(domain)
            .param("forceEdit", "true")
            .param("popup", "true")
            .map());
  }

  public void showSaleOrderOfProduct(ActionRequest request, ActionResponse response) {
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
        Beans.get(SaleOrderLineServiceSupplyChain.class)
            .getSaleOrderLineListForAProduct(productId, companyId, stockLocationId);
    Product product = productRepository.find(mapId.get("productId"));
    response.setView(
        ActionView.define(I18n.get(product.getCode() + " sale order"))
            .model(SaleOrderLine.class.getName())
            .add("grid", "sale-order-line-menu-grid")
            .add("form", "sale-order-line-all-form")
            .domain(domain)
            .param("forceEdit", "true")
            .param("popup", "true")
            .map());
  }

  public void showPurchaseOrderOfProduct(ActionRequest request, ActionResponse response) {
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
        Beans.get(PurchaseOrderStockService.class)
            .getPurchaseOrderLineListForAProduct(productId, companyId, stockLocationId);
    Product product = productRepository.find(mapId.get("productId"));
    response.setView(
        ActionView.define(I18n.get(product.getCode() + " purchase order"))
            .model(PurchaseOrderLine.class.getName())
            .add("grid", "purchase-order-line-menu-grid")
            .add("form", "purchase-order-line-all-form")
            .domain(domain)
            .param("forceEdit", "true")
            .param("popup", "true")
            .map());
  }

  public void showStockRequestedReservedQuantityOfProduct(
      ActionRequest request, ActionResponse response) {
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
        Beans.get(StockLocationLineService.class)
            .getRequestedReservedQtyForAProduct(productId, companyId, stockLocationId);
    Product product = productRepository.find(mapId.get("productId"));
    response.setView(
        ActionView.define(I18n.get(product.getCode() + " requested reserved"))
            .model(StockLocationLine.class.getName())
            .add("grid", "stock-location-line-grid")
            .add("form", "stock-location-line-form")
            .domain(domain)
            .param("forceEdit", "true")
            .param("popup", "true")
            .map());
  }

  public void showProjectedStock(ActionRequest request, ActionResponse response) {

    try {
      Map<String, Long> mapId =
          Beans.get(ProjectedStockService.class)
              .getProductIdCompanyIdStockLocationIdFromContext(request.getContext());
      if (mapId == null || mapId.get("productId") == 0L) {
        return;
      }
      List<MrpLine> mrpLineList = new ArrayList<>();
      try {
        mrpLineList =
            Beans.get(ProjectedStockService.class)
                .createProjectedStock(
                    mapId.get("productId"), mapId.get("companyId"), mapId.get("stockLocationId"));
        response.setView(
            ActionView.define(I18n.get("Projected stock"))
                .model(MrpLine.class.getName())
                .add("form", "projected-stock-form")
                .param("forceEdit", "true")
                .param("popup", "true")
                .context("_mrpLineList", mrpLineList)
                .map());
      } catch (Exception e) {
        TraceBackService.trace(response, e);
      } finally {
        if (mrpLineList != null && !mrpLineList.isEmpty()) {
          Long mrpId = mrpLineList.get(0).getId();
          Beans.get(MrpRepository.class).all().filter("self.id = ?1", mrpId).remove();
          Beans.get(MrpLineRepository.class).all().filter("self.mrp.id = ?1", mrpId).remove();
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showChartProjectedStock(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    List<Map<String, Object>> dataList = new ArrayList<>();

    @SuppressWarnings("unchecked")
    Collection<Map<String, Object>> contextMrpLineList =
        (Collection<Map<String, Object>>) context.get("_mrpLineListToProject");

    List<MrpLine> mrpLineList =
        contextMrpLineList
            .stream()
            .map(map -> Mapper.toBean(MrpLine.class, map))
            .collect(Collectors.toList());

    if (!mrpLineList.isEmpty()) {
      List<MrpLine> mrpLineLastList = new ArrayList<>();
      MrpLine lastMrpLine = mrpLineList.get(0);

      for (int i = 1; i < mrpLineList.size(); ++i) {
        MrpLine mrpLine = mrpLineList.get(i);
        if (mrpLine.getMaturityDate().isAfter(lastMrpLine.getMaturityDate())) {
          mrpLineLastList.add(lastMrpLine);
        }
        lastMrpLine = mrpLine;
      }
      mrpLineLastList.add(lastMrpLine);
      lastMrpLine = mrpLineList.get(0);
      LocalDate mrpDate = lastMrpLine.getMaturityDate();
      for (MrpLine mrpLine : mrpLineLastList) {
        mrpDate = addInterElementForProjectedStockChart(dataList, lastMrpLine, mrpDate, mrpLine);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", mrpLine.getMaturityDate());
        dataMap.put("cumulativeQty", mrpLine.getCumulativeQty());
        dataList.add(dataMap);
        lastMrpLine = mrpLine;
      }
    }
    response.setData(dataList);
  }

  private LocalDate addInterElementForProjectedStockChart(
      List<Map<String, Object>> dataList, MrpLine lastMrpLine, LocalDate mrpDate, MrpLine mrpLine) {
    while (mrpDate.isBefore(mrpLine.getMaturityDate())) {
      mrpDate = mrpDate.plusDays(1);
      Map<String, Object> dataMapDate = new HashMap<>();
      dataMapDate.put("name", mrpDate);
      dataMapDate.put("cumulativeQty", lastMrpLine.getCumulativeQty());
      dataList.add(dataMapDate);
    }
    return mrpDate;
  }
}
