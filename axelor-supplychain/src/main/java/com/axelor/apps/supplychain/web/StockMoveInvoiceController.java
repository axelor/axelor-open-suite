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
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.StockMoveInvoiceService;
import com.axelor.apps.supplychain.service.StockMoveMultiInvoiceService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.apps.supplychain.translation.ITranslation;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

@Singleton
public class StockMoveInvoiceController {

  @SuppressWarnings("unchecked")
  public void generateInvoice(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      if (context.containsKey("operationSelect")) {
        Integer operationSelect = Integer.parseInt(context.get("operationSelect").toString());
        List<Map<String, Object>> stockMoveLineListContext = null;
        if (operationSelect == StockMoveRepository.INVOICE_PARTIALLY
            && context.containsKey("stockMoveLines")) {
          stockMoveLineListContext = (List<Map<String, Object>>) context.get("stockMoveLines");
        }
        StockMove stockMove =
            Beans.get(StockMoveRepository.class)
                .find(Long.parseLong(request.getContext().get("_id").toString()));
        stockMove = Beans.get(StockMoveRepository.class).find(stockMove.getId());
        Invoice invoice =
            Beans.get(StockMoveInvoiceService.class)
                .createInvoice(stockMove, operationSelect, stockMoveLineListContext);

        if (invoice != null) {
          // Open the generated invoice in a new tab
          response.setView(
              ActionView.define(I18n.get(ITranslation.INVOICE))
                  .model(Invoice.class.getName())
                  .add("grid", "invoice-grid")
                  .add("form", "invoice-form")
                  .param("search-filters", "customer-invoices-filters")
                  .param("forceEdit", "true")
                  .context("_showRecord", String.valueOf(invoice.getId()))
                  .context("_operationTypeSelect", invoice.getOperationTypeSelect())
                  .context(
                      "todayDate",
                      Beans.get(AppSupplychainService.class).getTodayDate(stockMove.getCompany()))
                  .map());
          response.setCanClose(true);
        } else {
          response.setError(I18n.get(IExceptionMessage.STOCK_MOVE_NO_LINES_TO_INVOICE));
        }
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
                        .param("search-filters", "customer-invoices-filters")
                        .param("forceEdit", "true")
                        .context("_operationTypeSelect", inv.getOperationTypeSelect())
                        .context(
                            "todayDate",
                            Beans.get(AppSupplychainService.class)
                                .getTodayDate(stockMove.getCompany()))
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
                      .param("search-filters", "customer-invoices-filters")
                      .param("forceEdit", "true")
                      .context("_showRecord", String.valueOf(inv.getId()))
                      .context("_operationTypeSelect", inv.getOperationTypeSelect())
                      .context(
                          "todayDate",
                          Beans.get(AppSupplychainService.class).getTodayDate(inv.getCompany()))
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
                        .param("search-filters", "customer-invoices-filters")
                        .param("forceEdit", "true")
                        .context("_showRecord", String.valueOf(inv.getId()))
                        .context("_operationTypeSelect", inv.getOperationTypeSelect())
                        .context(
                            "todayDate",
                            Beans.get(AppSupplychainService.class).getTodayDate(inv.getCompany()))
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
                      .param("search-filters", "customer-invoices-filters")
                      .param("forceEdit", "true")
                      .context("_showRecord", String.valueOf(inv.getId()))
                      .context("_operationTypeSelect", inv.getOperationTypeSelect())
                      .context(
                          "todayDate",
                          Beans.get(AppSupplychainService.class).getTodayDate(inv.getCompany()))
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
      List<StockMove> stockMoveList = new ArrayList<>();

      for (Map map : stockMoveMap) {
        stockMoveIdList.add(((Number) map.get("id")).longValue());
      }
      for (Long stockMoveId : stockMoveIdList) {
        stockMoveList.add(JPA.em().find(StockMove.class, stockMoveId));
      }
      Beans.get(StockMoveMultiInvoiceService.class).checkForAlreadyInvoicedStockMove(stockMoveList);

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
            .param("search-filters", "customer-invoices-filters")
            .domain("self.id IN (" + Joiner.on(",").join(invoiceIdList) + ")")
            .context("_operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)
            .context(
                "todayDate",
                Beans.get(AppSupplychainService.class)
                    .getTodayDate(AuthUtils.getUser().getActiveCompany()));

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
      List<StockMove> stockMoveList = new ArrayList<>();

      for (Map map : stockMoveMap) {
        stockMoveIdList.add(((Number) map.get("id")).longValue());
      }
      for (Long stockMoveId : stockMoveIdList) {
        stockMoveList.add(JPA.em().find(StockMove.class, stockMoveId));
      }
      Beans.get(StockMoveMultiInvoiceService.class).checkForAlreadyInvoicedStockMove(stockMoveList);

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
            .param("search-filters", "customer-invoices-filters")
            .domain("self.id IN (" + Joiner.on(",").join(invoiceIdList) + ")")
            .context("_operationTypeSelect", InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)
            .context(
                "todayDate",
                Beans.get(AppSupplychainService.class)
                    .getTodayDate(AuthUtils.getUser().getActiveCompany()));

        response.setView(viewBuilder.map());
      }
      if (warningMessage != null && !warningMessage.isEmpty()) {
        response.setFlash(warningMessage);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillDefaultValueWizard(ActionRequest request, ActionResponse response) {
    try {
      Long id = Long.parseLong(request.getContext().get("_id").toString());
      StockMove stockMove = Beans.get(StockMoveRepository.class).find(id);
      StockMoveInvoiceService stockMoveInvoiceService = Beans.get(StockMoveInvoiceService.class);

      BigDecimal totalInvoicedQty = stockMoveInvoiceService.computeNonCanceledInvoiceQty(stockMove);
      if (totalInvoicedQty.compareTo(BigDecimal.ZERO) == 0) {
        response.setValue("operationSelect", StockMoveRepository.INVOICE_ALL);
      } else {
        response.setValue("operationSelect", StockMoveRepository.INVOICE_PARTIALLY);
        response.setAttr("operationSelect", "selection-in", "[2]");
      }
      List<Map<String, Object>> stockMoveLines =
          Beans.get(StockMoveInvoiceService.class).getStockMoveLinesToInvoice(stockMove);
      response.setValue("$stockMoveLines", stockMoveLines);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void openInvoicingWizard(ActionRequest request, ActionResponse response) {
    try {
      response.setReload(true);
      StockMove stockMove = request.getContext().asType(StockMove.class);
      stockMove = Beans.get(StockMoveRepository.class).find(stockMove.getId());
      StockMoveInvoiceService stockMoveInvoiceService = Beans.get(StockMoveInvoiceService.class);
      List<Map<String, Object>> stockMoveLines =
          stockMoveInvoiceService.getStockMoveLinesToInvoice(stockMove);
      Company company = stockMove.getCompany();
      SupplyChainConfig supplyChainConfig =
          Beans.get(SupplyChainConfigService.class).getSupplyChainConfig(company);
      boolean isPartialInvoicingActivated =
          (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING
                  && supplyChainConfig.getActivateIncStockMovePartialInvoicing())
              || (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING
                  && supplyChainConfig.getActivateOutStockMovePartialInvoicing());

      if (isPartialInvoicingActivated && !stockMoveLines.isEmpty()) {
        // open wizard view for partial invoicing
        response.setView(
            ActionView.define(I18n.get(ITranslation.INVOICING))
                .model(StockMove.class.getName())
                .add("form", "stock-move-invoicing-wizard-form")
                .param("popup", "reload")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .param("width", "large")
                .param("popup-save", "false")
                .context("_id", stockMove.getId())
                .map());
      } else if (!stockMoveLines.isEmpty()) {
        // invoice everything if config is disabled.
        Invoice invoice =
            stockMoveInvoiceService.createInvoice(stockMove, StockMoveRepository.INVOICE_ALL, null);
        if (invoice != null) {
          response.setView(
              ActionView.define(I18n.get(ITranslation.INVOICE))
                  .model(Invoice.class.getName())
                  .add("grid", "invoice-grid")
                  .add("form", "invoice-form")
                  .param("search-filters", "customer-invoices-filters")
                  .param("forceEdit", "true")
                  .context("_showRecord", String.valueOf(invoice.getId()))
                  .context("_operationTypeSelect", invoice.getOperationTypeSelect())
                  .context(
                      "todayDate", Beans.get(AppSupplychainService.class).getTodayDate(company))
                  .map());
        }
      } else {
        response.setAlert(I18n.get(IExceptionMessage.STOCK_MOVE_INVOICE_ERROR));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
