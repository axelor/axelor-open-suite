/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.SaleOrderReservedQtyService;
import com.axelor.apps.supplychain.service.SaleOrderStockService;
import com.axelor.apps.supplychain.service.StockMoveServiceSupplychain;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Optional;

public class StockMoveController {

  public void addSubLines(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      response.setValue(
          "stockMoveLineList",
          Beans.get(StockMoveServiceSupplychain.class)
              .addSubLines(stockMove.getStockMoveLineList()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
      response.setReload(true);
    }
  }

  public void removeSubLines(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      response.setValue(
          "stockMoveLineList",
          Beans.get(StockMoveServiceSupplychain.class)
              .removeSubLines(stockMove.getStockMoveLineList()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
      response.setReload(true);
    }
  }

  public void verifyProductStock(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      if (stockMove.getPickingIsEdited() && !stockMove.getAvailabilityRequest()) {
        response.setValue("availabilityRequest", true);
        response.setFlash(
            I18n.get(IExceptionMessage.STOCK_MOVE_AVAILABILITY_REQUEST_NOT_UPDATABLE));
        return;
      }
      Beans.get(StockMoveServiceSupplychain.class).verifyProductStock(stockMove);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
      response.setValue("availabilityRequest", false);
    }
  }

  /**
   * Called from stock move form view, on available qty boolean change. Only called if the user
   * accepted to allocate everything. Call {@link SaleOrderStockService#findSaleOrder(StockMove)} to
   * fetch related sale order then {@link SaleOrderReservedQtyService#allocateAll(SaleOrder)} to
   * allocate everything in sale order.
   *
   * @param request
   * @param response
   */
  public void allocateAll(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      Company company = stockMove.getCompany();
      if (company == null) {
        return;
      }

      SupplyChainConfig supplyChainConfig =
          Beans.get(SupplyChainConfigService.class).getSupplyChainConfig(company);
      if (!Beans.get(AppSupplychainService.class).getAppSupplychain().getManageStockReservation()
          || !stockMove.getAvailabilityRequest()
          || !supplyChainConfig.getAutoAllocateOnAvailabilityRequest()) {
        return;
      }
      Optional<SaleOrder> saleOrderOpt =
          Beans.get(SaleOrderStockService.class).findSaleOrder(stockMove);
      if (saleOrderOpt.isPresent()) {
        Beans.get(SaleOrderReservedQtyService.class).allocateAll(saleOrderOpt.get());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
      response.setReload(true);
    }
  }
}
