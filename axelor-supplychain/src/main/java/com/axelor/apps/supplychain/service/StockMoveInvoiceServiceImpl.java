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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceGeneratorSupplyChain;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineGeneratorSupplyChain;
import com.axelor.apps.tool.StringTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class StockMoveInvoiceServiceImpl implements StockMoveInvoiceService {

  private SaleOrderInvoiceService saleOrderInvoiceService;
  private PurchaseOrderInvoiceService purchaseOrderInvoiceService;
  private StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain;
  private InvoiceRepository invoiceRepository;
  private StockMoveRepository stockMoveRepo;
  private SaleOrderRepository saleOrderRepository;
  private PurchaseOrderRepository purchaseOrderRepository;

  @Inject
  public StockMoveInvoiceServiceImpl(
      SaleOrderInvoiceService saleOrderInvoiceService,
      PurchaseOrderInvoiceService purchaseOrderInvoiceService,
      StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain,
      InvoiceRepository invoiceRepository,
      StockMoveRepository stockMoveRepo,
      SaleOrderRepository saleOrderRepository,
      PurchaseOrderRepository purchaseOrderRepository) {
    this.saleOrderInvoiceService = saleOrderInvoiceService;
    this.purchaseOrderInvoiceService = purchaseOrderInvoiceService;
    this.stockMoveLineServiceSupplychain = stockMoveLineServiceSupplychain;
    this.invoiceRepository = invoiceRepository;
    this.stockMoveRepo = stockMoveRepo;
    this.saleOrderRepository = saleOrderRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Invoice createInvoiceFromSaleOrder(
      StockMove stockMove,
      SaleOrder saleOrder,
      int operationSelect,
      Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException {

    InvoiceGenerator invoiceGenerator =
        saleOrderInvoiceService.createInvoiceGenerator(saleOrder, stockMove.getIsReversion());

    Invoice invoice = invoiceGenerator.generate();

    invoiceGenerator.populate(
        invoice,
        this.createInvoiceLines(invoice, stockMove.getStockMoveLineList(), qtyToInvoiceMap));

    if (invoice != null) {
      invoice.setSaleOrder(saleOrder);
      saleOrderInvoiceService.fillInLines(invoice);
      this.extendInternalReference(stockMove, invoice);
      invoice.setAddressStr(saleOrder.getMainInvoicingAddressStr());

      // fill default advance payment invoice
      if (invoice.getOperationSubTypeSelect() != InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
        invoice.setAdvancePaymentInvoiceSet(
            Beans.get(InvoiceService.class).getDefaultAdvancePaymentInvoice(invoice));
      }

      invoice.setPartnerTaxNbr(saleOrder.getClientPartner().getTaxNbr());
      invoice.setNote(saleOrder.getInvoiceComments());

      invoiceRepository.save(invoice);

      stockMove.getInvoiceSet().add(invoice);

      stockMoveRepo.save(stockMove);
    }

    return invoice;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Invoice createInvoiceFromPurchaseOrder(
      StockMove stockMove,
      PurchaseOrder purchaseOrder,
      int operationSelect,
      Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException {

    InvoiceGenerator invoiceGenerator =
        purchaseOrderInvoiceService.createInvoiceGenerator(
            purchaseOrder, stockMove.getIsReversion());

    Invoice invoice = invoiceGenerator.generate();

    invoiceGenerator.populate(
        invoice,
        this.createInvoiceLines(invoice, stockMove.getStockMoveLineList(), qtyToInvoiceMap));

    if (invoice != null) {

      this.extendInternalReference(stockMove, invoice);
      invoice.setAddressStr(
          Beans.get(AddressService.class).computeAddressStr(invoice.getAddress()));
      invoiceRepository.save(invoice);

      stockMove.getInvoiceSet().add(invoice);

      stockMoveRepo.save(stockMove);
    }
    return invoice;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Invoice createInvoiceFromStockMove(
      StockMove stockMove, int operationSelect, Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException {

    int stockMoveType = stockMove.getTypeSelect();
    int invoiceOperationType;

    if (stockMove.getIsReversion()) {
      if (stockMoveType == StockMoveRepository.TYPE_INCOMING) {
        invoiceOperationType = InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND;
      } else if (stockMoveType == StockMoveRepository.TYPE_OUTGOING) {
        invoiceOperationType = InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND;
      } else {
        return null;
      }
    } else {
      if (stockMoveType == StockMoveRepository.TYPE_INCOMING) {
        invoiceOperationType = InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE;
      } else if (stockMoveType == StockMoveRepository.TYPE_OUTGOING) {
        invoiceOperationType = InvoiceRepository.OPERATION_TYPE_CLIENT_SALE;
      } else {
        return null;
      }
    }

    InvoiceGenerator invoiceGenerator =
        new InvoiceGeneratorSupplyChain(stockMove, invoiceOperationType) {

          @Override
          public Invoice generate() throws AxelorException {

            return super.createInvoiceHeader();
          }
        };

    Invoice invoice = invoiceGenerator.generate();

    invoiceGenerator.populate(
        invoice,
        this.createInvoiceLines(invoice, stockMove.getStockMoveLineList(), qtyToInvoiceMap));

    if (invoice != null) {
      saleOrderInvoiceService.fillInLines(invoice);
      this.extendInternalReference(stockMove, invoice);
      invoice.setAddressStr(
          Beans.get(AddressService.class).computeAddressStr(invoice.getAddress()));
      invoiceRepository.save(invoice);

      stockMove.getInvoiceSet().add(invoice);

      stockMoveRepo.save(stockMove);
    }

    return invoice;
  }

  @Override
  public Map<String, Object> areFieldsConflictedToGenerateCustInvoice(List<StockMove> stockMoveList)
      throws AxelorException {
    Map<String, Object> mapResult = new HashMap<>();

    boolean paymentConditionToCheck = false;
    boolean paymentModeToCheck = false;
    boolean contactPartnerToCheck = false;

    checkForAlreadyInvoicedStockMove(stockMoveList);
    List<Invoice> dummyInvoiceList =
        stockMoveList.stream().map(this::createDummyOutInvoice).collect(Collectors.toList());
    checkOutStockMoveRequiredFieldsAreTheSame(dummyInvoiceList);

    if (!dummyInvoiceList.isEmpty()) {
      PaymentCondition firstPaymentCondition = dummyInvoiceList.get(0).getPaymentCondition();
      PaymentMode firstPaymentMode = dummyInvoiceList.get(0).getPaymentMode();
      Partner firstContactPartner = dummyInvoiceList.get(0).getContactPartner();
      paymentConditionToCheck =
          !dummyInvoiceList
              .stream()
              .map(Invoice::getPaymentCondition)
              .allMatch(
                  paymentCondition -> Objects.equals(paymentCondition, firstPaymentCondition));
      paymentModeToCheck =
          !dummyInvoiceList
              .stream()
              .map(Invoice::getPaymentMode)
              .allMatch(paymentMode -> Objects.equals(paymentMode, firstPaymentMode));
      contactPartnerToCheck =
          !dummyInvoiceList
              .stream()
              .map(Invoice::getContactPartner)
              .allMatch(contactPartner -> Objects.equals(contactPartner, firstContactPartner));

      mapResult.put("paymentCondition", firstPaymentCondition);
      mapResult.put("paymentMode", firstPaymentMode);
      mapResult.put("contactPartner", firstContactPartner);
    }

    mapResult.put("paymentConditionToCheck", paymentConditionToCheck);
    mapResult.put("paymentModeToCheck", paymentModeToCheck);
    mapResult.put("contactPartnerToCheck", contactPartnerToCheck);

    return mapResult;
  }

  protected void checkOutStockMoveRequiredFieldsAreTheSame(List<Invoice> dummyInvoiceList)
      throws AxelorException {
    if (dummyInvoiceList == null || dummyInvoiceList.isEmpty()) {
      return;
    }
    Invoice firstDummyInvoice = dummyInvoiceList.get(0);

    for (Invoice dummyInvoice : dummyInvoiceList) {
      if (firstDummyInvoice.getCurrency() != null
          && !firstDummyInvoice.getCurrency().equals(dummyInvoice.getCurrency())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.STOCK_MOVE_MULTI_INVOICE_CURRENCY));
      }

      if (firstDummyInvoice.getPartner() != null
          && !firstDummyInvoice.getPartner().equals(dummyInvoice.getPartner())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.STOCK_MOVE_MULTI_INVOICE_CLIENT_PARTNER));
      }

      if (firstDummyInvoice.getCompany() != null
          && !firstDummyInvoice.getCompany().equals(dummyInvoice.getCompany())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.STOCK_MOVE_MULTI_INVOICE_COMPANY_SO));
      }

      if ((firstDummyInvoice.getTradingName() != null
              && !firstDummyInvoice.getTradingName().equals(dummyInvoice.getTradingName()))
          || (firstDummyInvoice.getTradingName() == null
              && dummyInvoice.getTradingName() != null)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.STOCK_MOVE_MULTI_INVOICE_TRADING_NAME_SO));
      }

      if (firstDummyInvoice.getInAti() != null
          && !firstDummyInvoice.getInAti().equals(dummyInvoice.getInAti())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.STOCK_MOVE_MULTI_INVOICE_IN_ATI));
      }
    }
  }

  @Override
  public Map<String, Object> areFieldsConflictedToGenerateSupplierInvoice(
      List<StockMove> stockMoveList) throws AxelorException {
    Map<String, Object> mapResult = new HashMap<>();

    boolean paymentConditionToCheck = false;
    boolean paymentModeToCheck = false;
    boolean contactPartnerToCheck = false;

    checkForAlreadyInvoicedStockMove(stockMoveList);
    List<Invoice> dummyInvoiceList =
        stockMoveList.stream().map(this::createDummyInInvoice).collect(Collectors.toList());
    checkInStockMoveRequiredFieldsAreTheSame(dummyInvoiceList);

    if (!dummyInvoiceList.isEmpty()) {
      PaymentCondition firstPaymentCondition = dummyInvoiceList.get(0).getPaymentCondition();
      PaymentMode firstPaymentMode = dummyInvoiceList.get(0).getPaymentMode();
      Partner firstContactPartner = dummyInvoiceList.get(0).getContactPartner();
      paymentConditionToCheck =
          !dummyInvoiceList
              .stream()
              .map(Invoice::getPaymentCondition)
              .allMatch(
                  paymentCondition -> Objects.equals(paymentCondition, firstPaymentCondition));
      paymentModeToCheck =
          !dummyInvoiceList
              .stream()
              .map(Invoice::getPaymentMode)
              .allMatch(paymentMode -> Objects.equals(paymentMode, firstPaymentMode));
      contactPartnerToCheck =
          !dummyInvoiceList
              .stream()
              .map(Invoice::getContactPartner)
              .allMatch(contactPartner -> Objects.equals(contactPartner, firstContactPartner));
      mapResult.put("paymentCondition", firstPaymentCondition);
      mapResult.put("paymentMode", firstPaymentMode);
      mapResult.put("contactPartner", firstContactPartner);
    }

    mapResult.put("paymentConditionToCheck", paymentConditionToCheck);
    mapResult.put("paymentModeToCheck", paymentModeToCheck);
    mapResult.put("contactPartnerToCheck", contactPartnerToCheck);

    return mapResult;
  }

  protected void checkInStockMoveRequiredFieldsAreTheSame(List<Invoice> dummyInvoiceList)
      throws AxelorException {
    if (dummyInvoiceList == null || dummyInvoiceList.isEmpty()) {
      return;
    }
    Invoice firstDummyInvoice = dummyInvoiceList.get(0);

    for (Invoice dummyInvoice : dummyInvoiceList) {

      if (firstDummyInvoice.getCurrency() != null
          && !firstDummyInvoice.getCurrency().equals(dummyInvoice.getCurrency())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.STOCK_MOVE_MULTI_INVOICE_CURRENCY));
      }

      if (firstDummyInvoice.getPartner() != null
          && !firstDummyInvoice.getPartner().equals(dummyInvoice.getPartner())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.STOCK_MOVE_MULTI_INVOICE_SUPPLIER_PARTNER));
      }

      if (firstDummyInvoice.getCompany() != null
          && !firstDummyInvoice.getCompany().equals(dummyInvoice.getCompany())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.STOCK_MOVE_MULTI_INVOICE_COMPANY_PO));
      }

      if ((firstDummyInvoice.getTradingName() != null
              && !firstDummyInvoice.getTradingName().equals(dummyInvoice.getTradingName()))
          || (firstDummyInvoice.getTradingName() == null
              && dummyInvoice.getTradingName() != null)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.STOCK_MOVE_MULTI_INVOICE_TRADING_NAME_PO));
      }

      if (firstDummyInvoice.getInAti() != null
          && !firstDummyInvoice.getInAti().equals(dummyInvoice.getInAti())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.STOCK_MOVE_MULTI_INVOICE_IN_ATI));
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public Optional<Invoice> createInvoiceFromMultiOutgoingStockMove(
      List<StockMove> stockMoveList,
      PaymentCondition paymentConditionIn,
      PaymentMode paymentModeIn,
      Partner contactPartnerIn)
      throws AxelorException {
    Optional<Invoice> invoiceOpt = this.createInvoiceFromMultiOutgoingStockMove(stockMoveList);
    invoiceOpt.ifPresent(
        invoice ->
            fillInvoiceFromMultiStockMoveDefaultValues(
                invoice, paymentConditionIn, paymentModeIn, contactPartnerIn));
    return invoiceOpt;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public Optional<Invoice> createInvoiceFromMultiOutgoingStockMove(List<StockMove> stockMoveList)
      throws AxelorException {

    if (stockMoveList == null || stockMoveList.isEmpty()) {
      return Optional.empty();
    }

    // create dummy invoice from the first stock move
    Invoice dummyInvoice = createDummyOutInvoice(stockMoveList.get(0));

    // Check if field constraints are respected
    for (StockMove stockMove : stockMoveList) {
      completeInvoiceInMultiOutgoingStockMove(dummyInvoice, stockMove);
    }

    /*  check if some other fields are different and assign a default value */
    if (dummyInvoice.getAddress() == null) {
      dummyInvoice.setAddress(
          Beans.get(PartnerService.class).getInvoicingAddress(dummyInvoice.getPartner()));
      dummyInvoice.setAddressStr(
          Beans.get(AddressService.class).computeAddressStr(dummyInvoice.getAddress()));
    }

    fillReferenceInvoiceFromMultiOutStockMove(stockMoveList, dummyInvoice);

    InvoiceGenerator invoiceGenerator =
        new InvoiceGenerator(
            InvoiceRepository.OPERATION_TYPE_CLIENT_SALE,
            dummyInvoice.getCompany(),
            dummyInvoice.getPaymentCondition(),
            dummyInvoice.getPaymentMode(),
            dummyInvoice.getAddress(),
            dummyInvoice.getPartner(),
            dummyInvoice.getContactPartner(),
            dummyInvoice.getCurrency(),
            dummyInvoice.getPriceList(),
            dummyInvoice.getInternalReference(),
            dummyInvoice.getExternalReference(),
            dummyInvoice.getInAti(),
            null,
            dummyInvoice.getTradingName()) {

          @Override
          public Invoice generate() throws AxelorException {

            return super.createInvoiceHeader();
          }
        };

    Invoice invoice = invoiceGenerator.generate();
    invoice.setAddressStr(dummyInvoice.getAddressStr());

    List<InvoiceLine> invoiceLineList = new ArrayList<>();

    for (StockMove stockMoveLocal : stockMoveList) {
      invoiceLineList.addAll(
          this.createInvoiceLines(invoice, stockMoveLocal.getStockMoveLineList(), null));
    }

    invoiceGenerator.populate(invoice, invoiceLineList);
    stockMoveList.forEach(stockMove -> stockMove.getInvoiceSet().add(invoice));
    invoiceRepository.save(invoice);
    return Optional.of(invoice);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public Optional<Invoice> createInvoiceFromMultiIncomingStockMove(
      List<StockMove> stockMoveList,
      PaymentCondition paymentConditionIn,
      PaymentMode paymentModeIn,
      Partner contactPartnerIn)
      throws AxelorException {
    Optional<Invoice> invoiceOpt = createInvoiceFromMultiIncomingStockMove(stockMoveList);
    invoiceOpt.ifPresent(
        invoice ->
            fillInvoiceFromMultiStockMoveDefaultValues(
                invoice, paymentConditionIn, paymentModeIn, contactPartnerIn));
    return invoiceOpt;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public Optional<Invoice> createInvoiceFromMultiIncomingStockMove(List<StockMove> stockMoveList)
      throws AxelorException {
    if (stockMoveList == null || stockMoveList.isEmpty()) {
      return Optional.empty();
    }

    // create dummy invoice from the first stock move
    Invoice dummyInvoice = createDummyInInvoice(stockMoveList.get(0));

    // Check if field constraints are respected
    for (StockMove stockMove : stockMoveList) {
      completeInvoiceInMultiIncomingStockMove(dummyInvoice, stockMove);
    }

    /*  check if some other fields are different and assign a default value */
    if (dummyInvoice.getAddress() == null) {
      dummyInvoice.setAddress(
          Beans.get(PartnerService.class).getInvoicingAddress(dummyInvoice.getPartner()));
      dummyInvoice.setAddressStr(
          Beans.get(AddressService.class).computeAddressStr(dummyInvoice.getAddress()));
    }

    fillReferenceInvoiceFromMultiInStockMove(stockMoveList, dummyInvoice);

    InvoiceGenerator invoiceGenerator =
        new InvoiceGenerator(
            InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE,
            dummyInvoice.getCompany(),
            dummyInvoice.getPaymentCondition(),
            dummyInvoice.getPaymentMode(),
            dummyInvoice.getAddress(),
            dummyInvoice.getPartner(),
            dummyInvoice.getContactPartner(),
            dummyInvoice.getCurrency(),
            dummyInvoice.getPriceList(),
            dummyInvoice.getInternalReference(),
            dummyInvoice.getExternalReference(),
            dummyInvoice.getInAti(),
            null,
            dummyInvoice.getTradingName()) {

          @Override
          public Invoice generate() throws AxelorException {

            return super.createInvoiceHeader();
          }
        };

    Invoice invoice = invoiceGenerator.generate();
    invoice.setAddressStr(dummyInvoice.getAddressStr());

    List<InvoiceLine> invoiceLineList = new ArrayList<>();

    for (StockMove stockMoveLocal : stockMoveList) {
      invoiceLineList.addAll(
          this.createInvoiceLines(invoice, stockMoveLocal.getStockMoveLineList(), null));
    }

    invoiceGenerator.populate(invoice, invoiceLineList);
    stockMoveList.forEach(stockMove -> stockMove.getInvoiceSet().add(invoice));
    invoiceRepository.save(invoice);
    return Optional.of(invoice);
  }

  protected void fillInvoiceFromMultiStockMoveDefaultValues(
      Invoice invoice,
      PaymentCondition paymentConditionIn,
      PaymentMode paymentModeIn,
      Partner contactPartnerIn) {
    invoice.setPaymentCondition(paymentConditionIn);
    invoice.setPaymentMode(paymentModeIn);
    invoice.setContactPartner(contactPartnerIn);
  }

  /**
   * Create a dummy invoice to hold fields used to generate the invoice which will be saved.
   *
   * @param stockMove an out stock move.
   * @return the created dummy invoice.
   */
  protected Invoice createDummyOutInvoice(StockMove stockMove) {
    Invoice dummyInvoice = new Invoice();

    if (stockMove.getOriginId() != null
        && StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      SaleOrder saleOrder = saleOrderRepository.find(stockMove.getOriginId());
      dummyInvoice.setCurrency(saleOrder.getCurrency());
      dummyInvoice.setPartner(saleOrder.getClientPartner());
      dummyInvoice.setCompany(saleOrder.getCompany());
      dummyInvoice.setTradingName(saleOrder.getTradingName());
      dummyInvoice.setPaymentCondition(saleOrder.getPaymentCondition());
      dummyInvoice.setPaymentMode(saleOrder.getPaymentMode());
      dummyInvoice.setAddress(saleOrder.getMainInvoicingAddress());
      dummyInvoice.setAddressStr(saleOrder.getMainInvoicingAddressStr());
      dummyInvoice.setContactPartner(saleOrder.getContactPartner());
      dummyInvoice.setPriceList(saleOrder.getPriceList());
      dummyInvoice.setInAti(saleOrder.getInAti());
    } else {
      dummyInvoice.setCurrency(stockMove.getCompany().getCurrency());
      dummyInvoice.setPartner(stockMove.getPartner());
      dummyInvoice.setCompany(stockMove.getCompany());
      dummyInvoice.setTradingName(stockMove.getTradingName());
      dummyInvoice.setAddress(stockMove.getToAddress());
      dummyInvoice.setAddressStr(stockMove.getToAddressStr());
    }
    return dummyInvoice;
  }

  /**
   * Create a dummy invoice to hold fields used to generate the invoice which will be saved.
   *
   * @param stockMove an in stock move.
   * @return the created dummy invoice.
   */
  protected Invoice createDummyInInvoice(StockMove stockMove) {
    Invoice dummyInvoice = new Invoice();

    if (stockMove.getOriginId() != null
        && StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      PurchaseOrder purchaseOrder = purchaseOrderRepository.find(stockMove.getOriginId());
      dummyInvoice.setCurrency(purchaseOrder.getCurrency());
      dummyInvoice.setPartner(purchaseOrder.getSupplierPartner());
      dummyInvoice.setCompany(purchaseOrder.getCompany());
      dummyInvoice.setTradingName(purchaseOrder.getTradingName());
      dummyInvoice.setPaymentCondition(purchaseOrder.getPaymentCondition());
      dummyInvoice.setPaymentMode(purchaseOrder.getPaymentMode());
      dummyInvoice.setContactPartner(purchaseOrder.getContactPartner());
      dummyInvoice.setPriceList(purchaseOrder.getPriceList());
      dummyInvoice.setInAti(purchaseOrder.getInAti());
    } else {
      dummyInvoice.setCurrency(stockMove.getCompany().getCurrency());
      dummyInvoice.setPartner(stockMove.getPartner());
      dummyInvoice.setCompany(stockMove.getCompany());
      dummyInvoice.setTradingName(stockMove.getTradingName());
      dummyInvoice.setAddress(stockMove.getFromAddress());
      dummyInvoice.setAddressStr(stockMove.getFromAddressStr());
    }
    return dummyInvoice;
  }

  /**
   * This method will throw an exception if a stock move has already been invoiced. The exception
   * message will give every already invoiced stock move.
   */
  protected void checkForAlreadyInvoicedStockMove(List<StockMove> stockMoveList)
      throws AxelorException {
    StringBuilder invoiceAlreadyGeneratedMessage = new StringBuilder();
    String message;

    for (StockMove stockMove : stockMoveList) {
      if (stockMove.getInvoiceSet() != null
          && stockMove
                  .getStockMoveLineList()
                  .stream()
                  .map(StockMoveLine::getQtyInvoiced)
                  .reduce(BigDecimal::add)
                  .orElse(BigDecimal.ZERO)
                  .compareTo(BigDecimal.ZERO)
              == 0) {
        String templateMessage;
        if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING) {
          templateMessage = IExceptionMessage.OUTGOING_STOCK_MOVE_INVOICE_EXISTS;
        } else {
          templateMessage = IExceptionMessage.INCOMING_STOCK_MOVE_INVOICE_EXISTS;
        }
        message = String.format(I18n.get(templateMessage), stockMove.getName());
        if (invoiceAlreadyGeneratedMessage.length() > 0) {
          invoiceAlreadyGeneratedMessage.append("<br/>");
        }
        invoiceAlreadyGeneratedMessage.append(message);
      }
    }

    if (invoiceAlreadyGeneratedMessage.length() > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, invoiceAlreadyGeneratedMessage.toString());
    }
  }

  /**
   * Try to complete a dummy invoice. If some fields are in conflict, empty them.
   *
   * @param dummyInvoice a dummy invoice used to store some fields that will be used to generate the
   *     real invoice.
   * @param stockMove a stock move to invoice.
   */
  protected void completeInvoiceInMultiOutgoingStockMove(
      Invoice dummyInvoice, StockMove stockMove) {

    if (stockMove.getOriginId() != null
        && StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      return;
    }

    Invoice comparedDummyInvoice = createDummyOutInvoice(stockMove);

    if (dummyInvoice.getPaymentCondition() != null
        && !dummyInvoice.getPaymentCondition().equals(comparedDummyInvoice.getPaymentCondition())) {
      dummyInvoice.setPaymentCondition(null);
    }

    if (dummyInvoice.getPaymentMode() != null
        && !dummyInvoice.getPaymentMode().equals(comparedDummyInvoice.getPaymentMode())) {
      dummyInvoice.setPaymentMode(null);
    }

    if (dummyInvoice.getAddress() != null
        && !dummyInvoice.getAddress().equals(comparedDummyInvoice.getAddress())) {
      dummyInvoice.setAddress(null);
      dummyInvoice.setAddressStr(null);
    }

    if (dummyInvoice.getContactPartner() != null
        && !dummyInvoice.getContactPartner().equals(comparedDummyInvoice.getContactPartner())) {
      dummyInvoice.setContactPartner(null);
    }

    if (dummyInvoice.getPriceList() != null
        && !dummyInvoice.getPriceList().equals(comparedDummyInvoice.getPriceList())) {
      dummyInvoice.setPriceList(null);
    }
  }

  /**
   * Try to complete a dummy invoice. If some fields are in conflict, empty them.
   *
   * @param dummyInvoice a dummy invoice used to store some fields that will be used to generate the
   *     real invoice.
   * @param stockMove a stock move to invoice.
   */
  protected void completeInvoiceInMultiIncomingStockMove(
      Invoice dummyInvoice, StockMove stockMove) {

    if (stockMove.getOriginId() != null
        && StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      return;
    }

    Invoice comparedDummyInvoice = createDummyInInvoice(stockMove);

    if (dummyInvoice.getPaymentCondition() != null
        && !dummyInvoice.getPaymentCondition().equals(comparedDummyInvoice.getPaymentCondition())) {
      dummyInvoice.setPaymentCondition(null);
    }

    if (dummyInvoice.getPaymentMode() != null
        && !dummyInvoice.getPaymentMode().equals(comparedDummyInvoice.getPaymentMode())) {
      dummyInvoice.setPaymentMode(null);
    }

    if (dummyInvoice.getContactPartner() != null
        && !dummyInvoice.getContactPartner().equals(comparedDummyInvoice.getContactPartner())) {
      dummyInvoice.setContactPartner(null);
    }

    if (dummyInvoice.getPriceList() != null
        && !dummyInvoice.getPriceList().equals(comparedDummyInvoice.getPriceList())) {
      dummyInvoice.setPriceList(null);
    }
  }

  /**
   * Fill external and internal reference in the given invoice, from the list of stock moves.
   *
   * @param stockMoveList
   * @param dummyInvoice
   */
  protected void fillReferenceInvoiceFromMultiOutStockMove(
      List<StockMove> stockMoveList, Invoice dummyInvoice) {
    // Concat sequence, internal ref and external ref from all saleOrder
    List<String> externalRefList = new ArrayList<>();
    List<String> internalRefList = new ArrayList<>();
    for (StockMove stockMove : stockMoveList) {
      SaleOrder saleOrder =
          StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())
                  && stockMove.getOriginId() != null
              ? saleOrderRepository.find(stockMove.getOriginId())
              : null;
      if (saleOrder != null) {
        externalRefList.add(saleOrder.getSaleOrderSeq());
      }
      internalRefList.add(
          stockMove.getStockMoveSeq()
              + (saleOrder != null ? (":" + saleOrder.getSaleOrderSeq()) : ""));
    }

    String externalRef = String.join("|", externalRefList);
    String internalRef = String.join("|", internalRefList);

    dummyInvoice.setExternalReference(StringTool.cutTooLongString(externalRef));
    dummyInvoice.setInternalReference(StringTool.cutTooLongString(internalRef));
  }

  /**
   * Fill external and internal reference in the given invoice, from the list of stock moves.
   *
   * @param stockMoveList
   * @param dummyInvoice
   */
  protected void fillReferenceInvoiceFromMultiInStockMove(
      List<StockMove> stockMoveList, Invoice dummyInvoice) {
    // Concat sequence, internal ref and external ref from all saleOrder

    List<String> externalRefList = new ArrayList<>();
    List<String> internalRefList = new ArrayList<>();
    for (StockMove stockMove : stockMoveList) {
      PurchaseOrder purchaseOrder =
          StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(stockMove.getOriginTypeSelect())
                  && stockMove.getOriginId() != null
              ? purchaseOrderRepository.find(stockMove.getOriginId())
              : null;
      if (purchaseOrder != null) {
        externalRefList.add(purchaseOrder.getPurchaseOrderSeq());
      }
      internalRefList.add(
          stockMove.getStockMoveSeq()
              + (purchaseOrder != null ? (":" + purchaseOrder.getPurchaseOrderSeq()) : ""));
    }

    String externalRef = String.join("|", externalRefList);
    String internalRef = String.join("|", internalRefList);

    dummyInvoice.setExternalReference(StringTool.cutTooLongString(externalRef));
    dummyInvoice.setInternalReference(StringTool.cutTooLongString(internalRef));
  }

  @Override
  public Invoice extendInternalReference(StockMove stockMove, Invoice invoice) {

    invoice.setInternalReference(
        StringTool.cutTooLongString(
            stockMove.getStockMoveSeq() + ":" + invoice.getInternalReference()));

    return invoice;
  }

  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<StockMoveLine> stockMoveLineList, Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

    Map<StockMoveLine, InvoiceLine> packLineMap = Maps.newHashMap();
    boolean setPack = Beans.get(AppSaleService.class).getAppSale().getProductPackMgt();
    for (StockMoveLine stockMoveLine : getConsolidatedStockMoveLineList(stockMoveLineList)) {

      List<InvoiceLine> invoiceLineListCreated = null;
      Long id = stockMoveLine.getId();
      if (qtyToInvoiceMap != null) {
        if (qtyToInvoiceMap.containsKey(id)) {
          invoiceLineListCreated =
              this.createInvoiceLine(invoice, stockMoveLine, qtyToInvoiceMap.get(id));
        }
      } else {
        invoiceLineListCreated =
            this.createInvoiceLine(
                invoice,
                stockMoveLine,
                stockMoveLine.getRealQty().subtract(stockMoveLine.getQtyInvoiced()));
      }

      if (invoiceLineListCreated != null) {
        invoiceLineList.addAll(invoiceLineListCreated);
        if (setPack
            && !invoiceLineListCreated.isEmpty()
            && (stockMoveLine.getLineTypeSelect() == StockMoveLineRepository.TYPE_PACK
                || stockMoveLine.getIsSubLine())) {
          packLineMap.put(stockMoveLine, invoiceLineListCreated.get(0));
        }
      }
      // Depending on stockMove type
      if (stockMoveLine.getSaleOrderLine() != null) {
        stockMoveLine.getSaleOrderLine().setInvoiced(true);
      } else if (stockMoveLine.getPurchaseOrderLine() != null) {
        stockMoveLine.getPurchaseOrderLine().setInvoiced(true);
      }
    }

    if (setPack) {
      for (StockMoveLine stockMoveLine : packLineMap.keySet()) {
        if (stockMoveLine.getLineTypeSelect() == StockMoveLineRepository.TYPE_PACK) {
          InvoiceLine invoiceLine = packLineMap.get(stockMoveLine);
          if (invoiceLine == null) {
            continue;
          }
          BigDecimal totalPack = BigDecimal.ZERO;
          for (StockMoveLine subLine : stockMoveLine.getSubLineList()) {
            InvoiceLine subInvoiceLine = packLineMap.get(subLine);
            if (subInvoiceLine != null) {
              totalPack = totalPack.add(subInvoiceLine.getExTaxTotal());
              subInvoiceLine.setParentLine(invoiceLine);
            }
          }
          invoiceLine.setTotalPack(totalPack);
        }
      }
    }

    return invoiceLineList;
  }

  @Override
  public List<InvoiceLine> createInvoiceLine(
      Invoice invoice, StockMoveLine stockMoveLine, BigDecimal qty) throws AxelorException {

    Product product = stockMoveLine.getProduct();
    boolean isTitleLine = false;

    int sequence = InvoiceLineGenerator.DEFAULT_SEQUENCE;
    SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
    PurchaseOrderLine purchaseOrderLine = stockMoveLine.getPurchaseOrderLine();

    if (saleOrderLine != null) {
      if (saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_PACK) {
        isTitleLine = true;
      }
      sequence = saleOrderLine.getSequence();
    } else if (purchaseOrderLine != null) {
      if (purchaseOrderLine.getIsTitleLine()) {
        isTitleLine = true;
      }
      sequence = purchaseOrderLine.getSequence();
    }

    if (stockMoveLine.getRealQty().compareTo(BigDecimal.ZERO) == 0 && !isTitleLine) {
      return new ArrayList<InvoiceLine>();
    }
    if (product == null && !isTitleLine) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.STOCK_MOVE_INVOICE_1),
          stockMoveLine.getStockMove().getStockMoveSeq());
    }

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGeneratorSupplyChain(
            invoice,
            product,
            stockMoveLine.getProductName(),
            stockMoveLine.getDescription(),
            qty,
            stockMoveLine.getUnit(),
            sequence,
            false,
            stockMoveLine.getSaleOrderLine(),
            stockMoveLine.getPurchaseOrderLine(),
            stockMoveLine,
            stockMoveLine.getIsSubLine(),
            stockMoveLine.getPackPriceSelect()) {
          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();

            List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    List<InvoiceLine> invoiceLines = invoiceLineGenerator.creates();
    for (InvoiceLine invoiceLine : invoiceLines) {
      invoiceLine.setStockMoveLine(stockMoveLine);
    }
    return invoiceLines;
  }

  /**
   * Get a list of stock move lines consolidated by parent line (sale or purchase order).
   *
   * @param stockMoveLineList
   * @return
   * @throws AxelorException
   */
  private List<StockMoveLine> getConsolidatedStockMoveLineList(
      List<StockMoveLine> stockMoveLineList) throws AxelorException {

    Map<SaleOrderLine, List<StockMoveLine>> stockMoveLineSaleMap = new LinkedHashMap<>();
    Map<PurchaseOrderLine, List<StockMoveLine>> stockMoveLinePurchaseMap = new LinkedHashMap<>();
    List<StockMoveLine> resultList = new ArrayList<>();

    List<StockMoveLine> list;
    for (StockMoveLine stockMoveLine : stockMoveLineList) {

      if (stockMoveLine.getSaleOrderLine() != null) {
        list = stockMoveLineSaleMap.get(stockMoveLine.getSaleOrderLine());
        if (list == null) {
          list = new ArrayList<>();
          stockMoveLineSaleMap.put(stockMoveLine.getSaleOrderLine(), list);
        }
        list.add(stockMoveLine);
      } else if (stockMoveLine.getPurchaseOrderLine() != null) {
        list = stockMoveLinePurchaseMap.get(stockMoveLine.getPurchaseOrderLine());
        if (list == null) {
          list = new ArrayList<>();
          stockMoveLinePurchaseMap.put(stockMoveLine.getPurchaseOrderLine(), list);
        }
        list.add(stockMoveLine);
      } else { // if the stock move line does not have a parent line (sale or purchase order line)
        resultList.add(stockMoveLine);
      }
    }

    for (List<StockMoveLine> stockMoveLines : stockMoveLineSaleMap.values()) {
      resultList.add(stockMoveLineServiceSupplychain.getMergedStockMoveLine(stockMoveLines));
    }
    for (List<StockMoveLine> stockMoveLines : stockMoveLinePurchaseMap.values()) {
      resultList.add(stockMoveLineServiceSupplychain.getMergedStockMoveLine(stockMoveLines));
    }
    return resultList;
  }

  @Override
  public List<Map<String, Object>> getStockMoveLinesToInvoice(StockMove stockMove) {
    List<Map<String, Object>> stockMoveLines = new ArrayList<Map<String, Object>>();

    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {

      if (stockMoveLine.getIsSubLine()) {
        continue;
      }
      BigDecimal qty = stockMoveLine.getRealQty().subtract(stockMoveLine.getQtyInvoiced());
      if (qty.compareTo(BigDecimal.ZERO) != 0) {
        Map<String, Object> stockMoveLineMap = new HashMap<>();
        stockMoveLineMap.put("productCode", stockMoveLine.getProduct().getCode());
        stockMoveLineMap.put("productName", stockMoveLine.getProductName());
        stockMoveLineMap.put("remainingQty", qty);
        stockMoveLineMap.put("realQty", stockMoveLine.getRealQty());
        stockMoveLineMap.put("qtyToInvoice", BigDecimal.ZERO);
        stockMoveLineMap.put("invoiceAll", false);
        stockMoveLineMap.put("isSubline", stockMoveLine.getIsSubLine());
        stockMoveLineMap.put("stockMoveLineId", stockMoveLine.getId());
        stockMoveLines.add(stockMoveLineMap);
      }
    }
    return stockMoveLines;
  }
}
