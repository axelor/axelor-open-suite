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
package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.StockMoveMergingService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import java.util.List;
import java.util.stream.Collectors;

public class StockMoveMergingController {

  @SuppressWarnings("unchecked")
  public void checkMergeValues(ActionRequest request, ActionResponse response) {
    try {
      List<Integer> ids = (List<Integer>) request.getContext().get("_ids");
      StockMoveMergingService stockMoveMergingService = Beans.get(StockMoveMergingService.class);
      if (ObjectUtils.isEmpty(ids) || ids.size() < 2) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(StockExceptionMessage.STOCK_MOVE_NO_LINE_SELECTED));
      }

      List<StockMove> stockMoveList = getStockMoveList(request);
      String errors = stockMoveMergingService.canMerge(stockMoveList);
      if (!Strings.isNullOrEmpty(errors)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(StockExceptionMessage.STOCK_MOVE_MERGE_ERROR),
            errors);
      }
      if (stockMoveMergingService.checkShipmentValues(stockMoveList)) {
        response.setAlert(I18n.get(StockExceptionMessage.STOCK_MOVE_DIFF_SHIPMENT_FIELDS));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void mergeStockMoves(ActionRequest request, ActionResponse response) {
    try {
      List<StockMove> stockMoveList = getStockMoveList(request);

      StockMove stockMove = Beans.get(StockMoveMergingService.class).mergeStockMoves(stockMoveList);
      if (stockMove != null) {
        response.setView(
            ActionView.define(I18n.get("Stock move"))
                .model(StockMove.class.getName())
                .add("form", "stock-move-form")
                .param("forceEdit", Boolean.TRUE.toString())
                .context("_showRecord", stockMove.getId())
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  protected List<StockMove> getStockMoveList(ActionRequest request) {
    List<Integer> ids = (List<Integer>) request.getContext().get("_ids");
    StockMoveRepository stockMoveRepository = Beans.get(StockMoveRepository.class);
    List<StockMove> stockMoveList =
        ids.stream().map(Long::valueOf).map(stockMoveRepository::find).collect(Collectors.toList());
    return stockMoveList;
  }
}
