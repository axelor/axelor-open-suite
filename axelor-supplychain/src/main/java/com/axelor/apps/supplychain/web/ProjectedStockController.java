/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectedStockController {

  public static final String VIEW_AVAILABLE_STOCK_QTY_TITLE = /*$$(*/ "%s stock location" /*)*/;
  public static final String VIEW_SOL_OF_PRODUCT_TITLE = /*$$(*/ "%s sale order" /*)*/;
  public static final String VIEW_POL_OF_PRODUCT_TITLE = /*$$(*/ "%s purchase order" /*)*/;
  public static final String VIEW_REQUESTED_RESERVED_QTY_TITLE = /*$$(*/
      "%s requested reserved" /*)*/;

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

    Product product = Beans.get(ProductRepository.class).find(mapId.get("productId"));
    String title = I18n.get(VIEW_AVAILABLE_STOCK_QTY_TITLE);
    title = String.format(title, product.getName());
    response.setView(
        ActionView.define(title)
            .model(StockLocationLine.class.getName())
            .add("grid", "stock-location-line-grid")
            .add("form", "stock-location-line-form")
            .domain(domain)
            .param("popup", "true")
            .param("popup-save", "false")
            .param("popup.maximized", "true")
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
    Product product = Beans.get(ProductRepository.class).find(mapId.get("productId"));
    String title = I18n.get(VIEW_SOL_OF_PRODUCT_TITLE);
    title = String.format(title, product.getName());
    response.setView(
        ActionView.define(title)
            .model(SaleOrderLine.class.getName())
            .add("grid", "sale-order-line-menu-grid")
            .add("form", "sale-order-line-all-form")
            .domain(domain)
            .param("popup", "true")
            .param("popup-save", "false")
            .param("popup.maximized", "true")
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
    Product product = Beans.get(ProductRepository.class).find(mapId.get("productId"));
    String title = I18n.get(VIEW_POL_OF_PRODUCT_TITLE);
    title = String.format(title, product.getName());
    response.setView(
        ActionView.define(title)
            .model(PurchaseOrderLine.class.getName())
            .add("grid", "purchase-order-line-menu-grid")
            .add("form", "purchase-order-line-all-form")
            .domain(domain)
            .param("popup", "true")
            .param("popup-save", "false")
            .param("popup.maximized", "true")
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
    Product product = Beans.get(ProductRepository.class).find(mapId.get("productId"));
    String title = I18n.get(VIEW_REQUESTED_RESERVED_QTY_TITLE);
    title = String.format(title, product.getName());
    response.setView(
        ActionView.define(title)
            .model(StockLocationLine.class.getName())
            .add("grid", "stock-location-line-grid")
            .add("form", "stock-location-line-form")
            .domain(domain)
            .param("popup", "true")
            .param("popup-save", "false")
            .param("popup.maximized", "true")
            .map());
  }

  public void showProjectedStock(ActionRequest request, ActionResponse response) {

    try {
      ProjectedStockService projectedStockService = Beans.get(ProjectedStockService.class);
      Map<String, Long> mapId =
          projectedStockService.getProductIdCompanyIdStockLocationIdFromContext(
              request.getContext());
      if (mapId == null || mapId.get("productId") == 0L) {
        return;
      }
      final List<MrpLine> mrpLineList = new ArrayList<>();
      try {
        mrpLineList.addAll(
            projectedStockService.createProjectedStock(
                mapId.get("productId"), mapId.get("companyId"), mapId.get("stockLocationId")));
        response.setView(
            ActionView.define(I18n.get("Projected stock"))
                .model(MrpLine.class.getName())
                .add("form", "projected-stock-form")
                .param("popup", "true")
                .param("popup-save", "false")
                .param("popup.maximized", "true")
                .context("_mrpLineList", mrpLineList)
                .map());
      } catch (Exception e) {
        TraceBackService.trace(response, e);
      } finally {
        projectedStockService.removeMrpAndMrpLine(mrpLineList);
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
        contextMrpLineList.stream()
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
