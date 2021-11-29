/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.web;

import com.axelor.apps.stock.db.StockHistoryLine;
import com.axelor.apps.stock.service.StockHistoryService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class StockHistoryController {

  /**
   * Called from stock history form view, on new and on date change. Call {@link
   * StockHistoryService#computeStockHistoryLineList(Long, Long, Long, LocalDate, LocalDate)}
   *
   * @param request
   * @param response
   */
  public void fillStockHistoryLineList(ActionRequest request, ActionResponse response) {
    try {
      response.setValue("$stockHistoryLineList", getStockHistoryLineList(request));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from stock history form view, on click on the export button. Call {@link
   * StockHistoryService#exportStockHistoryLineList(List<StockHistoryLine>)}
   *
   * @param request
   * @param response
   */
  public void exportStockHistoryLineList(ActionRequest request, ActionResponse response) {
    try {
      List<StockHistoryLine> stockHistoryLineList = getStockHistoryLineList(request);
      MetaFile csv =
          Beans.get(StockHistoryService.class).exportStockHistoryLineList(stockHistoryLineList);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }

    try {
      List<StockHistoryLine> stockHistoryLineList = getStockHistoryLineList(request);
      String fileName = Beans.get(StockHistoryService.class).getStockHistoryLineExportName();
      MetaFile csv =
          Beans.get(StockHistoryService.class).exportStockHistoryLineList(stockHistoryLineList);

      response.setView(
          ActionView.define(fileName)
              .add(
                  "html",
                  "ws/rest/com.axelor.meta.db.MetaFile/"
                      + csv.getId()
                      + "/content/download?v="
                      + csv.getVersion())
              .map());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  private List<StockHistoryLine> getStockHistoryLineList(ActionRequest request)
      throws AxelorException {
    Context context = request.getContext();
    Long productId = null;
    if (context.get("product") != null) {
      productId = Long.parseLong(((LinkedHashMap) context.get("product")).get("id").toString());
    }
    Long companyId = null;
    if (context.get("company") != null) {
      companyId = Long.parseLong(((LinkedHashMap) context.get("company")).get("id").toString());
    }
    Long stockLocationId = null;
    if (context.get("stockLocation") != null) {
      stockLocationId =
          Long.parseLong(((LinkedHashMap) context.get("stockLocation")).get("id").toString());
    }
    Object beginDateContext = context.get("beginDate");
    LocalDate beginDate = null;
    if (beginDateContext != null) {
      beginDate = LocalDate.parse(beginDateContext.toString());
    }

    Object endDateContext = context.get("endDate");
    LocalDate endDate = null;
    if (endDateContext != null) {
      endDate = LocalDate.parse(endDateContext.toString());
    }

    List<StockHistoryLine> stockHistoryLineList = new ArrayList<>();
    if (productId != null
        && companyId != null
        && stockLocationId != null
        && beginDate != null
        && endDate != null) {
      stockHistoryLineList =
          Beans.get(StockHistoryService.class)
              .computeStockHistoryLineList(
                  productId, companyId, stockLocationId, beginDate, endDate);
    }
    return stockHistoryLineList;
  }
}
