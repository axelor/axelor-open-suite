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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.StockMoveInvoiceService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class StockMoveInvoiceController {

  @Inject private StockMoveInvoiceService stockMoveInvoiceService;

  public void generateInvoice(ActionRequest request, ActionResponse response) {

    StockMove stockMove = request.getContext().asType(StockMove.class);
    Invoice invoice = null;
    try {
      stockMove = Beans.get(StockMoveRepository.class).find(stockMove.getId());

      if (stockMove.getSaleOrder() != null) {
        invoice =
            stockMoveInvoiceService.createInvoiceFromSaleOrder(stockMove, stockMove.getSaleOrder());
      } else if (stockMove.getPurchaseOrder() != null) {
        invoice =
            stockMoveInvoiceService.createInvoiceFromPurchaseOrder(
                stockMove, stockMove.getPurchaseOrder());
      } else {
        invoice = stockMoveInvoiceService.createInvoiceFromStockMove(stockMove);
      }

      if (invoice != null) {
        // refresh stockMove context
        response.setReload(true);
        // Open the generated invoice in a new tab
        response.setView(
            ActionView.define("Invoice")
                .model(Invoice.class.getName())
                .add("grid", "invoice-grid")
                .add("form", "invoice-form")
                .param("forceEdit", "true")
                .context("_showRecord", String.valueOf(invoice.getId()))
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  // Generate only one invoice from several stock moves
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void generateInvoiceConcatStockMove(ActionRequest request, ActionResponse response) {
    List<StockMove> stockMoveList = new ArrayList<StockMove>();
    List<Long> stockMoveIdList = new ArrayList<Long>();

    boolean isCustomerStockMove = true;
    if (request.getContext().get("customerStockMoveToInvoice") != null) {
      if (request.getContext().get("customerStockMoveToInvoice") instanceof List) {
        // No confirmation popup, stock Moves are content in a parameter list
        List<Map> stockMoveMap = (List<Map>) request.getContext().get("customerStockMoveToInvoice");
        for (Map map : stockMoveMap) {
          stockMoveIdList.add(new Long((Integer) map.get("id")));
        }
      } else {
        // After confirmation popup, stock move's id are in a string separated by ","
        String stockMoveListStr = (String) request.getContext().get("customerStockMoveToInvoice");
        for (String stockMoveId : stockMoveListStr.split(",")) {
          stockMoveIdList.add(new Long(stockMoveId));
        }
      }
    } else {
      if (request.getContext().get("supplierStockMoveToInvoice") instanceof List) {
        // No confirmation popup, stock Moves are content in a parameter list
        List<Map> stockMoveMap = (List<Map>) request.getContext().get("supplierStockMoveToInvoice");
        for (Map map : stockMoveMap) {
          stockMoveIdList.add(new Long((Integer) map.get("id")));
        }
      } else {
        // After confirmation popup, stock move's id are in a string separated by ","
        String stockMoveListStr = (String) request.getContext().get("supplierStockMoveToInvoice");
        for (String stockMoveId : stockMoveListStr.split(",")) {
          stockMoveIdList.add(new Long(stockMoveId));
        }
      }
      isCustomerStockMove = false;
    }

    for (Long stockMoveId : stockMoveIdList) {
      stockMoveList.add(JPA.em().find(StockMove.class, stockMoveId));
    }

    // Check if paymentCondition, paymentMode or contactPartner are content in parameters
    PaymentCondition paymentCondition = null;
    PaymentMode paymentMode = null;
    Partner contactPartner = null;
    // paymentCondition, only for customer stockMove
    if (request.getContext().get("paymentCondition") != null) {
      paymentCondition =
          JPA.em()
              .find(
                  PaymentCondition.class,
                  new Long(
                      (Integer) ((Map) request.getContext().get("paymentCondition")).get("id")));
    }
    // paymentMode, only for customer stockMove
    if (request.getContext().get("paymentMode") != null) {
      paymentMode =
          JPA.em()
              .find(
                  PaymentMode.class,
                  new Long((Integer) ((Map) request.getContext().get("paymentMode")).get("id")));
    }
    if (request.getContext().get("contactPartner") != null) {
      contactPartner =
          JPA.em()
              .find(
                  Partner.class,
                  new Long((Integer) ((Map) request.getContext().get("contactPartner")).get("id")));
    }
    try {
      Map<String, Object> mapResult = null;

      if (isCustomerStockMove) {
        mapResult =
            stockMoveInvoiceService.createInvoiceFromMultiOutgoingStockMove(
                stockMoveList, paymentCondition, paymentMode, contactPartner);
      } else {
        mapResult =
            stockMoveInvoiceService.createInvoiceFromMultiIncomingStockMove(
                stockMoveList, contactPartner);
      }
      if (mapResult.get("invoiceId") != null) {
        // No need to display intermediate screen
        // Open the generated invoice in a new tab
        response.setView(
            ActionView.define("Invoice")
                .model(Invoice.class.getName())
                .add("grid", "invoice-grid")
                .add("form", "invoice-form")
                .param("forceEdit", "true")
                .context("_showRecord", String.valueOf(mapResult.get("invoiceId")))
                .map());
        response.setCanClose(true);
        if (mapResult.get("information") != null) {
          response.setFlash((String) mapResult.get("information"));
        }
      } else if (mapResult.get("information") != null) {
        response.setFlash(
            I18n.get(IExceptionMessage.STOCK_MOVE_NO_INVOICE_GENERATED)
                + ": <br/>"
                + (String) mapResult.get("information"));
      } else {
        // Need to display intermediate screen to select some values
        ActionViewBuilder confirmView =
            ActionView.define("StockMove")
                .model(StockMove.class.getName())
                .add("form", "stock-move-supplychain-concat-invoice-confirm-form")
                .param("popup", "true")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .param("popup-save", "false")
                .param("forceEdit", "true");

        // paymentCondition, only for customer stockMove
        if (mapResult.get("paymentConditionToCheck") != null) {
          confirmView.context("contextPaymentConditionToCheck", "true");
        }
        // paymentMode, only for customer stockMove
        if (mapResult.get("paymentModeToCheck") != null) {
          confirmView.context("contextPaymentModeToCheck", "true");
        }
        if (mapResult.get("contactPartnerToCheck") != null) {
          confirmView.context("contextContactPartnerToCheck", "true");
          confirmView.context("contextPartnerId", mapResult.get("partnerId").toString());
        }

        if (isCustomerStockMove) {
          confirmView.context("customerStockMoveToInvoice", Joiner.on(",").join(stockMoveIdList));
        } else {
          confirmView.context("supplierStockMoveToInvoice", Joiner.on(",").join(stockMoveIdList));
        }
        response.setView(confirmView.map());
      }
    } catch (Exception e) {
      response.setFlash(e.getLocalizedMessage());
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public void generateMultiInvoice(ActionRequest request, ActionResponse response) {
    List<Map> stockMoveMap = null;

    boolean isCustomerStockMove = true;
    if (request.getContext().get("customerStockMoveToInvoice") != null) {
      stockMoveMap = (List<Map>) request.getContext().get("customerStockMoveToInvoice");
    } else {
      stockMoveMap = (List<Map>) request.getContext().get("supplierStockMoveToInvoice");
      isCustomerStockMove = false;
    }

    StringBuilder stockMovesInError = new StringBuilder();
    List<Long> invoiceIdList = new ArrayList<Long>();
    Invoice invoice = null;
    StockMove stockMove = null;

    if (isCustomerStockMove) {
      for (Map map : stockMoveMap) {
        try {
          stockMove = JPA.em().find(StockMove.class, new Long((Integer) map.get("id")));
          if (stockMove.getSaleOrder() != null) {
            invoice =
                stockMoveInvoiceService.createInvoiceFromSaleOrder(
                    stockMove, stockMove.getSaleOrder());
            invoiceIdList.add(invoice.getId());
          }
        } catch (AxelorException ae) {
          if (stockMovesInError.length() > 0) {
            stockMovesInError.append("<br/>");
          }
          stockMovesInError.append(
              String.format(
                  I18n.get(IExceptionMessage.STOCK_MOVE_GENERATE_INVOICE),
                  stockMove.getName(),
                  ae.getLocalizedMessage()));
        } catch (Exception e) {
          TraceBackService.trace(e);
        } finally {
          if (invoiceIdList.size() % 10 == 0) {
            JPA.clear();
          }
        }
      }
    } else {
      for (Map map : stockMoveMap) {
        try {
          stockMove = JPA.em().find(StockMove.class, new Long((Integer) map.get("id")));
          if (stockMove.getPurchaseOrder() != null) {
            invoice =
                stockMoveInvoiceService.createInvoiceFromPurchaseOrder(
                    stockMove, stockMove.getPurchaseOrder());
            invoiceIdList.add(invoice.getId());
          }
        } catch (AxelorException ae) {
          if (stockMovesInError.length() > 0) {
            stockMovesInError.append("<br/>");
          }
          stockMovesInError.append(
              String.format(
                  I18n.get(IExceptionMessage.STOCK_MOVE_GENERATE_INVOICE),
                  stockMove.getName(),
                  ae.getLocalizedMessage()));
        } catch (Exception e) {
          TraceBackService.trace(e);
        } finally {
          if (invoiceIdList.size() % 10 == 0) {
            JPA.clear();
          }
        }
      }
    }

    if (!invoiceIdList.isEmpty()) {
      ActionViewBuilder viewBuilder = null;

      if (isCustomerStockMove) {
        viewBuilder = ActionView.define("Cust. Invoices");
      } else {
        viewBuilder = ActionView.define("Suppl. Invoices");
      }

      viewBuilder
          .model(Invoice.class.getName())
          .add("grid", "invoice-grid")
          .add("form", "invoice-form")
          .domain("self.id IN (" + Joiner.on(",").join(invoiceIdList) + ")");

      response.setView(viewBuilder.map());
    }

    if (stockMovesInError.length() > 0) {
      response.setFlash(stockMovesInError.toString());
    }
  }
}
