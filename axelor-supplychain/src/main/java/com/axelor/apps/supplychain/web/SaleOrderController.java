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

import static java.util.stream.Collectors.groupingBy;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.db.repo.PartnerLinkTypeRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.PartnerLinkService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.stock.db.FreightCarrierMode;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.FreightCarrierModeRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.FreightCarrierModeService;
import com.axelor.apps.supplychain.service.PurchaseOrderFromSaleOrderLinesService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderReservedQtyService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderShipmentService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderStockLocationService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderStockService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineServiceSupplyChain;
import com.axelor.db.JPA;
import com.axelor.db.Model;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class SaleOrderController {

  private final String SO_LINES_WIZARD_PRICE_FIELD = "price";
  private final String SO_LINES_WIZARD_QTY_FIELD = "qty";
  private final String SO_LINES_WIZARD_INVOICE_ALL_FIELD = "invoiceAll";

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
          Beans.get(SaleOrderStockLocationService.class)
              .getStockLocation(saleOrder.getClientPartner(), company);
      response.setValue("stockLocation", stockLocation);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generatePurchaseOrdersFromSelectedSOLines(
      ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    List<SaleOrderLine> saleOrderLines =
        saleOrder.getSaleOrderLineList().stream()
            .filter(Model::isSelected)
            .collect(Collectors.toList());
    Partner supplierPartner = null;
    String saleOrderLinesIdStr = null;

    if (request.getContext().get("supplierPartnerSelect") != null) {
      supplierPartner =
          JPA.em()
              .find(
                  Partner.class,
                  Long.valueOf(
                      (Integer)
                          ((Map) request.getContext().get("supplierPartnerSelect")).get("id")));

      saleOrderLinesIdStr = (String) request.getContext().get("saleOrderLineIdSelected");
    }

    PurchaseOrderFromSaleOrderLinesService purchaseOrderFromSaleOrderLinesService =
        Beans.get(PurchaseOrderFromSaleOrderLinesService.class);

    try {
      response.setView(
          purchaseOrderFromSaleOrderLinesService.generatePurchaseOrdersFromSOLines(
              saleOrder, saleOrderLines, supplierPartner, saleOrderLinesIdStr));

      if (supplierPartner != null
          && !purchaseOrderFromSaleOrderLinesService.isDirectOrderLocation(saleOrder)) {
        response.setCanClose(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from the sale order invoicing wizard. Call {@link
   * SaleOrderInvoiceService#generateInvoice(SaleOrder, int, BigDecimal, boolean, Map)} } Return to
   * the view the generated invoice.
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
      fillMaps(saleOrderLineListContext, qtyToInvoiceMap, priceMap, qtyMap);

      saleOrderInvoiceService.displayErrorMessageIfSaleOrderIsInvoiceable(
          saleOrder,
          amountToInvoice,
          operationSelect,
          qtyToInvoiceMap,
          priceMap,
          qtyMap,
          isPercent);

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

  private void fillMaps(
      List<Map<String, Object>> saleOrderLineListContext,
      Map<Long, BigDecimal> qtyToInvoiceMap,
      Map<Long, BigDecimal> priceMap,
      Map<Long, BigDecimal> qtyMap) {
    for (Map<String, Object> map : saleOrderLineListContext) {
      if (map.get(SaleOrderInvoiceService.SO_LINES_WIZARD_QTY_TO_INVOICE_FIELD) != null) {
        BigDecimal qtyToInvoiceItem =
            new BigDecimal(
                map.get(SaleOrderInvoiceService.SO_LINES_WIZARD_QTY_TO_INVOICE_FIELD).toString());
        boolean invoiceAllItem =
            Boolean.parseBoolean(
                map.getOrDefault(SO_LINES_WIZARD_INVOICE_ALL_FIELD, false).toString());
        if (qtyToInvoiceItem.compareTo(BigDecimal.ZERO) != 0
            || (Objects.equals(SaleOrderLineRepository.TYPE_TITLE, map.get("typeSelect"))
                && invoiceAllItem)) {
          Long soLineId = Long.valueOf((Integer) map.get("id"));
          qtyToInvoiceMap.put(soLineId, qtyToInvoiceItem);
          BigDecimal priceItem = new BigDecimal(map.get(SO_LINES_WIZARD_PRICE_FIELD).toString());
          priceMap.put(soLineId, priceItem);
          BigDecimal qtyItem = new BigDecimal(map.get(SO_LINES_WIZARD_QTY_FIELD).toString());
          qtyMap.put(soLineId, qtyItem);
        }
      }
    }
  }

  public void generateInvoicesFromSelectedLines(ActionRequest request, ActionResponse response) {

    try {

      SaleOrderRepository saleOrderRepository = Beans.get(SaleOrderRepository.class);
      Context context = request.getContext();
      List<Map<String, Object>> saleOrderLineListContext;
      saleOrderLineListContext =
          (List<Map<String, Object>>) request.getRawContext().get("saleOrderLineListToInvoice");
      if (saleOrderLineListContext.isEmpty()) {
        response.setAlert(I18n.get("No items have been selected."));
        return;
      }
      int operationSelect = SaleOrderRepository.INVOICE_LINES;
      boolean isPercent = (Boolean) context.getOrDefault("isPercent", false);

      Map<SaleOrder, List<Map<String, Object>>> saleOrderLineListContextMap =
          saleOrderLineListContext.stream()
              .collect(
                  groupingBy(
                      stringObjectMap ->
                          saleOrderRepository.find(
                              Long.valueOf(
                                  (Integer)
                                      ((LinkedHashMap<?, ?>) stringObjectMap.get("saleOrder"))
                                          .get("id")))));

      Map<SaleOrder, BigDecimal> amountToInvoiceMap = new HashMap<>();
      Map<SaleOrder, Map<Long, BigDecimal>> qtyMaps = new HashMap<>();
      Map<SaleOrder, Map<Long, BigDecimal>> qtyToInvoiceMaps = new HashMap<>();
      Map<SaleOrder, Map<Long, BigDecimal>> priceMaps = new HashMap<>();
      for (Map.Entry<SaleOrder, List<Map<String, Object>>> entry :
          saleOrderLineListContextMap.entrySet()) {
        SaleOrder saleOrder = entry.getKey();
        BigDecimal amountToInvoice =
            saleOrder.getExTaxTotal().subtract(saleOrder.getAmountInvoiced());
        amountToInvoiceMap.put(saleOrder, amountToInvoice);
        Map<Long, BigDecimal> qtyMap = new HashMap<>();
        Map<Long, BigDecimal> qtyToInvoiceMap = new HashMap<>();
        Map<Long, BigDecimal> priceMap = new HashMap<>();
        fillMaps(entry.getValue(), qtyToInvoiceMap, priceMap, qtyMap);
        qtyMaps.put(saleOrder, qtyMap);
        qtyToInvoiceMaps.put(saleOrder, qtyToInvoiceMap);
        priceMaps.put(saleOrder, priceMap);
      }

      List<Invoice> invoiceList =
          Beans.get(SaleOrderInvoiceService.class)
              .generateInvoicesFromSaleOrderLines(
                  priceMaps,
                  qtyToInvoiceMaps,
                  qtyMaps,
                  amountToInvoiceMap,
                  isPercent,
                  operationSelect);
      response.setCanClose(true);
      List<Long> invoicesIds =
          invoiceList.stream().map(Invoice::getId).collect(Collectors.toList());
      response.setView(
          ActionView.define(I18n.get("Invoices"))
              .model(Invoice.class.getName())
              .add("grid", "invoice-grid")
              .add("form", "invoice-form")
              .domain("self.id IN :invoicesIds")
              .context("invoicesIds", invoicesIds)
              .map());

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

    if (saleOrder.getCompany() != null) {
      String blockedPartnerQuery =
          Beans.get(BlockingService.class)
              .listOfBlockedPartner(saleOrder.getCompany(), BlockingRepository.PURCHASE_BLOCKING);

      if (!Strings.isNullOrEmpty(blockedPartnerQuery)) {
        domain += String.format(" AND self.id NOT in (%s)", blockedPartnerQuery);
      }
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
      List<Map<String, Object>> saleOrderLineList =
          Beans.get(SaleOrderInvoiceService.class).getSaleOrderLineList(saleOrder);
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
                ":saleOrderId MEMBER OF self.saleOrderSet AND self.statusSelect = :statusSelect")
            .bind("saleOrderId", saleOrder.getId())
            .bind("statusSelect", StockMoveRepository.STATUS_PLANNED)
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
      ShipmentMode shipmentMode = saleOrder.getShipmentMode();
      String message =
          Beans.get(SaleOrderShipmentService.class).createShipmentCostLine(saleOrder, shipmentMode);
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
   * PartnerLinkService#computePartnerFilter}
   *
   * @param request
   * @param response
   */
  public void setInvoicedPartnerDomain(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      String strFilter =
          Beans.get(PartnerLinkService.class)
              .computePartnerFilter(
                  saleOrder.getClientPartner(), PartnerLinkTypeRepository.TYPE_SELECT_INVOICED_BY);

      response.setAttr("invoicedPartner", "domain", strFilter);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from sale order form view, on delivered partner select. Call {@link
   * PartnerLinkService#computePartnerFilter}
   *
   * @param request
   * @param response
   */
  public void setDeliveredPartnerDomain(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      String strFilter =
          Beans.get(PartnerLinkService.class)
              .computePartnerFilter(
                  saleOrder.getClientPartner(), PartnerLinkTypeRepository.TYPE_SELECT_DELIVERED_BY);

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
          Beans.get(SaleOrderStockLocationService.class)
              .getToStockLocation(saleOrder.getClientPartner(), company);
      response.setValue("toStockLocation", toStockLocation);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAdvancePayment(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    Beans.get(SaleOrderSupplychainService.class).setAdvancePayment(saleOrder);
    response.setValues(saleOrder);
  }

  public void updateTimetableAmounts(ActionRequest request, ActionResponse response)
      throws AxelorException {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    Beans.get(SaleOrderSupplychainService.class).updateTimetableAmounts(saleOrder);
    response.setValues(saleOrder);
  }

  public void setAmountToInvoiceScale(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    try {
      boolean isPercent = (Boolean) request.getContext().getOrDefault("isPercent", false);

      if (saleOrder != null && saleOrder.getCurrency() != null) {
        response.setAttr(
            "$amountToInvoice",
            "scale",
            isPercent
                ? AppSaleService.DEFAULT_NB_DECIMAL_DIGITS
                : Beans.get(CurrencyScaleService.class).getScale(saleOrder));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void selectFreightCarrierPricings(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    try {
      FreightCarrierModeRepository freightCarrierModeRepository =
          Beans.get(FreightCarrierModeRepository.class);
      List<FreightCarrierMode> freightCarrierModeList =
          ((List<Map<String, Object>>) context.get("freightCarrierPricingsSet"))
              .stream()
                  .map(o -> Mapper.toBean(FreightCarrierMode.class, o))
                  .filter(FreightCarrierMode::isSelected)
                  .map(it -> freightCarrierModeRepository.find(it.getId()))
                  .collect(Collectors.toList());

      if (context.get("_id") != null) {
        Beans.get(FreightCarrierModeService.class)
            .computeFreightCarrierMode(
                freightCarrierModeList, Long.valueOf(context.get("_id").toString()));
        response.setCanClose(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
