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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.invoice.RefundInvoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.address.AddressService;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.saleorder.merge.SaleOrderMergingServiceSupplyChain;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

public class StockMoveMultiInvoiceServiceImpl implements StockMoveMultiInvoiceService {

  protected final InvoiceRepository invoiceRepository;
  protected final SaleOrderRepository saleOrderRepository;
  protected final PurchaseOrderRepository purchaseOrderRepository;
  protected final StockMoveInvoiceService stockMoveInvoiceService;
  protected final AppStockService appStockService;
  protected final SaleOrderMergingServiceSupplyChain saleOrderMergingServiceSupplyChain;
  protected final PurchaseOrderMergingSupplychainService purchaseOrderMergingSupplychainService;

  @Inject
  public StockMoveMultiInvoiceServiceImpl(
      InvoiceRepository invoiceRepository,
      SaleOrderRepository saleOrderRepository,
      PurchaseOrderRepository purchaseOrderRepository,
      StockMoveInvoiceService stockMoveInvoiceService,
      AppStockService appStockService,
      SaleOrderMergingServiceSupplyChain saleOrderMergingServiceSupplyChain,
      PurchaseOrderMergingSupplychainService purchaseOrderMergingSupplychainService) {
    this.invoiceRepository = invoiceRepository;
    this.saleOrderRepository = saleOrderRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.stockMoveInvoiceService = stockMoveInvoiceService;
    this.appStockService = appStockService;
    this.saleOrderMergingServiceSupplyChain = saleOrderMergingServiceSupplyChain;
    this.purchaseOrderMergingSupplychainService = purchaseOrderMergingSupplychainService;
  }

  @Override
  public Entry<List<Long>, String> generateMultipleInvoices(List<Long> stockMoveIdList) {
    StockMoveRepository stockMoveRepository = Beans.get(StockMoveRepository.class);
    List<Long> invoiceIdList = new ArrayList<>();

    StringBuilder stockMovesInError = new StringBuilder();
    List<StockMove> stockMoveList;
    Query<StockMove> stockMoveQuery =
        stockMoveRepository
            .all()
            .filter("self.id IN :stockMoveIdList")
            .bind("stockMoveIdList", stockMoveIdList)
            .order("id");
    int offset = 0;

    while (!(stockMoveList = stockMoveQuery.fetch(AbstractBatch.FETCH_LIMIT, offset)).isEmpty()) {
      for (StockMove stockMove : stockMoveList) {
        offset++;
        try {
          Invoice invoice = stockMoveInvoiceService.createInvoice(stockMove, 0, null);
          if (invoice != null) {
            invoiceIdList.add(invoice.getId());
          }
        } catch (Exception e) {
          if (stockMovesInError.length() > 0) {
            stockMovesInError.append("<br/>");
          }
          stockMovesInError.append(
              String.format(
                  I18n.get(SupplychainExceptionMessage.STOCK_MOVE_GENERATE_INVOICE),
                  stockMove.getName(),
                  e.getLocalizedMessage()));
          break;
        }
      }
      JPA.clear();
    }

    return new SimpleImmutableEntry<>(invoiceIdList, stockMovesInError.toString());
  }

  @Override
  public Map<String, Object> areFieldsConflictedToGenerateCustInvoice(List<StockMove> stockMoveList)
      throws AxelorException {
    Map<String, Object> mapResult = new HashMap<>();

    boolean paymentConditionToCheck = false;
    boolean paymentModeToCheck = false;
    boolean contactPartnerToCheck = false;

    checkForAlreadyInvoicedStockMove(stockMoveList);

    List<Invoice> dummyInvoiceList = new ArrayList<>();
    for (StockMove stockMove : stockMoveList) {
      dummyInvoiceList.add(createDummyOutInvoice(stockMove));
    }
    checkOutStockMoveRequiredFieldsAreTheSame(dummyInvoiceList);

    if (!dummyInvoiceList.isEmpty()) {
      PaymentCondition firstPaymentCondition = dummyInvoiceList.get(0).getPaymentCondition();
      PaymentMode firstPaymentMode = dummyInvoiceList.get(0).getPaymentMode();
      Partner firstContactPartner = dummyInvoiceList.get(0).getContactPartner();
      paymentConditionToCheck =
          !dummyInvoiceList.stream()
              .map(Invoice::getPaymentCondition)
              .allMatch(
                  paymentCondition -> Objects.equals(paymentCondition, firstPaymentCondition));
      paymentModeToCheck =
          !dummyInvoiceList.stream()
              .map(Invoice::getPaymentMode)
              .allMatch(paymentMode -> Objects.equals(paymentMode, firstPaymentMode));
      contactPartnerToCheck =
          !dummyInvoiceList.stream()
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
            I18n.get(SupplychainExceptionMessage.STOCK_MOVE_MULTI_INVOICE_CURRENCY));
      }

      if (firstDummyInvoice.getPartner() != null
          && !firstDummyInvoice.getPartner().equals(dummyInvoice.getPartner())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.STOCK_MOVE_MULTI_INVOICE_CLIENT_PARTNER));
      }

      if (firstDummyInvoice.getCompany() != null
          && !firstDummyInvoice.getCompany().equals(dummyInvoice.getCompany())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.STOCK_MOVE_MULTI_INVOICE_COMPANY_SO));
      }

      if ((firstDummyInvoice.getTradingName() != null
              && !firstDummyInvoice.getTradingName().equals(dummyInvoice.getTradingName()))
          || (firstDummyInvoice.getTradingName() == null
              && dummyInvoice.getTradingName() != null)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.STOCK_MOVE_MULTI_INVOICE_TRADING_NAME_SO));
      }

      if (firstDummyInvoice.getInAti() != null
          && !firstDummyInvoice.getInAti().equals(dummyInvoice.getInAti())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.STOCK_MOVE_MULTI_INVOICE_IN_ATI));
      }

      if (appStockService.getAppStock().getIsIncotermEnabled()
          && firstDummyInvoice.getIncoterm() != null
          && !firstDummyInvoice.getIncoterm().equals(dummyInvoice.getIncoterm())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.STOCK_MOVE_MULTI_INVOICE_INCOTERM));
      }

      if ((firstDummyInvoice.getFiscalPosition() == null
              && dummyInvoice.getFiscalPosition() != null)
          || (firstDummyInvoice.getFiscalPosition() != null
              && !firstDummyInvoice.getFiscalPosition().equals(dummyInvoice.getFiscalPosition()))) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.STOCK_MOVE_MULTI_FISCAL_POSITION_SO));
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

    List<Invoice> dummyInvoiceList = new ArrayList<>();
    for (StockMove stockMove : stockMoveList) {
      dummyInvoiceList.add(createDummyInInvoice(stockMove));
    }
    checkInStockMoveRequiredFieldsAreTheSame(dummyInvoiceList);

    if (!dummyInvoiceList.isEmpty()) {
      PaymentCondition firstPaymentCondition = dummyInvoiceList.get(0).getPaymentCondition();
      PaymentMode firstPaymentMode = dummyInvoiceList.get(0).getPaymentMode();
      Partner firstContactPartner = dummyInvoiceList.get(0).getContactPartner();
      paymentConditionToCheck =
          !dummyInvoiceList.stream()
              .map(Invoice::getPaymentCondition)
              .allMatch(
                  paymentCondition -> Objects.equals(paymentCondition, firstPaymentCondition));
      paymentModeToCheck =
          !dummyInvoiceList.stream()
              .map(Invoice::getPaymentMode)
              .allMatch(paymentMode -> Objects.equals(paymentMode, firstPaymentMode));
      contactPartnerToCheck =
          !dummyInvoiceList.stream()
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
            I18n.get(SupplychainExceptionMessage.STOCK_MOVE_MULTI_INVOICE_CURRENCY));
      }

      if (firstDummyInvoice.getPartner() != null
          && !firstDummyInvoice.getPartner().equals(dummyInvoice.getPartner())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.STOCK_MOVE_MULTI_INVOICE_SUPPLIER_PARTNER));
      }

      if (firstDummyInvoice.getCompany() != null
          && !firstDummyInvoice.getCompany().equals(dummyInvoice.getCompany())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.STOCK_MOVE_MULTI_INVOICE_COMPANY_PO));
      }

      if ((firstDummyInvoice.getTradingName() != null
              && !firstDummyInvoice.getTradingName().equals(dummyInvoice.getTradingName()))
          || (firstDummyInvoice.getTradingName() == null
              && dummyInvoice.getTradingName() != null)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.STOCK_MOVE_MULTI_INVOICE_TRADING_NAME_PO));
      }

      if (firstDummyInvoice.getInAti() != null
          && !firstDummyInvoice.getInAti().equals(dummyInvoice.getInAti())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.STOCK_MOVE_MULTI_INVOICE_IN_ATI));
      }
      if (appStockService.getAppStock().getIsIncotermEnabled()
          && firstDummyInvoice.getIncoterm() != null
          && !firstDummyInvoice.getIncoterm().equals(dummyInvoice.getIncoterm())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.STOCK_MOVE_MULTI_INVOICE_INCOTERM));
      }

      if ((firstDummyInvoice.getFiscalPosition() == null
              && dummyInvoice.getFiscalPosition() != null)
          || (firstDummyInvoice.getFiscalPosition() != null
              && !firstDummyInvoice.getFiscalPosition().equals(dummyInvoice.getFiscalPosition()))) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.STOCK_MOVE_MULTI_FISCAL_POSITION_PO));
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
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
  @Transactional(rollbackOn = {Exception.class})
  public Optional<Invoice> createInvoiceFromMultiOutgoingStockMove(List<StockMove> stockMoveList)
      throws AxelorException {

    if (stockMoveList == null || stockMoveList.isEmpty()) {
      return Optional.empty();
    }

    Set<Address> deliveryAddressSet = new HashSet<>();

    // create dummy invoice from the first stock move
    Invoice dummyInvoice = createDummyOutInvoice(stockMoveList.get(0));

    // Check if field constraints are respected
    for (StockMove stockMove : stockMoveList) {
      completeInvoiceInMultiOutgoingStockMove(dummyInvoice, stockMove);
      if (stockMove.getToAddressStr() != null) {
        deliveryAddressSet.add(stockMove.getToAddress());
      }
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
            dummyInvoice.getTradingName(),
            dummyInvoice.getGroupProductsOnPrintings(),
            dummyInvoice.getCompanyTaxNumber()) {

          @Override
          public Invoice generate() throws AxelorException {
            Invoice invoice = super.createInvoiceHeader();
            invoice.setPartnerTaxNbr(partner.getTaxNbr());

            return invoice;
          }
        };

    Invoice invoice = invoiceGenerator.generate();
    invoice.setAddressStr(dummyInvoice.getAddressStr());
    if (appStockService.getAppStock().getIsIncotermEnabled()) {
      invoice.setIncoterm(dummyInvoice.getIncoterm());
    }
    invoice.setFiscalPosition(dummyInvoice.getFiscalPosition());

    StringBuilder deliveryAddressStr = new StringBuilder();
    AddressService addressService = Beans.get(AddressService.class);

    for (Address address : deliveryAddressSet) {
      deliveryAddressStr.append(
          addressService.computeAddressStr(address).replace("\n", ", ") + "\n");
    }

    invoice.setDeliveryAddressStr(deliveryAddressStr.toString());

    fillInvoiceCommentsFromMultiOutStockMove(stockMoveList, invoice);

    List<InvoiceLine> invoiceLineList = new ArrayList<>();

    for (StockMove stockMoveLocal : stockMoveList) {

      stockMoveInvoiceService.checkSplitSalePartiallyInvoicedStockMoveLines(
          stockMoveLocal, stockMoveLocal.getStockMoveLineList());
      List<InvoiceLine> createdInvoiceLines =
          stockMoveInvoiceService.createInvoiceLines(
              invoice, stockMoveLocal, stockMoveLocal.getStockMoveLineList(), null);
      if (stockMoveLocal.getTypeSelect() == StockMoveRepository.TYPE_INCOMING) {
        createdInvoiceLines.forEach(this::negateInvoiceLinePrice);
      }
      invoiceLineList.addAll(createdInvoiceLines);
    }

    invoiceGenerator.populate(invoice, invoiceLineList);

    invoiceRepository.save(invoice);
    invoice = toPositivePriceInvoice(invoice);
    if (invoice.getExTaxTotal().signum() == 0
        && stockMoveList.stream().allMatch(StockMove::getIsReversion)) {
      invoice.setOperationTypeSelect(InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND);
    }
    AccountConfig accountConfig =
        Beans.get(AccountConfigService.class).getAccountConfig(invoice.getCompany());
    invoice.setDisplayStockMoveOnInvoicePrinting(
        accountConfig.getDisplayStockMoveOnInvoicePrinting());
    stockMoveList.forEach(invoice::addStockMoveSetItem);
    return Optional.of(invoice);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
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
  @Transactional(rollbackOn = {Exception.class})
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
            dummyInvoice.getTradingName(),
            null,
            dummyInvoice.getCompanyTaxNumber()) {

          @Override
          public Invoice generate() throws AxelorException {

            return super.createInvoiceHeader();
          }
        };

    Invoice invoice = invoiceGenerator.generate();
    invoice.setAddressStr(dummyInvoice.getAddressStr());
    if (appStockService.getAppStock().getIsIncotermEnabled()) {
      invoice.setIncoterm(dummyInvoice.getIncoterm());
    }
    invoice.setFiscalPosition(dummyInvoice.getFiscalPosition());

    List<InvoiceLine> invoiceLineList = new ArrayList<>();

    for (StockMove stockMoveLocal : stockMoveList) {
      List<InvoiceLine> createdInvoiceLines =
          stockMoveInvoiceService.createInvoiceLines(
              invoice, stockMoveLocal, stockMoveLocal.getStockMoveLineList(), null);
      if (stockMoveLocal.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING) {
        createdInvoiceLines.forEach(this::negateInvoiceLinePrice);
      }
      invoiceLineList.addAll(createdInvoiceLines);
    }

    invoiceGenerator.populate(invoice, invoiceLineList);

    invoiceRepository.save(invoice);
    invoice = toPositivePriceInvoice(invoice);
    if (invoice.getExTaxTotal().signum() == 0
        && stockMoveList.stream().allMatch(StockMove::getIsReversion)) {
      invoice.setOperationTypeSelect(InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND);
    }
    stockMoveList.forEach(invoice::addStockMoveSetItem);
    return Optional.of(invoice);
  }

  /** For any invoice, if ex tax total is negative, returns the corresponding refund invoice. */
  protected Invoice toPositivePriceInvoice(Invoice invoice) throws AxelorException {
    if (invoice.getExTaxTotal().signum() < 0) {
      Invoice refund = transformToRefund(invoice);
      invoice.setStatusSelect(InvoiceRepository.STATUS_CANCELED);
      invoiceRepository.remove(invoice);
      return refund;
    } else {
      return invoice;
    }
  }

  /**
   * Change the operation type and invert all prices in the invoice.
   *
   * @param invoice an invoice
   * @return the refund invoice
   */
  protected Invoice transformToRefund(Invoice invoice) throws AxelorException {
    Invoice refund = new RefundInvoice(invoice).generate();
    if (refund.getInvoiceLineList() != null) {
      for (InvoiceLine invoiceLine : refund.getInvoiceLineList()) {
        invoiceLine.setQty(invoiceLine.getQty().negate());
        invoiceLine.setOldQty(invoiceLine.getOldQty().negate());
        invoiceLine.setExTaxTotal(invoiceLine.getExTaxTotal().negate());
        invoiceLine.setInTaxTotal(invoiceLine.getInTaxTotal().negate());
        invoiceLine.setCompanyExTaxTotal(invoiceLine.getCompanyExTaxTotal().negate());
        invoiceLine.setCompanyInTaxTotal(invoiceLine.getCompanyInTaxTotal().negate());
      }
    }
    if (refund.getInvoiceLineTaxList() != null) {
      for (InvoiceLineTax invoiceLineTax : refund.getInvoiceLineTaxList()) {
        invoiceLineTax.setExTaxBase(invoiceLineTax.getExTaxBase().negate());
        invoiceLineTax.setTaxTotal(invoiceLineTax.getTaxTotal().negate());
        invoiceLineTax.setCompanyExTaxBase(invoiceLineTax.getCompanyExTaxBase().negate());
        invoiceLineTax.setInTaxTotal(invoiceLineTax.getInTaxTotal().negate());
        invoiceLineTax.setCompanyInTaxTotal(invoiceLineTax.getCompanyInTaxTotal().negate());
      }
    }
    refund.setExTaxTotal(refund.getExTaxTotal().negate());
    refund.setInTaxTotal(refund.getInTaxTotal().negate());
    refund.setCompanyExTaxTotal(refund.getCompanyExTaxTotal().negate());
    refund.setCompanyInTaxTotal(refund.getCompanyInTaxTotal().negate());
    refund.setTaxTotal(refund.getTaxTotal().negate());
    refund.setAmountRemaining(refund.getAmountRemaining().negate());
    refund.setCompanyTaxTotal(refund.getCompanyTaxTotal().negate());
    refund.setPaymentMode(InvoiceToolService.getPaymentMode(refund));
    return invoiceRepository.save(refund);
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
   * @throws AxelorException
   */
  protected Invoice createDummyOutInvoice(StockMove stockMove) throws AxelorException {
    Invoice dummyInvoice = new Invoice();

    if (ObjectUtils.notEmpty(stockMove.getSaleOrderSet())) {
      SaleOrder saleOrder = saleOrderMergingServiceSupplyChain.getDummyMergedSaleOrder(stockMove);
      Partner invoicedPartner = saleOrder.getInvoicedPartner();
      if (invoicedPartner == null) {
        if (stockMove.getInvoicedPartner() != null) {
          invoicedPartner = stockMove.getInvoicedPartner();
        } else {
          invoicedPartner = saleOrder.getClientPartner();
        }
      }
      dummyInvoice.setCurrency(saleOrder.getCurrency());
      dummyInvoice.setPartner(invoicedPartner);
      dummyInvoice.setCompany(saleOrder.getCompany());
      dummyInvoice.setTradingName(saleOrder.getTradingName());
      dummyInvoice.setPaymentCondition(saleOrder.getPaymentCondition());
      dummyInvoice.setPaymentMode(saleOrder.getPaymentMode());
      dummyInvoice.setAddress(saleOrder.getMainInvoicingAddress());
      dummyInvoice.setAddressStr(saleOrder.getMainInvoicingAddressStr());
      dummyInvoice.setContactPartner(saleOrder.getContactPartner());
      dummyInvoice.setPriceList(saleOrder.getPriceList());
      dummyInvoice.setInAti(saleOrder.getInAti());
      dummyInvoice.setGroupProductsOnPrintings(saleOrder.getGroupProductsOnPrintings());
      if (appStockService.getAppStock().getIsIncotermEnabled()) {
        dummyInvoice.setIncoterm(saleOrder.getIncoterm());
      }
      dummyInvoice.setFiscalPosition(saleOrder.getFiscalPosition());
    } else {
      dummyInvoice.setCurrency(stockMove.getCompany().getCurrency());
      dummyInvoice.setPartner(
          stockMove.getInvoicedPartner() != null
              ? stockMove.getInvoicedPartner()
              : stockMove.getPartner());
      dummyInvoice.setCompany(stockMove.getCompany());
      dummyInvoice.setTradingName(stockMove.getTradingName());
      dummyInvoice.setAddress(stockMove.getToAddress());
      dummyInvoice.setAddressStr(stockMove.getToAddressStr());
      dummyInvoice.setGroupProductsOnPrintings(stockMove.getGroupProductsOnPrintings());
      if (appStockService.getAppStock().getIsIncotermEnabled()) {
        dummyInvoice.setIncoterm(stockMove.getIncoterm());
      }
    }
    return dummyInvoice;
  }

  /**
   * Create a dummy invoice to hold fields used to generate the invoice which will be saved.
   *
   * @param stockMove an in stock move.
   * @return the created dummy invoice.
   * @throws AxelorException
   */
  protected Invoice createDummyInInvoice(StockMove stockMove) throws AxelorException {
    Invoice dummyInvoice = new Invoice();

    Set<PurchaseOrder> purchaseOrderSet = stockMove.getPurchaseOrderSet();
    if (ObjectUtils.notEmpty(purchaseOrderSet)) {
      PurchaseOrder purchaseOrder =
          purchaseOrderMergingSupplychainService.getDummyMergedPurchaseOrder(stockMove);
      dummyInvoice.setCurrency(purchaseOrder.getCurrency());
      dummyInvoice.setPartner(purchaseOrder.getSupplierPartner());
      dummyInvoice.setCompany(purchaseOrder.getCompany());
      dummyInvoice.setTradingName(purchaseOrder.getTradingName());
      dummyInvoice.setPaymentCondition(purchaseOrder.getPaymentCondition());
      dummyInvoice.setPaymentMode(purchaseOrder.getPaymentMode());
      dummyInvoice.setContactPartner(purchaseOrder.getContactPartner());
      dummyInvoice.setPriceList(purchaseOrder.getPriceList());
      dummyInvoice.setInAti(purchaseOrder.getInAti());
      dummyInvoice.setFiscalPosition(purchaseOrder.getFiscalPosition());
    } else {
      if (ObjectUtils.notEmpty(stockMove.getSaleOrderSet())) {
        SaleOrder saleOrder = saleOrderMergingServiceSupplyChain.getDummyMergedSaleOrder(stockMove);
        if (appStockService.getAppStock().getIsIncotermEnabled()) {
          dummyInvoice.setIncoterm(saleOrder.getIncoterm());
        }
        dummyInvoice.setFiscalPosition(saleOrder.getFiscalPosition());
      }
      dummyInvoice.setCurrency(stockMove.getCompany().getCurrency());
      dummyInvoice.setPartner(stockMove.getPartner());
      dummyInvoice.setCompany(stockMove.getCompany());
      dummyInvoice.setTradingName(stockMove.getTradingName());
      dummyInvoice.setAddress(stockMove.getFromAddress());
      dummyInvoice.setAddressStr(stockMove.getFromAddressStr());
      if (appStockService.getAppStock().getIsIncotermEnabled()) {
        dummyInvoice.setIncoterm(stockMove.getIncoterm());
      }
    }
    return dummyInvoice;
  }

  /**
   * This method will throw an exception if a stock move has already been invoiced. The exception
   * message will give every already invoiced stock move.
   */
  @Override
  public void checkForAlreadyInvoicedStockMove(List<StockMove> stockMoveList)
      throws AxelorException {
    StringBuilder invoiceAlreadyGeneratedMessage = new StringBuilder();

    for (StockMove stockMove : stockMoveList) {
      try {
        checkIfAlreadyInvoiced(stockMove);
      } catch (AxelorException e) {
        if (invoiceAlreadyGeneratedMessage.length() > 0) {
          invoiceAlreadyGeneratedMessage.append("<br/>");
        }
        invoiceAlreadyGeneratedMessage.append(e.getMessage());
      }
    }
    if (invoiceAlreadyGeneratedMessage.length() > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, invoiceAlreadyGeneratedMessage.toString());
    }
  }

  /** This method will throw an exception if the given stock move is already invoiced. */
  protected void checkIfAlreadyInvoiced(StockMove stockMove) throws AxelorException {
    if (stockMove.getInvoiceSet() != null
        && stockMove.getInvoiceSet().stream()
            .anyMatch(invoice -> invoice.getStatusSelect() != InvoiceRepository.STATUS_CANCELED)) {
      String templateMessage;
      if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING) {
        templateMessage = SupplychainExceptionMessage.OUTGOING_STOCK_MOVE_INVOICE_EXISTS;
      } else {
        templateMessage = SupplychainExceptionMessage.INCOMING_STOCK_MOVE_INVOICE_EXISTS;
      }
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(templateMessage),
          stockMove.getName());
    }
  }

  /**
   * Try to complete a dummy invoice. If some fields are in conflict, empty them.
   *
   * @param dummyInvoice a dummy invoice used to store some fields that will be used to generate the
   *     real invoice.
   * @param stockMove a stock move to invoice.
   * @throws AxelorException
   */
  protected void completeInvoiceInMultiOutgoingStockMove(Invoice dummyInvoice, StockMove stockMove)
      throws AxelorException {

    if (ObjectUtils.notEmpty(stockMove.getSaleOrderSet())) {
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
   * @throws AxelorException
   */
  protected void completeInvoiceInMultiIncomingStockMove(Invoice dummyInvoice, StockMove stockMove)
      throws AxelorException {

    if (ObjectUtils.notEmpty(stockMove.getPurchaseOrderSet())) {
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

    if (dummyInvoice.getFiscalPosition() != null
        && !dummyInvoice.getFiscalPosition().equals(comparedDummyInvoice.getFiscalPosition())) {
      dummyInvoice.setFiscalPosition(null);
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
    StringJoiner externalRef = new StringJoiner("|");
    StringJoiner internalRef = new StringJoiner("|");

    for (StockMove stockMove : stockMoveList) {
      Set<SaleOrder> saleOrderSet = stockMove.getSaleOrderSet();

      if (ObjectUtils.isEmpty(saleOrderSet)) {
        continue;
      }
      String externalReference =
          stockMoveInvoiceService.fillExternalReferenceInvoiceFromOutStockMove(saleOrderSet);
      if (StringUtils.notEmpty(externalReference)) {
        externalRef.add(externalReference);
      }
      String internalReference =
          stockMoveInvoiceService.fillInternalReferenceInvoiceFromOutStockMove(
              stockMove, saleOrderSet);
      if (StringUtils.notEmpty(internalReference)) {
        internalRef.add(internalReference);
      }
    }
    dummyInvoice.setExternalReference(externalRef.toString());
    dummyInvoice.setInternalReference(internalRef.toString());
  }

  /**
   * Fill external and internal reference in the given invoice, from the list of stock moves.
   *
   * @param stockMoveList
   * @param dummyInvoice
   */
  protected void fillReferenceInvoiceFromMultiInStockMove(
      List<StockMove> stockMoveList, Invoice dummyInvoice) {
    // Concat sequence, internal ref and external ref from all purchaseOrder
    StringJoiner externalRef = new StringJoiner("|");
    StringJoiner internalRef = new StringJoiner("|");

    for (StockMove stockMove : stockMoveList) {
      Set<PurchaseOrder> purchaseOrderSet = stockMove.getPurchaseOrderSet();

      if (ObjectUtils.isEmpty(purchaseOrderSet)) {
        continue;
      }
      String externalReference =
          stockMoveInvoiceService.fillExternalReferenceInvoiceFromInStockMove(purchaseOrderSet);
      if (StringUtils.notEmpty(externalReference)) {
        externalRef.add(externalReference);
      }
      String internalReference =
          stockMoveInvoiceService.fillInternalReferenceInvoiceFromInStockMove(
              stockMove, purchaseOrderSet);
      if (StringUtils.notEmpty(internalReference)) {
        internalRef.add(internalReference);
      }
    }
    dummyInvoice.setExternalReference(externalRef.toString());
    dummyInvoice.setInternalReference(internalRef.toString());
  }

  /**
   * Negate all price fields in invoice line.
   *
   * @param invoiceLine
   */
  protected void negateInvoiceLinePrice(InvoiceLine invoiceLine) {

    invoiceLine.setQty(invoiceLine.getQty().negate());
    invoiceLine.setOldQty(invoiceLine.getOldQty().negate());

    // totals
    invoiceLine.setInTaxTotal(invoiceLine.getInTaxTotal().negate());
    invoiceLine.setCompanyInTaxTotal(invoiceLine.getCompanyInTaxTotal().negate());
    invoiceLine.setExTaxTotal(invoiceLine.getExTaxTotal().negate());
    invoiceLine.setCompanyExTaxTotal(invoiceLine.getCompanyExTaxTotal().negate());
  }

  protected void fillInvoiceCommentsFromMultiOutStockMove(
      List<StockMove> stockMoveList, Invoice invoice) {
    StringBuilder notes = new StringBuilder();
    StringBuilder proformaComments = new StringBuilder();

    for (StockMove stockMove : stockMoveList) {
      Set<SaleOrder> saleOrderSet = stockMove.getSaleOrderSet();

      if (ObjectUtils.isEmpty(saleOrderSet)) {
        continue;
      }
      String note = stockMoveInvoiceService.fillInvoiceNoteFromOutStockMove(saleOrderSet);
      if (StringUtils.notEmpty(note)) {
        notes.append(note);
      }
      String proformaComment =
          stockMoveInvoiceService.fillInvoiceProformaCommentsFromOutStockMove(saleOrderSet);
      if (StringUtils.notEmpty(proformaComment)) {
        proformaComments.append(proformaComment);
      }
    }
    invoice.setNote(notes.toString());
    invoice.setProformaComments(proformaComments.toString());
  }
}
