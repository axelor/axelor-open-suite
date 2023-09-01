/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
