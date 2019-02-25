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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.service.StockMoveInvoiceService;
import com.axelor.apps.supplychain.service.StockMoveMultiInvoiceService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.db.JPA;
import com.axelor.exception.service.TraceBackService;
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
import java.util.Map.Entry;
import java.util.Optional;

@Singleton
public class StockMoveInvoiceController {

  @Inject private StockMoveInvoiceService stockMoveInvoiceService;
  @Inject private SaleOrderRepository saleRepo;
  @Inject private PurchaseOrderRepository purchaseRepo;

  public void generateInvoice(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      stockMove = Beans.get(StockMoveRepository.class).find(stockMove.getId());

      Invoice invoice = Beans.get(StockMoveInvoiceService.class).createInvoice(stockMove);

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
                .context("_operationTypeSelect", invoice.getOperationTypeSelect())
                .context("todayDate", Beans.get(AppSupplychainService.class).getTodayDate())
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from mass invoicing out stock move form view. Call method to check for missing fields.
   * If there are missing fields, show a wizard. Else call {@link
   * StockMoveMultiInvoiceService#createInvoiceFromMultiOutgoingStockMove(List)} and show the
   * generated invoice.
   *
   * @param request
   * @param response
   */
  public void generateInvoiceConcatOutStockMoveCheckMissingFields(
      ActionRequest request, ActionResponse response) {
    try {
      List<StockMove> stockMoveList = new ArrayList<>();
      List<Long> stockMoveIdList = new ArrayList<>();

      // No confirmation popup, stock Moves are content in a parameter list
      List<Map> stockMoveMap = (List<Map>) request.getContext().get("customerStockMoveToInvoice");
      for (Map map : stockMoveMap) {
        stockMoveIdList.add(Long.valueOf((Integer) map.get("id")));
      }
      for (Long stockMoveId : stockMoveIdList) {
        stockMoveList.add(JPA.em().find(StockMove.class, stockMoveId));
      }

      Map<String, Object> mapResult =
          Beans.get(StockMoveMultiInvoiceService.class)
              .areFieldsConflictedToGenerateCustInvoice(stockMoveList);
      boolean paymentConditionToCheck =
          (Boolean) mapResult.getOrDefault("paymentConditionToCheck", false);
      boolean paymentModeToCheck = (Boolean) mapResult.getOrDefault("paymentModeToCheck", false);
      boolean contactPartnerToCheck =
          (Boolean) mapResult.getOrDefault("contactPartnerToCheck", false);

      StockMove stockMove = stockMoveList.get(0);
      Partner partner = stockMove.getPartner();
      if (paymentConditionToCheck || paymentModeToCheck || contactPartnerToCheck) {
        ActionViewBuilder confirmView =
            ActionView.define("StockMove")
                .model(StockMove.class.getName())
                .add("form", "stock-move-supplychain-concat-cust-invoice-confirm-form")
                .param("popup", "true")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .param("popup-save", "false")
                .param("forceEdit", "true");

        if (paymentConditionToCheck) {
          confirmView.context("contextPaymentConditionToCheck", "true");
        } else {
          confirmView.context("paymentCondition", mapResult.get("paymentCondition"));
        }

        if (paymentModeToCheck) {
          confirmView.context("contextPaymentModeToCheck", "true");
        } else {
          confirmView.context("paymentMode", mapResult.get("paymentMode"));
        }
        if (contactPartnerToCheck) {
          confirmView.context("contextContactPartnerToCheck", "true");
          confirmView.context("contextPartnerId", partner.getId().toString());
        } else {
          confirmView.context("contactPartner", mapResult.get("contactPartner"));
        }
        confirmView.context("customerStockMoveToInvoice", Joiner.on(",").join(stockMoveIdList));
        response.setView(confirmView.map());
      } else {
        Optional<Invoice> invoice =
            Beans.get(StockMoveMultiInvoiceService.class)
                .createInvoiceFromMultiOutgoingStockMove(stockMoveList);
        invoice.ifPresent(
            inv ->
                response.setView(
                    ActionView.define("Invoice")
                        .model(Invoice.class.getName())
                        .add("grid", "invoice-grid")
                        .add("form", "invoice-form")
                        .param("forceEdit", "true")
                        .context("_operationTypeSelect", inv.getOperationTypeSelect())
                        .context("todayDate", Beans.get(AppSupplychainService.class).getTodayDate())
                        .context("_showRecord", String.valueOf(inv.getId()))
                        .map()));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from mass invoicing in stock move confirm view. Get parameters entered by the user, then
   * call {@link StockMoveMultiInvoiceService#createInvoiceFromMultiOutgoingStockMove(List,
   * PaymentCondition, PaymentMode, Partner)} and show the generated invoice.
   *
   * @param request
   * @param response
   */
  public void generateInvoiceConcatOutStockMove(ActionRequest request, ActionResponse response) {
    try {
      List<StockMove> stockMoveList = new ArrayList<>();

      String stockMoveListStr = (String) request.getContext().get("customerStockMoveToInvoice");

      for (String stockMoveId : stockMoveListStr.split(",")) {
        stockMoveList.add(JPA.em().find(StockMove.class, new Long(stockMoveId)));
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
                    Long.valueOf(
                        (Integer) ((Map) request.getContext().get("paymentCondition")).get("id")));
      }
      // paymentMode, only for customer stockMove
      if (request.getContext().get("paymentMode") != null) {
        paymentMode =
            JPA.em()
                .find(
                    PaymentMode.class,
                    Long.valueOf(
                        (Integer) ((Map) request.getContext().get("paymentMode")).get("id")));
      }
      if (request.getContext().get("contactPartner") != null) {
        contactPartner =
            JPA.em()
                .find(
                    Partner.class,
                    Long.valueOf(
                        (Integer) ((Map) request.getContext().get("contactPartner")).get("id")));
      }
      Optional<Invoice> invoice =
          Beans.get(StockMoveMultiInvoiceService.class)
              .createInvoiceFromMultiOutgoingStockMove(
                  stockMoveList, paymentCondition, paymentMode, contactPartner);
      invoice.ifPresent(
          inv ->
              response.setView(
                  ActionView.define("Invoice")
                      .model(Invoice.class.getName())
                      .add("grid", "invoice-grid")
                      .add("form", "invoice-form")
                      .param("forceEdit", "true")
                      .context("_showRecord", String.valueOf(inv.getId()))
                      .context("_operationTypeSelect", inv.getOperationTypeSelect())
                      .context("todayDate", Beans.get(AppSupplychainService.class).getTodayDate())
                      .map()));
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from mass invoicing out stock move form view. Call method to check for missing fields.
   * If there are missing fields, show a wizard. Else call {@link
   * StockMoveMultiInvoiceService#createInvoiceFromMultiOutgoingStockMove(List)} and show the
   * generated invoice.
   *
   * @param request
   * @param response
   */
  public void generateInvoiceConcatInStockMoveCheckMissingFields(
      ActionRequest request, ActionResponse response) {
    try {
      List<StockMove> stockMoveList = new ArrayList<>();
      List<Long> stockMoveIdList = new ArrayList<>();

      List<Map> stockMoveMap = (List<Map>) request.getContext().get("supplierStockMoveToInvoice");
      for (Map map : stockMoveMap) {
        stockMoveIdList.add(Long.valueOf((Integer) map.get("id")));
      }
      for (Long stockMoveId : stockMoveIdList) {
        stockMoveList.add(JPA.em().find(StockMove.class, stockMoveId));
      }
      Map<String, Object> mapResult =
          Beans.get(StockMoveMultiInvoiceService.class)
              .areFieldsConflictedToGenerateSupplierInvoice(stockMoveList);
      boolean paymentConditionToCheck =
          (Boolean) mapResult.getOrDefault("paymentConditionToCheck", false);
      boolean paymentModeToCheck = (Boolean) mapResult.getOrDefault("paymentModeToCheck", false);
      boolean contactPartnerToCheck =
          (Boolean) mapResult.getOrDefault("contactPartnerToCheck", false);

      Partner partner = stockMoveList.get(0).getPartner();
      if (paymentConditionToCheck || paymentModeToCheck || contactPartnerToCheck) {
        ActionViewBuilder confirmView =
            ActionView.define("StockMove")
                .model(StockMove.class.getName())
                .add("form", "stock-move-supplychain-concat-suppl-invoice-confirm-form")
                .param("popup", "true")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .param("popup-save", "false")
                .param("forceEdit", "true");

        if (paymentConditionToCheck) {
          confirmView.context("contextPaymentConditionToCheck", "true");
        } else {
          confirmView.context("paymentCondition", mapResult.get("paymentCondition"));
        }

        if (paymentModeToCheck) {
          confirmView.context("contextPaymentModeToCheck", "true");
        } else {
          confirmView.context("paymentMode", mapResult.get("paymentMode"));
        }
        if (contactPartnerToCheck) {
          confirmView.context("contextContactPartnerToCheck", "true");
          confirmView.context("contextPartnerId", partner.getId().toString());
        } else {
          confirmView.context("contactPartner", mapResult.get("contactPartner"));
        }

        confirmView.context("supplierStockMoveToInvoice", Joiner.on(",").join(stockMoveIdList));
        response.setView(confirmView.map());
      } else {
        Optional<Invoice> invoice =
            Beans.get(StockMoveMultiInvoiceService.class)
                .createInvoiceFromMultiIncomingStockMove(stockMoveList);
        invoice.ifPresent(
            inv ->
                response.setView(
                    ActionView.define("Invoice")
                        .model(Invoice.class.getName())
                        .add("grid", "invoice-grid")
                        .add("form", "invoice-form")
                        .param("forceEdit", "true")
                        .context("_showRecord", String.valueOf(inv.getId()))
                        .context("_operationTypeSelect", inv.getOperationTypeSelect())
                        .context("todayDate", Beans.get(AppSupplychainService.class).getTodayDate())
                        .map()));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from mass invoicing in stock move confirm view. Get parameters entered by the user, then
   * call {@link StockMoveMultiInvoiceService#createInvoiceFromMultiIncomingStockMove(List,
   * PaymentCondition, PaymentMode, Partner)} and show the generated invoice.
   *
   * @param request
   * @param response
   */
  public void generateInvoiceConcatInStockMove(ActionRequest request, ActionResponse response) {
    try {
      List<StockMove> stockMoveList = new ArrayList<>();

      String stockMoveListStr = (String) request.getContext().get("supplierStockMoveToInvoice");

      for (String stockMoveId : stockMoveListStr.split(",")) {
        stockMoveList.add(JPA.em().find(StockMove.class, new Long(stockMoveId)));
      }

      PaymentCondition paymentCondition = null;
      PaymentMode paymentMode = null;
      Partner contactPartner = null;
      if (request.getContext().get("paymentCondition") != null) {
        paymentCondition =
            JPA.em()
                .find(
                    PaymentCondition.class,
                    Long.valueOf(
                        (Integer) ((Map) request.getContext().get("paymentCondition")).get("id")));
      }
      if (request.getContext().get("paymentMode") != null) {
        paymentMode =
            JPA.em()
                .find(
                    PaymentMode.class,
                    Long.valueOf(
                        (Integer) ((Map) request.getContext().get("paymentMode")).get("id")));
      }
      if (request.getContext().get("contactPartner") != null) {
        contactPartner =
            JPA.em()
                .find(
                    Partner.class,
                    Long.valueOf(
                        (Integer) ((Map) request.getContext().get("contactPartner")).get("id")));
      }
      Optional<Invoice> invoice =
          Beans.get(StockMoveMultiInvoiceService.class)
              .createInvoiceFromMultiIncomingStockMove(
                  stockMoveList, paymentCondition, paymentMode, contactPartner);
      invoice.ifPresent(
          inv ->
              response.setView(
                  ActionView.define("Invoice")
                      .model(Invoice.class.getName())
                      .add("grid", "invoice-grid")
                      .add("form", "invoice-form")
                      .param("forceEdit", "true")
                      .context("_showRecord", String.valueOf(inv.getId()))
                      .context("_operationTypeSelect", inv.getOperationTypeSelect())
                      .context("todayDate", Beans.get(AppSupplychainService.class).getTodayDate())
                      .map()));
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public void generateMultiCustomerInvoice(ActionRequest request, ActionResponse response) {
    try {
      List<Map> stockMoveMap = (List<Map>) request.getContext().get("customerStockMoveToInvoice");

      List<Long> stockMoveIdList = new ArrayList<>();

      for (Map map : stockMoveMap) {
        stockMoveIdList.add(((Number) map.get("id")).longValue());
      }

      Entry<List<Long>, String> result =
          Beans.get(StockMoveMultiInvoiceService.class).generateMultipleInvoices(stockMoveIdList);
      List<Long> invoiceIdList = result.getKey();
      String warningMessage = result.getValue();
      if (!invoiceIdList.isEmpty()) {
        ActionViewBuilder viewBuilder;

        viewBuilder = ActionView.define("Cust. Invoices");

        viewBuilder
            .model(Invoice.class.getName())
            .add("grid", "invoice-grid")
            .add("form", "invoice-form")
            .domain("self.id IN (" + Joiner.on(",").join(invoiceIdList) + ")")
            .context("_operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)
            .context("todayDate", Beans.get(AppSupplychainService.class).getTodayDate());

        response.setView(viewBuilder.map());
      }
      if (warningMessage != null && !warningMessage.isEmpty()) {
        response.setFlash(warningMessage);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public void generateMultiSupplierInvoice(ActionRequest request, ActionResponse response) {
    try {
      List<Map> stockMoveMap = (List<Map>) request.getContext().get("supplierStockMoveToInvoice");

      List<Long> stockMoveIdList = new ArrayList<>();

      for (Map map : stockMoveMap) {
        stockMoveIdList.add(((Number) map.get("id")).longValue());
      }

      Entry<List<Long>, String> result =
          Beans.get(StockMoveMultiInvoiceService.class).generateMultipleInvoices(stockMoveIdList);
      List<Long> invoiceIdList = result.getKey();
      String warningMessage = result.getValue();
      if (!invoiceIdList.isEmpty()) {
        ActionViewBuilder viewBuilder;

        viewBuilder = ActionView.define("Suppl. Invoices");

        viewBuilder
            .model(Invoice.class.getName())
            .add("grid", "invoice-grid")
            .add("form", "invoice-form")
            .domain("self.id IN (" + Joiner.on(",").join(invoiceIdList) + ")")
            .context("_operationTypeSelect", InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)
            .context("todayDate", Beans.get(AppSupplychainService.class).getTodayDate());

        response.setView(viewBuilder.map());
      }
      if (warningMessage != null && !warningMessage.isEmpty()) {
        response.setFlash(warningMessage);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
