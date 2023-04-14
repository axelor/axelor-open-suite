package com.axelor.apps.stock.rest.mapper;

import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.rest.dto.StockInternalMoveStockMoveLinePostRequest;
import java.util.List;
import java.util.stream.Collectors;

public class StockInternalMoveStockMoveLinePostRequestMapper {

  public static StockMoveLine map(
      StockInternalMoveStockMoveLinePostRequest stockMoveLinePostRequest) {

    StockMoveLine stockMoveLineResult = new StockMoveLine();

    stockMoveLineResult.setProduct(stockMoveLinePostRequest.fetchProduct());
    stockMoveLineResult.setUnit(stockMoveLinePostRequest.fetchUnit());
    stockMoveLineResult.setExpectedQty(stockMoveLinePostRequest.getRealQty());
    stockMoveLineResult.setRealQty(stockMoveLinePostRequest.getRealQty());
    stockMoveLineResult.setFromStockLocation(stockMoveLinePostRequest.fetchFromStockLocation());
    stockMoveLineResult.setToStockLocation(stockMoveLinePostRequest.fetchtoStockLocation());

    return stockMoveLineResult;
  }

  public static List<StockMoveLine> map(
      List<StockInternalMoveStockMoveLinePostRequest> stockMoveLinePostRequestList) {

    if (stockMoveLinePostRequestList != null) {
      return stockMoveLinePostRequestList.stream()
          .map(smlPr -> map(smlPr))
          .collect(Collectors.toList());
    }

    return List.of();
  }
}
