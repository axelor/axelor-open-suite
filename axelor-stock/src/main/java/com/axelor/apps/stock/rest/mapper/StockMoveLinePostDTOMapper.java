package com.axelor.apps.stock.rest.mapper;

import com.axelor.apps.stock.rest.dto.StockMoveLinePostDTO;
import com.axelor.apps.stock.rest.dto.StockMoveLinePostRequest;
import java.util.List;
import java.util.stream.Collectors;

public class StockMoveLinePostDTOMapper {

  public static StockMoveLinePostDTO map(StockMoveLinePostRequest stockMoveLinePostRequest) {

    StockMoveLinePostDTO stockMoveLineResult = new StockMoveLinePostDTO();
    stockMoveLineResult.setProduct(stockMoveLinePostRequest.fetchProduct());
    stockMoveLineResult.setConformity(stockMoveLinePostRequest.getConformity());
    stockMoveLineResult.setUnit(stockMoveLinePostRequest.fetchUnit());
    stockMoveLineResult.setTrackingNumber(stockMoveLinePostRequest.fetchTrackingNumber());
    stockMoveLineResult.setExpectedQty(stockMoveLinePostRequest.getExpectedQty());
    stockMoveLineResult.setRealQty(stockMoveLinePostRequest.getRealQty());
    stockMoveLineResult.setFromStockLocation(stockMoveLinePostRequest.fetchFromStockLocation());
    stockMoveLineResult.setToStockLocation(stockMoveLinePostRequest.fetchtoStockLocation());

    return stockMoveLineResult;
  }

  public static List<StockMoveLinePostDTO> map(
      List<StockMoveLinePostRequest> stockMoveLinePostRequestList) {

    if (stockMoveLinePostRequestList != null) {
      return stockMoveLinePostRequestList.stream()
          .map(smlPr -> map(smlPr))
          .collect(Collectors.toList());
    }

    return List.of();
  }
}
