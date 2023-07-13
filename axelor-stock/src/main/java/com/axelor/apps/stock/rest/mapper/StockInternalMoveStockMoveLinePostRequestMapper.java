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
    stockMoveLineResult.setQty(stockMoveLinePostRequest.getRealQty());
    stockMoveLineResult.setRealQty(stockMoveLinePostRequest.getRealQty());
    stockMoveLineResult.setFromStockLocation(stockMoveLinePostRequest.fetchFromStockLocation());
    stockMoveLineResult.setToStockLocation(stockMoveLinePostRequest.fetchtoStockLocation());
    stockMoveLineResult.setTrackingNumber(stockMoveLinePostRequest.fetchTrackingNumber());

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
