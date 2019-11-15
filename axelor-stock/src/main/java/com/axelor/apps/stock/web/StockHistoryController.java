package com.axelor.apps.stock.web;

import com.axelor.apps.stock.db.StockHistoryLine;
import com.axelor.apps.stock.service.StockHistoryService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
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
      response.setValue("$stockHistoryLineList", stockHistoryLineList);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
