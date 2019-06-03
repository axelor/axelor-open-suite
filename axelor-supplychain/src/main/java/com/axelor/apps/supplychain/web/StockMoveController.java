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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.db.AppSupplychain;
import com.axelor.apps.base.db.repo.AppSupplychainRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.StockMoveServiceSupplychain;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.StringJoiner;

public class StockMoveController {

  @Inject private StockMoveServiceSupplychain stockMoveService;

  @Inject private AppSupplychainRepository appSupplychainRepo;

  public void addSubLines(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      response.setValue(
          "stockMoveLineList", stockMoveService.addSubLines(stockMove.getStockMoveLineList()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
      response.setReload(true);
    }
  }

  public void removeSubLines(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      response.setValue(
          "stockMoveLineList", stockMoveService.removeSubLines(stockMove.getStockMoveLineList()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
      response.setReload(true);
    }
  }

  public void verifyProductStock(ActionRequest request, ActionResponse response) {
    StockMove stockMove = request.getContext().asType(StockMove.class);
    AppSupplychain appSupplychain = appSupplychainRepo.all().fetchOne();
    StringJoiner notAvailableProducts = new StringJoiner(",");
    if (stockMove.getAvailabilityRequest()
        && stockMove.getStockMoveLineList() != null
        && appSupplychain.getIsVerifyProductStock()
        && stockMove.getFromStockLocation() != null) {
      try {
        int counter = 1;
        for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
          SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
          if (saleOrderLine != null) {
            BigDecimal availableStock =
                stockMoveService.getAvailableStock(stockMove, stockMoveLine);
            if (availableStock.compareTo(
                        saleOrderLine.getQty().subtract(saleOrderLine.getReservedQty()))
                    < 0
                && counter <= 10) {
              notAvailableProducts.add(stockMoveLine.getProduct().getFullName());
              counter++;
            }
          }
        }
        if (!Strings.isNullOrEmpty(notAvailableProducts.toString())) {
          response.setValue("availabilityRequest", false);
          response.setFlash(
              String.format(
                  I18n.get(IExceptionMessage.STOCK_MOVE_VERIFY_PRODUCT_STOCK_ERROR),
                  notAvailableProducts.toString()));
        }
      } catch (AxelorException e) {
        TraceBackService.trace(response, e);
      }
    }
  }
}
