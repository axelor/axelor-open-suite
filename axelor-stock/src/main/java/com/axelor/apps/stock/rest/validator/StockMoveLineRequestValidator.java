package com.axelor.apps.stock.rest.validator;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.rest.dto.StockMoveLinePostRequest;
import javax.ws.rs.BadRequestException;

public class StockMoveLineRequestValidator {

  protected final boolean isLocationOnStockMoveLine;

  public StockMoveLineRequestValidator(boolean isLocationOnStockMoveLine) {
    this.isLocationOnStockMoveLine = isLocationOnStockMoveLine;
  }

  public void validate(StockMoveLinePostRequest stockMoveLinePostRequest, StockMove stockMove) {

    if (stockMove != null && isLocationOnStockMoveLine) {
      Integer typeSelect = stockMove.getTypeSelect();
      Long toStockLocationId = stockMoveLinePostRequest.getToStockLocationId();
      Long fromStockLocationId = stockMoveLinePostRequest.getFromStockLocationId();

      if (typeSelect.equals(StockMoveRepository.TYPE_INCOMING) && toStockLocationId == null) {
        throw new BadRequestException(
            String.format(
                StockExceptionMessage.REST_STOCK_MOVE_LINE_STOCK_LOCATION_REQUIRED,
                "toStockLocationId"));
      }

      if (typeSelect.equals(StockMoveRepository.TYPE_OUTGOING) && fromStockLocationId == null) {
        throw new BadRequestException(
            String.format(
                StockExceptionMessage.REST_STOCK_MOVE_LINE_STOCK_LOCATION_REQUIRED,
                "fromStockLocationId"));
      }
      if (typeSelect.equals(StockMoveRepository.TYPE_INTERNAL)
          && (toStockLocationId == null || fromStockLocationId == null)) {
        throw new BadRequestException(
            String.format(
                StockExceptionMessage.REST_STOCK_MOVE_LINE_STOCK_LOCATION_REQUIRED,
                "fromStockLocationId",
                "toStockLocationId"));
      }
    }
  }
}
