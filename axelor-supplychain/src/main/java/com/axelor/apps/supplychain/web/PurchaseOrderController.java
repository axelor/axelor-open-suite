/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.PurchaseOrderShipmentService;
import com.axelor.apps.supplychain.service.PurchaseOrderStockService;
import com.axelor.apps.supplychain.service.PurchaseOrderStockServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderSupplychainService;
import com.axelor.apps.supplychain.service.analytic.AnalyticToolSupplychainService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class PurchaseOrderController {

  public void createStockMove(ActionRequest request, ActionResponse response)
      throws AxelorException {

    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

    try {
      if (purchaseOrder.getId() != null) {

        List<Long> stockMoveList =
            Beans.get(PurchaseOrderStockServiceImpl.class)
                .createStockMoveFromPurchaseOrder(
                    Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId()));

        if (stockMoveList != null && stockMoveList.size() == 1) {
          response.setView(
              ActionView.define(I18n.get("Stock move"))
                  .model(StockMove.class.getName())
                  .add("grid", "stock-move-grid")
                  .add("form", "stock-move-form")
                  .param("search-filters", "internal-stock-move-filters")
                  .param("forceEdit", "true")
                  .domain("self.id = " + stockMoveList.get(0))
                  .context("_showRecord", String.valueOf(stockMoveList.get(0)))
                  .map());
        } else if (stockMoveList != null && stockMoveList.size() > 1) {
          response.setView(
              ActionView.define(I18n.get("Stock move"))
                  .model(StockMove.class.getName())
                  .add("grid", "stock-move-grid")
                  .add("form", "stock-move-form")
                  .param("search-filters", "internal-stock-move-filters")
                  .domain("self.id in (" + Joiner.on(",").join(stockMoveList) + ")")
                  .map());
        } else {
          response.setInfo(
              I18n.get(SupplychainExceptionMessage.PO_NO_DELIVERY_STOCK_MOVE_TO_GENERATE));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getStockLocation(ActionRequest request, ActionResponse response) {
    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
    try {
      Company company = purchaseOrder.getCompany();
      StockLocation stockLocation =
          Beans.get(PurchaseOrderSupplychainService.class)
              .getStockLocation(purchaseOrder.getSupplierPartner(), company);
      response.setValue("stockLocation", stockLocation);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelReceipt(ActionRequest request, ActionResponse response) throws AxelorException {

    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
    purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());
    Beans.get(PurchaseOrderStockServiceImpl.class).cancelReceipt(purchaseOrder);
  }

  public void updateAmountToBeSpreadOverTheTimetable(
      ActionRequest request, ActionResponse response) {
    try {

      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      Beans.get(PurchaseOrderSupplychainService.class)
          .updateAmountToBeSpreadOverTheTimetable(purchaseOrder);
      response.setValue(
          "amountToBeSpreadOverTheTimetable", purchaseOrder.getAmountToBeSpreadOverTheTimetable());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateEstimatedReceiptDate(ActionRequest request, ActionResponse response) {
    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

    List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrder.getPurchaseOrderLineList();
    if (purchaseOrderLineList != null) {
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
        Integer receiptState = purchaseOrderLine.getReceiptState();
        if (receiptState != null
            && !receiptState.equals(PurchaseOrderLineRepository.RECEIPT_STATE_RECEIVED)
            && !receiptState.equals(PurchaseOrderLineRepository.RECEIPT_STATE_PARTIALLY_RECEIVED)) {
          purchaseOrderLine.setEstimatedReceiptDate(purchaseOrder.getEstimatedReceiptDate());
        }
      }
    }
    response.setValue("purchaseOrderLineList", purchaseOrderLineList);
  }

  /**
   * Called from purchase order form view when validating purchase order and analytic distribution
   * is required from company's purchase config.
   *
   * @param request
   * @param response
   */
  public void checkPurchaseOrderAnalyticDistributionTemplate(
      ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      Beans.get(AnalyticToolSupplychainService.class)
          .checkPurchaseOrderLinesAnalyticDistribution(purchaseOrder);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void backToValidatedStatus(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());
      Beans.get(PurchaseOrderSupplychainService.class).updateToValidatedStatus(purchaseOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createShipmentCostLine(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      ShipmentMode shipmentMode = purchaseOrder.getShipmentMode();
      String message =
          Beans.get(PurchaseOrderShipmentService.class)
              .createShipmentCostLine(purchaseOrder, shipmentMode);
      if (message != null) {
        response.setNotify(message);
      }
      response.setValues(purchaseOrder);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getFromStockLocation(ActionRequest request, ActionResponse response) {
    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
    try {
      Company company = purchaseOrder.getCompany();
      StockLocation fromStockLocation =
          Beans.get(PurchaseOrderSupplychainService.class)
              .getFromStockLocation(purchaseOrder.getSupplierPartner(), company);
      response.setValue("fromStockLocation", fromStockLocation);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updatePurchaseOrderLinesStockLocation(
      ActionRequest request, ActionResponse response) {
    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
    List<PurchaseOrderLine> purchaseOrderLineList =
        Beans.get(PurchaseOrderStockService.class)
            .updatePurchaseOrderLinesStockLocation(purchaseOrder);
    response.setValue("purchaseOrderLineList", purchaseOrderLineList);
  }

  public void checkAnalyticAxis(ActionRequest request, ActionResponse response) {
    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
    try {
      Beans.get(PurchaseOrderSupplychainService.class).checkAnalyticAxisByCompany(purchaseOrder);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
