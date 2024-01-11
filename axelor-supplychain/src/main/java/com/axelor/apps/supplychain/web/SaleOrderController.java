/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.db.repo.PartnerSupplychainLinkTypeRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.PartnerSupplychainLinkService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChain;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseService;
import com.axelor.apps.supplychain.service.SaleOrderReservedQtyService;
import com.axelor.apps.supplychain.service.SaleOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.SaleOrderStockService;
import com.axelor.apps.supplychain.service.SaleOrderSupplychainService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.exception.MessageExceptionMessage;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class SaleOrderController {

  private final String SO_LINES_WIZARD_QTY_TO_INVOICE_FIELD = "qtyToInvoice";
  private final String SO_LINES_WIZARD_PRICE_FIELD = "price";
  private final String SO_LINES_WIZARD_QTY_FIELD = "qty";

  public void createStockMove(ActionRequest request, ActionResponse response) {

    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

    try {
      if (saleOrder.getId() != null) {

        SaleOrderStockService saleOrderStockService = Beans.get(SaleOrderStockService.class);
        List<Long> stockMoveList =
            saleOrderStockService.createStocksMovesFromSaleOrder(
                Beans.get(SaleOrderRepository.class).find(saleOrder.getId()));

        if (stockMoveList != null && stockMoveList.size() == 1) {
          response.setView(
              ActionView.define(I18n.get("Stock move"))
                  .model(StockMove.class.getName())
                  .add("form", "stock-move-form")
                  .add("grid", "stock-move-grid")
                  .param("search-filters", "internal-stock-move-filters")
                  .param("forceEdit", "true")
                  .domain("self.id = " + stockMoveList.get(0))
                  .context("_showRecord", String.valueOf(stockMoveList.get(0)))
                  .context("_userType", StockMoveRepository.USER_TYPE_SALESPERSON)
                  .map());
          // we have to inject TraceBackService to use non static methods
          Beans.get(TraceBackService.class)
              .findLastMessageTraceBack(
                  Beans.get(StockMoveRepository.class).find(stockMoveList.get(0)))
              .ifPresent(
                  traceback ->
                      response.setNotify(
                          String.format(
                              I18n.get(MessageExceptionMessage.SEND_EMAIL_EXCEPTION),
                              traceback.getMessage())));
        } else if (stockMoveList != null && stockMoveList.size() > 1) {
          response.setView(
              ActionView.define(I18n.get("Stock move"))
                  .model(StockMove.class.getName())
                  .add("grid", "stock-move-grid")
                  .add("form", "stock-move-form")
                  .param("search-filters", "internal-stock-move-filters")
                  .domain("self.id in (" + Joiner.on(",").join(stockMoveList) + ")")
                  .context("_userType", StockMoveRepository.USER_TYPE_SALESPERSON)
                  .map());
          // we have to inject TraceBackService to use non static methods
          TraceBackService traceBackService = Beans.get(TraceBackService.class);
          StockMoveRepository stockMoveRepository = Beans.get(StockMoveRepository.class);

          stockMoveList.stream()
              .map(stockMoveRepository::find)
              .map(traceBackService::findLastMessageTraceBack)
              .filter(Optional::isPresent)
              .map(Optional::get)
              .findAny()
              .ifPresent(
                  traceback ->
                      response.setNotify(
                          String.format(
                              I18n.get(MessageExceptionMessage.SEND_EMAIL_EXCEPTION),
                              traceback.getMessage())));
        } else {
          response.setInfo(
              I18n.get(SupplychainExceptionMessage.SO_NO_DELIVERY_STOCK_MOVE_TO_GENERATE));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getStockLocation(ActionRequest request, ActionResponse response) {

    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    try {
      Company company = saleOrder.getCompany();
      StockLocation stockLocation =
          Beans.get(SaleOrderSupplychainService.class)
              .getStockLocation(saleOrder.getClientPartner(), company);
      response.setValue("stockLocation", stockLocation);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings({"unchecked"})
  public void generatePurchaseOrdersFromSelectedSOLines(
      ActionRequest request, ActionResponse response) {

    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

    try {
      if (saleOrder.getId() != null) {

        Partner supplierPartner = null;
        List<Long> saleOrderLineIdSelected;
        Boolean isDirectOrderLocation = false;
        Boolean noProduct = true;
        Map<String, Object> values = getSelectedId(request, response, saleOrder);
        supplierPartner = (Partner) values.get("supplierPartner");
        saleOrderLineIdSelected = (List<Long>) values.get("saleOrderLineIdSelected");
        isDirectOrderLocation = (Boolean) values.get("isDirectOrderLocation");

        if (supplierPartner == null) {
          saleOrderLineIdSelected = new ArrayList<>();
          for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
            if (saleOrderLine.isSelected()) {
              if (supplierPartner == null) {
                supplierPartner = saleOrderLine.getSupplierPartner();
              }
              if (saleOrderLine.getProduct() != null) {
                noProduct = false;
              }
              saleOrderLineIdSelected.add(saleOrderLine.getId());
            }
          }

          if (saleOrderLineIdSelected.isEmpty() || noProduct) {
            response.setInfo(I18n.get(SupplychainExceptionMessage.SO_LINE_PURCHASE_AT_LEAST_ONE));
          } else {
            response.setView(
                ActionView.define(I18n.get("SaleOrder"))
                    .model(SaleOrder.class.getName())
                    .add("form", "sale-order-generate-po-select-supplierpartner-form")
                    .param("popup", "true")
                    .param("show-toolbar", "false")
                    .param("show-confirm", "false")
                    .param("popup-save", "false")
                    .param("forceEdit", "true")
                    .context("_showRecord", String.valueOf(saleOrder.getId()))
                    .context(
                        "supplierPartnerId",
                        ((supplierPartner != null) ? supplierPartner.getId() : 0L))
                    .context(
                        "saleOrderLineIdSelected", Joiner.on(",").join(saleOrderLineIdSelected))
                    .map());
          }
        } else {
          List<SaleOrderLine> saleOrderLinesSelected =
              JPA.all(SaleOrderLine.class)
                  .filter("self.id IN (:saleOderLineIdList)")
                  .bind("saleOderLineIdList", saleOrderLineIdSelected)
                  .fetch();
          PurchaseOrder purchaseOrder =
              Beans.get(SaleOrderPurchaseService.class)
                  .createPurchaseOrder(
                      supplierPartner,
                      saleOrderLinesSelected,
                      Beans.get(SaleOrderRepository.class).find(saleOrder.getId()));
          response.setView(
              ActionView.define(I18n.get("Purchase order"))
                  .model(PurchaseOrder.class.getName())
                  .add("form", "purchase-order-form")
                  .param("forceEdit", "true")
                  .context("_showRecord", String.valueOf(purchaseOrder.getId()))
                  .map());

          if (isDirectOrderLocation == false) {
            response.setCanClose(true);
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("rawtypes")
  private Map<String, Object> getSelectedId(
      ActionRequest request, ActionResponse response, SaleOrder saleOrder) throws AxelorException {
    Partner supplierPartner = null;
    List<Long> saleOrderLineIdSelected = new ArrayList<>();
    Map<String, Object> values = new HashMap<>();
    Boolean isDirectOrderLocation = false;
    Boolean noProduct = true;

    if (saleOrder.getDirectOrderLocation()
        && saleOrder.getStockLocation() != null
        && saleOrder.getStockLocation().getPartner() != null
        && saleOrder.getStockLocation().getPartner().getIsSupplier()) {
      values.put("supplierPartner", saleOrder.getStockLocation().getPartner());

      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        if (saleOrderLine.isSelected()) {
          if (saleOrderLine.getProduct() != null) {
            noProduct = false;
          }
          saleOrderLineIdSelected.add(saleOrderLine.getId());
        }
      }
      values.put("saleOrderLineIdSelected", saleOrderLineIdSelected);
      isDirectOrderLocation = true;
      values.put("isDirectOrderLocation", isDirectOrderLocation);

      if (saleOrderLineIdSelected.isEmpty() || noProduct) {
        throw new AxelorException(
            3, I18n.get(SupplychainExceptionMessage.SO_LINE_PURCHASE_AT_LEAST_ONE));
      }
    } else if (request.getContext().get("supplierPartnerSelect") != null) {
      supplierPartner =
          JPA.em()
              .find(
                  Partner.class,
                  Long.valueOf(
                      (Integer)
                          ((Map) request.getContext().get("supplierPartnerSelect")).get("id")));
      values.put("supplierPartner", supplierPartner);
      String saleOrderLineIdSelectedStr =
          (String) request.getContext().get("saleOrderLineIdSelected");

      for (String saleOrderId : saleOrderLineIdSelectedStr.split(",")) {
        saleOrderLineIdSelected.add(Long.valueOf(saleOrderId));
      }
      values.put("saleOrderLineIdSelected", saleOrderLineIdSelected);
      values.put("isDirectOrderLocation", isDirectOrderLocation);
    }

    return values;
  }

  /**
   * Called from the sale order invoicing wizard. Call {@link
   * com.axelor.apps.supplychain.service.SaleOrderInvoiceService#generateInvoice(SaleOrder, int,
   * BigDecimal, boolean, Map)} } Return to the view the generated invoice.
   *
   * @param request
   * @param response
   */
  @SuppressWarnings(value = "unchecked")
  public void generateInvoice(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    try {
      SaleOrder saleOrder = context.asType(SaleOrder.class);
      int operationSelect = Integer.parseInt(context.get("operationSelect").toString());
      boolean isPercent = (Boolean) context.getOrDefault("isPercent", false);
      BigDecimal amountToInvoice =
          new BigDecimal(context.getOrDefault("amountToInvoice", "0").toString());

      SaleOrderInvoiceService saleOrderInvoiceService = Beans.get(SaleOrderInvoiceService.class);

      Map<Long, BigDecimal> qtyMap = new HashMap<>();
      Map<Long, BigDecimal> qtyToInvoiceMap = new HashMap<>();
      Map<Long, BigDecimal> priceMap = new HashMap<>();

      List<Map<String, Object>> saleOrderLineListContext;
      saleOrderLineListContext =
          (List<Map<String, Object>>) request.getRawContext().get("saleOrderLineList");
      for (Map<String, Object> map : saleOrderLineListContext) {
        if (map.get(SO_LINES_WIZARD_QTY_TO_INVOICE_FIELD) != null) {
          BigDecimal qtyToInvoiceItem =
              new BigDecimal(map.get(SO_LINES_WIZARD_QTY_TO_INVOICE_FIELD).toString());
          if (qtyToInvoiceItem.compareTo(BigDecimal.ZERO) != 0) {
            Long soLineId = Long.valueOf((Integer) map.get("id"));
            qtyToInvoiceMap.put(soLineId, qtyToInvoiceItem);
            BigDecimal priceItem = new BigDecimal(map.get(SO_LINES_WIZARD_PRICE_FIELD).toString());
            priceMap.put(soLineId, priceItem);
            BigDecimal qtyItem = new BigDecimal(map.get(SO_LINES_WIZARD_QTY_FIELD).toString());
            qtyMap.put(soLineId, qtyItem);
          }
        }
      }

      // Re-compute amount to invoice if invoicing partially
      amountToInvoice =
          saleOrderInvoiceService.computeAmountToInvoice(
              amountToInvoice,
              operationSelect,
              saleOrder,
              qtyToInvoiceMap,
              priceMap,
              qtyMap,
              isPercent);

      saleOrderInvoiceService.displayErrorMessageIfSaleOrderIsInvoiceable(
          saleOrder, amountToInvoice, isPercent);

      // Information to send to the service to handle an invoicing on timetables
      List<Long> timetableIdList = new ArrayList<>();
      ArrayList<LinkedHashMap<String, Object>> uninvoicedTimetablesList =
          (context.get("uninvoicedTimetablesList") != null)
              ? (ArrayList<LinkedHashMap<String, Object>>) context.get("uninvoicedTimetablesList")
              : null;
      if (uninvoicedTimetablesList != null && !uninvoicedTimetablesList.isEmpty()) {

        for (LinkedHashMap<String, Object> timetable : uninvoicedTimetablesList) {
          if (timetable.get("toInvoice") != null && (boolean) timetable.get("toInvoice")) {
            timetableIdList.add(Long.parseLong(timetable.get("id").toString()));
          }
        }
      }

      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());

      Invoice invoice =
          saleOrderInvoiceService.generateInvoice(
              saleOrder,
              operationSelect,
              amountToInvoice,
              isPercent,
              qtyToInvoiceMap,
              timetableIdList);

      if (invoice != null) {
        response.setCanClose(true);
        response.setView(
            ActionView.define(I18n.get("Invoice generated"))
                .model(Invoice.class.getName())
                .add("form", "invoice-form")
                .add("grid", "invoice-grid")
                .param("search-filters", "customer-invoices-filters")
                .context("_showRecord", String.valueOf(invoice.getId()))
                .context("_operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)
                .context(
                    "todayDate",
                    Beans.get(AppSupplychainService.class).getTodayDate(saleOrder.getCompany()))
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateAmountToBeSpreadOverTheTimetable(
      ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    Beans.get(SaleOrderSupplychainService.class).updateAmountToBeSpreadOverTheTimetable(saleOrder);
    response.setValue(
        "amountToBeSpreadOverTheTimetable", saleOrder.getAmountToBeSpreadOverTheTimetable());
  }

  /**
   * Called from sale order on save. Call {@link
   * SaleOrderServiceSupplychainImpl#checkModifiedConfirmedOrder(SaleOrder, SaleOrder)}.
   *
   * @param request
   * @param response
   */
  public void onSave(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrderView = request.getContext().asType(SaleOrder.class);
      if (saleOrderView.getOrderBeingEdited()) {
        SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrderView.getId());
        Beans.get(SaleOrderServiceSupplychainImpl.class)
            .checkModifiedConfirmedOrder(saleOrder, saleOrderView);
        response.setValues(saleOrderView);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  /**
   * Called on sale order invoicing wizard form. Call {@link
   * SaleOrderInvoiceService#getInvoicingWizardOperationDomain(SaleOrder)}
   *
   * @param request
   * @param response
   */
  public void changeWizardOperationDomain(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    List<Integer> operationSelectValues =
        Beans.get(SaleOrderInvoiceService.class).getInvoicingWizardOperationDomain(saleOrder);
    response.setAttr(
        "$operationSelect",
        "value",
        operationSelectValues.stream().min(Integer::compareTo).orElse(null));

    response.setAttr("$operationSelect", "selection-in", operationSelectValues);
  }

  /**
   * Called from sale order generate purchase order form. Set domain for supplier partner.
   *
   * @param request
   * @param response
   */
  public void supplierPartnerSelectDomain(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    String domain = "self.isContact = false AND self.isSupplier = true";

    String blockedPartnerQuery =
        Beans.get(BlockingService.class)
            .listOfBlockedPartner(saleOrder.getCompany(), BlockingRepository.PURCHASE_BLOCKING);

    if (!Strings.isNullOrEmpty(blockedPartnerQuery)) {
      domain += String.format(" AND self.id NOT in (%s)", blockedPartnerQuery);
    }

    if (saleOrder.getCompany() != null) {
      domain += " AND " + saleOrder.getCompany().getId() + " in (SELECT id FROM self.companySet)";
    }
    response.setAttr("supplierPartnerSelect", "domain", domain);
  }

  public void setNextInvoicingStartPeriodDate(ActionRequest request, ActionResponse response) {

    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

    TemporalUnit temporalUnit = ChronoUnit.MONTHS;

    if (saleOrder.getPeriodicityTypeSelect() != null
        && saleOrder.getNextInvoicingStartPeriodDate() != null) {
      LocalDate invoicingPeriodStartDate = saleOrder.getNextInvoicingStartPeriodDate();
      if (saleOrder.getPeriodicityTypeSelect() == 1) {
        temporalUnit = ChronoUnit.DAYS;
      }
      LocalDate subscriptionToDate =
          invoicingPeriodStartDate.plus(saleOrder.getNumberOfPeriods(), temporalUnit);
      subscriptionToDate = subscriptionToDate.minusDays(1);
      response.setValue("nextInvoicingEndPeriodDate", subscriptionToDate);
    }
  }

  /**
   * Called on load of sale order invoicing wizard view. Fill dummy field with default value to
   * avoid issues with null values.
   *
   * @param request
   * @param response
   */
  public void fillDefaultValueWizard(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      List<Map<String, Object>> saleOrderLineList = new ArrayList<>();
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        Map<String, Object> saleOrderLineMap = Mapper.toMap(saleOrderLine);
        saleOrderLineMap.put(SO_LINES_WIZARD_QTY_TO_INVOICE_FIELD, BigDecimal.ZERO);
        saleOrderLineList.add(saleOrderLineMap);
      }
      response.setValue("$amountToInvoice", BigDecimal.ZERO);
      response.setValue("saleOrderLineList", saleOrderLineList);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillSaleOrderLinesEstimatedDate(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (saleOrderLineList != null) {
      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        Integer deliveryState = saleOrderLine.getDeliveryState();
        if (!deliveryState.equals(SaleOrderLineRepository.DELIVERY_STATE_DELIVERED)
            && !deliveryState.equals(SaleOrderLineRepository.DELIVERY_STATE_PARTIALLY_DELIVERED)) {
          saleOrderLine.setEstimatedShippingDate(saleOrder.getEstimatedShippingDate());
        }
      }
    }

    response.setValue("saleOrderLineList", saleOrderLineList);
  }

  public void fillSaleOrderLinesDeliveryDate(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (saleOrderLineList != null) {
      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        saleOrderLine.setEstimatedDeliveryDate(saleOrder.getEstimatedDeliveryDate());
      }
    }

    response.setValue("saleOrderLineList", saleOrderLineList);
  }

  public void notifyStockMoveCreated(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    StockMoveRepository stockMoveRepo = Beans.get(StockMoveRepository.class);
    StockMove stockMove =
        stockMoveRepo
            .all()
            .filter(
                "self.originTypeSelect = ?1 AND self.originId = ?2 AND self.statusSelect = ?3",
                "com.axelor.apps.sale.db.SaleOrder",
                saleOrder.getId(),
                StockMoveRepository.STATUS_PLANNED)
            .fetchOne();
    if (stockMove != null) {
      response.setNotify(
          String.format(
              I18n.get(SupplychainExceptionMessage.SALE_ORDER_STOCK_MOVE_CREATED),
              stockMove.getStockMoveSeq()));
    }
  }

  /**
   * Called from the toolbar in sale order form view. Call {@link
   * com.axelor.apps.supplychain.service.SaleOrderReservedQtyService#allocateAll(SaleOrder)}.
   *
   * @param request
   * @param response
   */
  public void allocateAll(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
      Beans.get(SaleOrderReservedQtyService.class).allocateAll(saleOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from the toolbar in sale order form view. Call {@link
   * com.axelor.apps.supplychain.service.SaleOrderReservedQtyService#deallocateAll(SaleOrder)}.
   *
   * @param request
   * @param response
   */
  public void deallocateAll(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
      Beans.get(SaleOrderReservedQtyService.class).deallocateAll(saleOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from the toolbar in sale order form view. Call {@link
   * com.axelor.apps.supplychain.service.SaleOrderReservedQtyService#reserveAll(SaleOrder)}.
   *
   * @param request
   * @param response
   */
  public void reserveAll(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
      Beans.get(SaleOrderReservedQtyService.class).reserveAll(saleOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from the toolbar in sale order form view. Call {@link
   * com.axelor.apps.supplychain.service.SaleOrderReservedQtyService#cancelReservation(SaleOrder)}.
   *
   * @param request
   * @param response
   */
  public void cancelReservation(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
      Beans.get(SaleOrderReservedQtyService.class).cancelReservation(saleOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showPopUpInvoicingWizard(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
      Beans.get(SaleOrderInvoiceService.class).displayErrorMessageBtnGenerateInvoice(saleOrder);
      response.setView(
          ActionView.define(I18n.get("Invoicing"))
              .model(SaleOrder.class.getName())
              .add("form", "sale-order-invoicing-wizard-form")
              .param("popup", "reload")
              .param("show-toolbar", "false")
              .param("show-confirm", "false")
              .param("popup-save", "false")
              .param("forceEdit", "true")
              .context("_showRecord", String.valueOf(saleOrder.getId()))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateAdvancePaymentInvoice(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    try {
      SaleOrder saleOrder = context.asType(SaleOrder.class);
      Beans.get(SaleOrderInvoiceService.class).displayErrorMessageBtnGenerateInvoice(saleOrder);
      Boolean isPercent = (Boolean) context.getOrDefault("isPercent", false);
      BigDecimal amountToInvoice =
          new BigDecimal(context.getOrDefault("amountToInvoice", "0").toString());
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());

      Invoice invoice =
          Beans.get(SaleOrderInvoiceService.class)
              .generateInvoice(
                  saleOrder,
                  SaleOrderRepository.INVOICE_ADVANCE_PAYMENT,
                  amountToInvoice,
                  isPercent,
                  null,
                  null);

      if (invoice != null) {
        response.setCanClose(true);
        response.setView(
            ActionView.define(I18n.get("Invoice generated"))
                .model(Invoice.class.getName())
                .add("form", "invoice-form")
                .add("grid", "invoice-grid")
                .param("search-filters", "customer-invoices-filters")
                .context("_showRecord", String.valueOf(invoice.getId()))
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void backToConfirmedStatus(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
      Beans.get(SaleOrderSupplychainService.class).updateToConfirmedStatus(saleOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createShipmentCostLine(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      String message =
          Beans.get(SaleOrderSupplychainService.class).createShipmentCostLine(saleOrder);
      if (message != null) {
        response.setInfo(message);
      }
      response.setValues(saleOrder);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
  /**
   * Called from sale order form view, on invoiced partner select. Call {@link
   * PartnerSupplychainLinkService#computePartnerFilter}
   *
   * @param request
   * @param response
   */
  public void setInvoicedPartnerDomain(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      String strFilter =
          Beans.get(PartnerSupplychainLinkService.class)
              .computePartnerFilter(
                  saleOrder.getClientPartner(),
                  PartnerSupplychainLinkTypeRepository.TYPE_SELECT_INVOICED_BY);

      response.setAttr("invoicedPartner", "domain", strFilter);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from sale order form view, on delivered partner select. Call {@link
   * PartnerSupplychainLinkService#computePartnerFilter}
   *
   * @param request
   * @param response
   */
  public void setDeliveredPartnerDomain(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      String strFilter =
          Beans.get(PartnerSupplychainLinkService.class)
              .computePartnerFilter(
                  saleOrder.getClientPartner(),
                  PartnerSupplychainLinkTypeRepository.TYPE_SELECT_DELIVERED_BY);

      response.setAttr("deliveredPartner", "domain", strFilter);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from sale order view, on delivery date change. <br>
   * Update stock reservation date for each sale order line by calling {@link
   * SaleOrderLineServiceSupplyChain#updateStockMoveReservationDateTime(SaleOrderLine)}.
   *
   * @param request
   * @param response
   */
  public void updateStockReservationDate(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain =
          Beans.get(SaleOrderLineServiceSupplyChain.class);
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        saleOrderLineServiceSupplyChain.updateStockMoveReservationDateTime(saleOrderLine);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setDefaultInvoicedAndDeliveredPartnersAndAddresses(
      ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      Beans.get(SaleOrderSupplychainService.class)
          .setDefaultInvoicedAndDeliveredPartnersAndAddresses(saleOrder);
      response.setValues(saleOrder);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getToStockLocation(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    try {
      Company company = saleOrder.getCompany();
      StockLocation toStockLocation =
          Beans.get(SaleOrderSupplychainService.class)
              .getToStockLocation(saleOrder.getClientPartner(), company);
      response.setValue("toStockLocation", toStockLocation);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
