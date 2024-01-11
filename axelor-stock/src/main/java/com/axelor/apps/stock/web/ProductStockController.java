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
package com.axelor.apps.stock.web;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.WeightedAveragePriceService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Singleton
public class ProductStockController {

  public void setStockPerDay(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    Long productId = Long.parseLong(context.get("id").toString());
    Long locationId = Long.parseLong(context.get("locationId").toString());
    LocalDate fromDate = LocalDate.parse(context.get("stockFromDate").toString());
    LocalDate toDate = LocalDate.parse(context.get("stockToDate").toString());

    List<Map<String, Object>> stocks =
        Beans.get(StockMoveService.class).getStockPerDate(locationId, productId, fromDate, toDate);
    response.setValue("$stockPerDayList", stocks);
  }

  public void displayStockMoveLine(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    if (context.get("date") != null && context.getParent().get("locationId") != null) {
      LocalDate stockDate = LocalDate.parse(context.get("date").toString());
      Long locationId = Long.parseLong(context.getParent().get("locationId").toString());

      if (request.getContext().getParent().get("id") != null)
        response.setView(
            ActionView.define(I18n.get("Stock Move Lines"))
                .model(StockMoveLine.class.getName())
                .add("grid", "stock-move-line-all-grid")
                .add("form", "stock-move-line-all-form")
                .param("search-filters", "stock-move-line-filters")
                .domain(
                    "self.product.id = :id AND (self.stockMove.fromStockLocation.id = :locationId OR self.stockMove.toStockLocation.id = :locationId) AND self.stockMove.statusSelect != :status AND (self.stockMove.estimatedDate <= :stockDate OR self.stockMove.realDate <= :stockDate)")
                .context("id", request.getContext().getParent().get("id"))
                .context("locationId", locationId)
                .context("status", StockMoveRepository.STATUS_CANCELED)
                .context("stockDate", stockDate)
                .map());
      response.setCanClose(true);
    }
  }

  public void updateStockLocation(ActionRequest request, ActionResponse response) {
    try {
      Product product = request.getContext().asType(Product.class);
      StockLocationLineService stockLocationLineService = Beans.get(StockLocationLineService.class);
      if (product.getId() == null) {
        return;
      }
      product = Beans.get(ProductRepository.class).find(product.getId());
      List<StockLocationLine> stockLocationLineList =
          stockLocationLineService.getStockLocationLines(product);

      for (StockLocationLine stockLocationLine : stockLocationLineList) {
        stockLocationLineService.updateStockLocationFromProduct(stockLocationLine, product);
      }
      Beans.get(WeightedAveragePriceService.class).computeAvgPriceForProduct(product);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
