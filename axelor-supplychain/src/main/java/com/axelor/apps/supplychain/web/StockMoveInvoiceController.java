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
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.StockMoveInvoiceService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class StockMoveInvoiceController {

  @Inject private StockMoveInvoiceService stockMoveInvoiceService;
  @Inject private SaleOrderRepository saleRepo;
  @Inject private PurchaseOrderRepository purchaseRepo;
  @Inject private StockMoveLineRepository stockMoveLineRepository;

  public void generateInvoice(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    if (context.containsKey("operationSelect")) {
      StockMove stockMove =
          Beans.get(StockMoveRepository.class)
              .find(Long.parseLong(request.getContext().get("_id").toString()));
      Map<Long, BigDecimal> qtyToInvoiceMap = new HashMap<>();
      int operationSelect = Integer.parseInt(context.get("operationSelect").toString());

      if (operationSelect == StockMoveRepository.INVOICE_PARTILLY) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stockMoveLineListContext =
            (List<Map<String, Object>>) context.get("stockMoveLines");
        for (Map<String, Object> map : stockMoveLineListContext) {
          if (map.get("qtyToInvoice") != null) {
            BigDecimal qtyToInvoiceItem = new BigDecimal(map.get("qtyToInvoice").toString());
            BigDecimal remainingQty = new BigDecimal(map.get("remainingQty").toString());
            if (qtyToInvoiceItem.compareTo(BigDecimal.ZERO) != 0) {
              if (qtyToInvoiceItem.compareTo(remainingQty) == 1) {
                qtyToInvoiceItem = remainingQty;
              }
              Long stockMoveLineId = Long.valueOf((Integer) map.get("stockMoveLineId"));
              StockMoveLine stockMoveLine = stockMoveLineRepository.find(stockMoveLineId);
              addSubLineQty(qtyToInvoiceMap, qtyToInvoiceItem, stockMoveLineId);
              qtyToInvoiceMap.put(stockMoveLine.getId(), qtyToInvoiceItem);
            }
          }
        }
      } else {
        for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
          qtyToInvoiceMap.put(
              stockMoveLine.getId(),
              stockMoveLine.getRealQty().subtract(stockMoveLine.getQtyInvoiced()));
        }
      }

      Invoice invoice = null;
      Long origin = stockMove.getOriginId();
      try {
        if (StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())) {
          invoice =
              stockMoveInvoiceService.createInvoiceFromSaleOrder(
                  stockMove, saleRepo.find(origin), operationSelect, qtyToInvoiceMap);
        } else if (StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(
            stockMove.getOriginTypeSelect())) {
          invoice =
              stockMoveInvoiceService.createInvoiceFromPurchaseOrder(
                  stockMove, purchaseRepo.find(origin), operationSelect, qtyToInvoiceMap);
        } else {
          invoice =
              stockMoveInvoiceService.createInvoiceFromStockMove(
                  stockMove, operationSelect, qtyToInvoiceMap);
        }

        if (invoice != null) {
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
        response.setCanClose(true);
      } catch (Exception e) {
        TraceBackService.trace(response, e);
      }
    }
  }

  private void addSubLineQty(
      Map<Long, BigDecimal> qtyToInvoiceMap, BigDecimal qtyToInvoiceItem, Long stockMoveLineId) {

    StockMoveLine stockMoveLine = stockMoveLineRepository.find(stockMoveLineId);

    if (stockMoveLine.getProductTypeSelect().equals("pack")) {
      for (StockMoveLine subLine : stockMoveLine.getSubLineList()) {
        BigDecimal qty = BigDecimal.ZERO;
        if (stockMoveLine.getQty().compareTo(BigDecimal.ZERO) != 0) {
          qty =
              qtyToInvoiceItem
                  .multiply(subLine.getQty())
                  .divide(stockMoveLine.getQty(), 2, RoundingMode.HALF_EVEN);
        }
        qty = qty.setScale(2, RoundingMode.HALF_EVEN);
        qtyToInvoiceMap.put(subLine.getId(), qty);
      }
    }
  }

  /**
   * Called from mass invoicing out stock move form view. Call method to check for missing fields.
   * If there are missing fields, show a wizard. Else call {@link
   * StockMoveInvoiceService#createInvoiceFromMultiOutgoingStockMove(List)} and show the generated
   * invoice.
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
          stockMoveInvoiceService.areFieldsConflictedToGenerateCustInvoice(stockMoveList);
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
            stockMoveInvoiceService.createInvoiceFromMultiOutgoingStockMove(stockMoveList);
        invoice.ifPresent(
            inv ->
                response.setView(
                    ActionView.define("Invoice")
                        .model(Invoice.class.getName())
                        .add("grid", "invoice-grid")
                        .add("form", "invoice-form")
                        .param("forceEdit", "true")
                        .context("_showRecord", String.valueOf(inv.getId()))
                        .map()));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from mass invoicing in stock move confirm view. Get parameters entered by the user, then
   * call {@link StockMoveInvoiceService#createInvoiceFromMultiOutgoingStockMove(List,
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
          stockMoveInvoiceService.createInvoiceFromMultiOutgoingStockMove(
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
                      .map()));
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from mass invoicing out stock move form view. Call method to check for missing fields.
   * If there are missing fields, show a wizard. Else call {@link
   * StockMoveInvoiceService#createInvoiceFromMultiOutgoingStockMove(List)} and show the generated
   * invoice.
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
          stockMoveInvoiceService.areFieldsConflictedToGenerateSupplierInvoice(stockMoveList);
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
            stockMoveInvoiceService.createInvoiceFromMultiIncomingStockMove(stockMoveList);
        invoice.ifPresent(
            inv ->
                response.setView(
                    ActionView.define("Invoice")
                        .model(Invoice.class.getName())
                        .add("grid", "invoice-grid")
                        .add("form", "invoice-form")
                        .param("forceEdit", "true")
                        .context("_showRecord", String.valueOf(inv.getId()))
                        .map()));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from mass invoicing in stock move confirm view. Get parameters entered by the user, then
   * call {@link StockMoveInvoiceService#createInvoiceFromMultiIncomingStockMove(List,
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
          stockMoveInvoiceService.createInvoiceFromMultiIncomingStockMove(
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
                      .map()));
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
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
          if (StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())) {
            invoice =
                stockMoveInvoiceService.createInvoiceFromSaleOrder(
                    stockMove, saleRepo.find(stockMove.getOriginId()), 0, null);
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
          if (StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(stockMove.getOriginTypeSelect())) {
            invoice =
                stockMoveInvoiceService.createInvoiceFromPurchaseOrder(
                    stockMove, purchaseRepo.find(stockMove.getOriginId()), 0, null);
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

  public void fillDefaultValueWizard(ActionRequest request, ActionResponse response) {
    try {
      Long id = Long.parseLong(request.getContext().get("_id").toString());
      StockMove stockMove = Beans.get(StockMoveRepository.class).find(id);

      BigDecimal TotalInvoicedQty =
          stockMove
              .getStockMoveLineList()
              .stream()
              .map(StockMoveLine::getQtyInvoiced)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
      if (TotalInvoicedQty.compareTo(BigDecimal.ZERO) == 0) {
        response.setValue("operationSelect", StockMoveRepository.INVOICE_ALL);
      } else {
        response.setValue("operationSelect", StockMoveRepository.INVOICE_PARTILLY);
      }
      List<Map<String, Object>> stockMoveLines =
          stockMoveInvoiceService.getStockMoveLinesToInvoice(stockMove);
      response.setValue("$stockMoveLines", stockMoveLines);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void openInvoicingWizard(ActionRequest request, ActionResponse response) {
    try {
      response.setReload(true);
      StockMove stockMove = request.getContext().asType(StockMove.class);
      List<Map<String, Object>> stockMoveLines =
          stockMoveInvoiceService.getStockMoveLinesToInvoice(stockMove);

      if (stockMoveLines.size() > 0) {
        response.setView(
            ActionView.define("Invoicing")
                .model(StockMove.class.getName())
                .add("form", "stock-move-invoicing-wizard-form")
                .param("popup", "reloa d")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .param("width", "large")
                .param("popup-save", "false")
                .context("_id", stockMove.getId())
                .map());
      } else {
        response.setAlert(I18n.get(IExceptionMessage.STOCK_MOVE_INVOICE_ERROR));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
