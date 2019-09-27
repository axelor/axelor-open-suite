/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.web;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.ManufOrderStockMoveService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.Optional;

public class StockMoveLineController {

  public void compute(ActionRequest request, ActionResponse response) throws AxelorException {
    StockMoveLine stockMoveLine = request.getContext().asType(StockMoveLine.class);
    Optional<StockMove> stockMove = Optional.ofNullable(stockMoveLine.getStockMove());

    if (!stockMove.isPresent()) {
      Context parentContext = request.getContext().getParent();
      if (parentContext.getContextClass().equals(StockMove.class)) {
        stockMove = Optional.ofNullable(parentContext.asType(StockMove.class));
      } else if (parentContext.getContextClass().equals(ManufOrder.class)) {
        ManufOrder manufOrder = parentContext.asType(ManufOrder.class);
        ManufOrderStockMoveService manufOrderStockMoveService =
            Beans.get(ManufOrderStockMoveService.class);
        stockMove = manufOrderStockMoveService.getPlannedStockMove(manufOrder.getInStockMoveList());
      }

      if (!stockMove.isPresent()) {
        return;
      }
    }

    stockMoveLine = Beans.get(StockMoveLineService.class).compute(stockMoveLine, stockMove.get());
    response.setValue("valuatedUnitPrice", stockMoveLine.getValuatedUnitPrice());
  }

  /**
   * Called from stock move line form. Fill product info using the company either from the stock
   * move line, from the parent stock move or the parent manuf order.
   *
   * @param request
   * @param response
   */
  public void setProductInfo(ActionRequest request, ActionResponse response) {
    StockMoveLine stockMoveLine;
    try {
      stockMoveLine = request.getContext().asType(StockMoveLine.class);
      Company company;
      StockMove stockMove = stockMoveLine.getStockMove();
      if (stockMove == null) {
        Context parentContext = request.getContext().getParent();
        if (parentContext.getContextClass().equals(StockMove.class)) {
          stockMove = parentContext.asType(StockMove.class);
          company = stockMove.getCompany();
        } else if (parentContext.getContextClass().equals(ManufOrder.class)) {
          ManufOrder manufOrder = parentContext.asType(ManufOrder.class);
          company = manufOrder.getCompany();
        } else if (parentContext.getContextClass().equals(OperationOrder.class)) {
          OperationOrder operationOrder = parentContext.asType(OperationOrder.class);
          if (operationOrder.getManufOrder() == null) {
            return;
          }
          company = operationOrder.getManufOrder().getCompany();
        } else {
          throw new AxelorException(
              TraceBackRepository.TYPE_TECHNICAL,
              IExceptionMessage.STOCK_MOVE_LINE_UNKNOWN_PARENT_CONTEXT);
        }
      } else {
        company = stockMove.getCompany();
      }

      Beans.get(StockMoveLineService.class).setProductInfo(stockMove, stockMoveLine, company);
      response.setValues(stockMoveLine);
    } catch (Exception e) {
      stockMoveLine = new StockMoveLine();
      response.setValues(Mapper.toMap(stockMoveLine));
      TraceBackService.trace(response, e);
    }
  }
}
