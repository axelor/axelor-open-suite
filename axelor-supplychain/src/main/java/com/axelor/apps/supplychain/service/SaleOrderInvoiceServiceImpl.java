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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderLineTax;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowService;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.apps.supplychain.db.repo.TimetableRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.listener.InvoicingStateCache;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceGeneratorSupplyChain;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineGeneratorSupplyChain;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineOrderService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderInvoiceServiceImpl implements SaleOrderInvoiceService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppBaseService appBaseService;

  protected AppSupplychainService appSupplychainService;

  protected SaleOrderRepository saleOrderRepo;

  protected InvoiceRepository invoiceRepo;

  protected InvoiceServiceSupplychainImpl invoiceService;

  protected SaleOrderLineService saleOrderLineService;

  protected StockMoveRepository stockMoveRepository;

  protected SaleOrderWorkflowService saleOrderWorkflowService;

  protected InvoiceTermService invoiceTermService;
  protected CommonInvoiceService commonInvoiceService;
  protected InvoiceLineOrderService invoiceLineOrderService;
  protected SaleInvoicingStateService saleInvoicingStateService;

  @Inject
  public SaleOrderInvoiceServiceImpl(
      AppBaseService appBaseService,
      AppSupplychainService appSupplychainService,
      SaleOrderRepository saleOrderRepo,
      InvoiceRepository invoiceRepo,
      InvoiceServiceSupplychainImpl invoiceService,
      SaleOrderLineService saleOrderLineService,
      StockMoveRepository stockMoveRepository,
      InvoiceTermService invoiceTermService,
      SaleOrderWorkflowService saleOrderWorkflowService,
      CommonInvoiceService commonInvoiceService,
      InvoiceLineOrderService invoiceLineOrderService,
      SaleInvoicingStateService saleInvoicingStateService) {

    this.appBaseService = appBaseService;
    this.appSupplychainService = appSupplychainService;
    this.saleOrderRepo = saleOrderRepo;
    this.invoiceRepo = invoiceRepo;
    this.invoiceService = invoiceService;
    this.stockMoveRepository = stockMoveRepository;
    this.saleOrderLineService = saleOrderLineService;
    this.invoiceTermService = invoiceTermService;
    this.saleOrderWorkflowService = saleOrderWorkflowService;
    this.commonInvoiceService = commonInvoiceService;
    this.invoiceLineOrderService = invoiceLineOrderService;
    this.saleInvoicingStateService = saleInvoicingStateService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice generateInvoice(
      SaleOrder saleOrder,
      int operationSelect,
      BigDecimal amount,
      boolean isPercent,
      Map<Long, BigDecimal> qtyToInvoiceMap,
      List<Long> timetableIdList)
      throws AxelorException {

    Invoice invoice;
    switch (operationSelect) {
      case SaleOrderRepository.INVOICE_ALL:
        invoice = generateInvoice(saleOrder);
        break;

      case SaleOrderRepository.INVOICE_LINES:
        invoice = generateInvoiceFromLines(saleOrder, qtyToInvoiceMap, isPercent);
        break;

      case SaleOrderRepository.INVOICE_ADVANCE_PAYMENT:
        invoice = generateAdvancePayment(saleOrder, amount, isPercent);
        break;

      case SaleOrderRepository.INVOICE_TIMETABLES:
        BigDecimal percentSum = BigDecimal.ZERO;
        TimetableRepository timetableRepo = Beans.get(TimetableRepository.class);
        List<Timetable> timetableList = new ArrayList<>();
        if (timetableIdList == null || timetableIdList.isEmpty()) {
          throw new AxelorException(
              saleOrder,
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(SupplychainExceptionMessage.SO_INVOICE_NO_TIMETABLES_SELECTED));
        }
        for (Long timetableId : timetableIdList) {
          Timetable timetable = timetableRepo.find(timetableId);
          timetableList.add(timetable);
          percentSum = percentSum.add(timetable.getPercentage());
        }
        invoice =
            generateInvoiceFromLines(
                saleOrder, this.generateQtyToInvoiceMap(saleOrder, percentSum), true);

        if (!timetableList.isEmpty()) {
          for (Timetable timetable : timetableList) {
            timetable.setInvoice(invoice);
            timetableRepo.save(timetable);
          }
        }

        break;

      default:
        return null;
    }
    invoice.setSaleOrder(saleOrder);

    if (!Strings.isNullOrEmpty(saleOrder.getInvoiceComments())) {
      invoice.setNote(saleOrder.getInvoiceComments());
    }

    if (ObjectUtils.isEmpty(invoice.getProformaComments())
        && !Strings.isNullOrEmpty(saleOrder.getProformaComments())) {
      invoice.setProformaComments(saleOrder.getProformaComments());
    }

    // fill default advance payment invoice
    if (invoice.getOperationSubTypeSelect() != InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
      invoice.setAdvancePaymentInvoiceSet(invoiceService.getDefaultAdvancePaymentInvoice(invoice));
    }

    invoice.setHideDiscount(saleOrder.getHideDiscount());

    invoice.setPartnerTaxNbr(saleOrder.getClientPartner().getTaxNbr());

    invoiceTermService.computeInvoiceTerms(invoice);
    invoice.setIncoterm(saleOrder.getIncoterm());
    invoice = invoiceRepo.save(invoice);

    return invoice;
  }

  private Map<Long, BigDecimal> generateQtyToInvoiceMap(
      SaleOrder saleOrder, BigDecimal percentage) {
    Map<Long, BigDecimal> map = new HashMap<>();

    for (SaleOrderLine soLine : saleOrder.getSaleOrderLineList()) {
      map.put(soLine.getId(), percentage);
    }
    return map;
  }

  @Override
  public BigDecimal computeAmountToInvoicePercent(
      SaleOrder saleOrder, BigDecimal amount, boolean isPercent) throws AxelorException {
    BigDecimal total = Beans.get(SaleOrderComputeService.class).getTotalSaleOrderPrice(saleOrder);
    return commonInvoiceService.computeAmountToInvoicePercent(saleOrder, amount, isPercent, total);
  }

  @Override
  public BigDecimal computeAmountToInvoice(
      BigDecimal amountToInvoice,
      int operationSelect,
      SaleOrder saleOrder,
      Map<Long, BigDecimal> qtyToInvoiceMap,
      Map<Long, BigDecimal> priceMap,
      Map<Long, BigDecimal> qtyMap,
      boolean isPercent) {

    if (operationSelect == SaleOrderRepository.INVOICE_LINES) {
      amountToInvoice = BigDecimal.ZERO;
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        Long saleOrderLineId = saleOrderLine.getId();
        if (qtyToInvoiceMap.containsKey(saleOrderLineId) && priceMap.containsKey(saleOrderLineId)) {
          BigDecimal qtyToInvoice = qtyToInvoiceMap.get(saleOrderLineId);

          if (isPercent) {
            qtyToInvoice =
                (qtyToInvoice.multiply(qtyMap.get(saleOrderLineId)))
                    .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
          }
          amountToInvoice =
              amountToInvoice.add(qtyToInvoice.multiply(priceMap.get(saleOrderLineId)));
        }
      }
    }
    return amountToInvoice;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice generateAdvancePayment(
      SaleOrder saleOrder, BigDecimal amountToInvoice, boolean isPercent) throws AxelorException {
    List<SaleOrderLineTax> taxLineList = saleOrder.getSaleOrderLineTaxList();
    AccountConfigService accountConfigService = Beans.get(AccountConfigService.class);

    BigDecimal percentToInvoice =
        computeAmountToInvoicePercent(saleOrder, amountToInvoice, isPercent);
    Product invoicingProduct =
        accountConfigService.getAccountConfig(saleOrder.getCompany()).getAdvancePaymentProduct();
    Account advancePaymentAccount =
        accountConfigService.getAccountConfig(saleOrder.getCompany()).getAdvancePaymentAccount();
    if (invoicingProduct == null) {
      throw new AxelorException(
          saleOrder,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SupplychainExceptionMessage.SO_INVOICE_MISSING_ADVANCE_PAYMENT_PRODUCT));
    }
    if (advancePaymentAccount == null) {
      throw new AxelorException(
          saleOrder,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SupplychainExceptionMessage.SO_INVOICE_MISSING_ADVANCE_PAYMENT_ACCOUNT),
          saleOrder.getCompany().getName());
    }

    Invoice invoice =
        createInvoiceAndLines(
            saleOrder,
            taxLineList,
            invoicingProduct,
            percentToInvoice,
            InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE,
            advancePaymentAccount);

    // no need for link to sale order lines for an advance payment
    if (invoice.getInvoiceLineList() != null) {
      invoice.getInvoiceLineList().forEach(invoiceLine -> invoiceLine.setSaleOrderLine(null));
    }

    return invoiceRepo.save(invoice);
  }

  public Invoice createInvoiceAndLines(
      SaleOrder saleOrder,
      List<SaleOrderLineTax> taxLineList,
      Product invoicingProduct,
      BigDecimal percentToInvoice,
      int operationSubTypeSelect,
      Account partnerAccount)
      throws AxelorException {
    InvoiceGenerator invoiceGenerator = this.createInvoiceGenerator(saleOrder);

    Invoice invoice = invoiceGenerator.generate();

    List<InvoiceLine> invoiceLinesList =
        (taxLineList != null && !taxLineList.isEmpty())
            ? this.createInvoiceLinesFromTax(
                invoice, taxLineList, invoicingProduct, percentToInvoice)
            : commonInvoiceService.createInvoiceLinesFromOrder(
                invoice, saleOrder.getInTaxTotal(), invoicingProduct, percentToInvoice);

    invoiceGenerator.populate(invoice, invoiceLinesList);

    invoice.setAddressStr(saleOrder.getMainInvoicingAddressStr());
    invoice.setDeliveryAddressStr(saleOrder.getDeliveryAddressStr());

    invoice.setOperationSubTypeSelect(operationSubTypeSelect);

    if (partnerAccount != null) {
      Partner partner = invoice.getPartner();
      FiscalPosition fiscalPosition = null;
      if (saleOrder != null) {
        fiscalPosition = saleOrder.getFiscalPosition();
      }
      if (fiscalPosition != null) {
        partnerAccount =
            Beans.get(FiscalPositionAccountService.class)
                .getAccount(fiscalPosition, partnerAccount);
      }
      invoice.setPartnerAccount(partnerAccount);
    }

    return invoice;
  }

  @Override
  public List<InvoiceLine> createInvoiceLinesFromTax(
      Invoice invoice,
      List<SaleOrderLineTax> taxLineList,
      Product invoicingProduct,
      BigDecimal percentToInvoice)
      throws AxelorException {

    List<InvoiceLine> createdInvoiceLineList = new ArrayList<>();
    if (taxLineList != null) {
      for (SaleOrderLineTax saleOrderLineTax : taxLineList) {
        InvoiceLineGenerator invoiceLineGenerator =
            invoiceLineOrderService.getInvoiceLineGeneratorWithComputedTaxPrice(
                invoice, invoicingProduct, percentToInvoice, saleOrderLineTax);

        List<InvoiceLine> invoiceOneLineList = invoiceLineGenerator.creates();
        // link to the created invoice line the first line of the sale order.
        for (InvoiceLine invoiceLine : invoiceOneLineList) {
          SaleOrderLine saleOrderLine =
              saleOrderLineTax.getSaleOrder().getSaleOrderLineList().get(0);
          invoiceLine.setSaleOrderLine(saleOrderLine);
        }
        createdInvoiceLineList.addAll(invoiceOneLineList);
      }
    }
    return createdInvoiceLineList;
  }

  @Override
  public Invoice generateInvoiceFromLines(
      SaleOrder saleOrder, Map<Long, BigDecimal> qtyToInvoiceMap, boolean isPercent)
      throws AxelorException {

    if (qtyToInvoiceMap.isEmpty()) {
      throw new AxelorException(
          saleOrder,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.SO_INVOICE_NO_LINES_SELECTED));
    }

    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      Long SOrderId = saleOrderLine.getId();
      if (qtyToInvoiceMap.containsKey(SOrderId)) {
        if (isPercent) {
          BigDecimal percent = qtyToInvoiceMap.get(SOrderId);
          BigDecimal realQty =
              saleOrderLine
                  .getQty()
                  .multiply(percent)
                  .divide(
                      new BigDecimal("100"),
                      appBaseService.getNbDecimalDigitForQty(),
                      RoundingMode.HALF_UP);
          qtyToInvoiceMap.put(SOrderId, realQty);
        }
        if (qtyToInvoiceMap.get(SOrderId).compareTo(saleOrderLine.getQty()) > 0) {
          throw new AxelorException(
              saleOrder,
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(SupplychainExceptionMessage.SO_INVOICE_QTY_MAX));
        }
      }
    }
    return this.generateInvoice(saleOrder, saleOrder.getSaleOrderLineList(), qtyToInvoiceMap);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice generateInvoice(SaleOrder saleOrder) throws AxelorException {

    Invoice invoice = this.createInvoice(saleOrder);
    invoice.setDeliveryAddress(saleOrder.getDeliveryAddress());
    invoice.setDeliveryAddressStr(saleOrder.getDeliveryAddressStr());

    invoiceRepo.save(invoice);

    saleOrderRepo.save(fillSaleOrder(saleOrder, invoice));

    return invoice;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice generateInvoice(SaleOrder saleOrder, List<SaleOrderLine> saleOrderLinesSelected)
      throws AxelorException {

    Invoice invoice = this.createInvoice(saleOrder, saleOrderLinesSelected);

    invoiceRepo.save(invoice);

    saleOrderRepo.save(fillSaleOrder(saleOrder, invoice));

    return invoice;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice generateInvoice(
      SaleOrder saleOrder,
      List<SaleOrderLine> saleOrderLinesSelected,
      Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException {

    Invoice invoice = this.createInvoice(saleOrder, saleOrderLinesSelected, qtyToInvoiceMap);
    invoice.setDeliveryAddress(saleOrder.getDeliveryAddress());
    invoice.setDeliveryAddressStr(saleOrder.getDeliveryAddressStr());

    FiscalPosition fiscalPosition = saleOrder.getFiscalPosition();
    invoice.setFiscalPosition(fiscalPosition);

    invoiceRepo.save(invoice);

    saleOrderRepo.save(fillSaleOrder(saleOrder, invoice));

    return invoice;
  }

  public BigDecimal computeInTaxTotalInvoiced(Invoice invoice) {

    BigDecimal total = BigDecimal.ZERO;

    if (invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED) {
      if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE) {
        total = total.add(invoice.getInTaxTotal());
      }
      if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND) {
        total = total.subtract(invoice.getInTaxTotal());
      }
    }

    if (invoice.getRefundInvoiceList() != null) {
      for (Invoice refund : invoice.getRefundInvoiceList()) {
        total = total.add(this.computeInTaxTotalInvoiced(refund));
      }
    }

    return total;
  }

  @Override
  public SaleOrder fillSaleOrder(SaleOrder saleOrder, Invoice invoice) {

    saleOrder.setOrderDate(appSupplychainService.getTodayDate(invoice.getCompany()));

    return saleOrder;
  }

  @Override
  public Invoice createInvoice(SaleOrder saleOrder) throws AxelorException {

    return createInvoice(saleOrder, saleOrder.getSaleOrderLineList());
  }

  @Override
  public Invoice createInvoice(SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList)
      throws AxelorException {
    Map<Long, BigDecimal> qtyToInvoiceMap = new HashMap<>();
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      qtyToInvoiceMap.put(saleOrderLine.getId(), saleOrderLine.getQty());
    }
    return createInvoice(saleOrder, saleOrderLineList, qtyToInvoiceMap);
  }

  @Override
  public Invoice createInvoice(
      SaleOrder saleOrder,
      List<SaleOrderLine> saleOrderLineList,
      Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException {

    InvoiceGenerator invoiceGenerator = this.createInvoiceGenerator(saleOrder);

    Invoice invoice = invoiceGenerator.generate();

    invoiceGenerator.populate(
        invoice, this.createInvoiceLines(invoice, saleOrderLineList, qtyToInvoiceMap));
    invoice.setAddressStr(saleOrder.getMainInvoicingAddressStr());

    return invoice;
  }

  @Override
  public InvoiceGenerator createInvoiceGenerator(SaleOrder saleOrder) throws AxelorException {
    return createInvoiceGenerator(saleOrder, false);
  }

  @Override
  public InvoiceGenerator createInvoiceGenerator(SaleOrder saleOrder, boolean isRefund)
      throws AxelorException {
    if (saleOrder.getCurrency() == null) {
      throw new AxelorException(
          saleOrder,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SupplychainExceptionMessage.SO_INVOICE_6),
          saleOrder.getSaleOrderSeq());
    }

    // do not use invoiced partner if the option is disabled
    if (!appSupplychainService.getAppSupplychain().getActivatePartnerRelations()) {
      saleOrder.setInvoicedPartner(null);
    }

    return new InvoiceGeneratorSupplyChain(saleOrder, isRefund) {
      @Override
      public Invoice generate() throws AxelorException {
        Invoice invoice = super.createInvoiceHeader();
        invoice.setHeadOfficeAddress(saleOrder.getClientPartner().getHeadOfficeAddress());
        return invoice;
      }
    };
  }

  // TODO ajouter tri sur les séquences
  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<SaleOrderLine> saleOrderLineList, Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {

      if (qtyToInvoiceMap.containsKey(saleOrderLine.getId())) {
        List<InvoiceLine> invoiceLines =
            createInvoiceLine(invoice, saleOrderLine, qtyToInvoiceMap.get(saleOrderLine.getId()));
        invoiceLineList.addAll(invoiceLines);
        saleOrderLine.setInvoiced(true);
      }
    }
    return invoiceLineList;
  }

  @Override
  public List<InvoiceLine> createInvoiceLine(
      Invoice invoice, SaleOrderLine saleOrderLine, BigDecimal qtyToInvoice)
      throws AxelorException {

    Product product = saleOrderLine.getProduct();

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGeneratorSupplyChain(
            invoice,
            product,
            saleOrderLine.getProductName(),
            saleOrderLine.getDescription(),
            qtyToInvoice,
            saleOrderLine.getUnit(),
            saleOrderLine.getSequence(),
            false,
            saleOrderLine,
            null,
            null) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();

            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
  }

  @Override
  public void update(SaleOrder saleOrder, Long currentInvoiceId, boolean excludeCurrentInvoice)
      throws AxelorException {
    Beans.get(InvoicingStateCache.class).invalidateSaleOrder(saleOrder.getId());
    update(saleOrder, currentInvoiceId, excludeCurrentInvoice, false);
  }

  protected void update(
      SaleOrder saleOrder,
      Long currentInvoiceId,
      boolean excludeCurrentInvoice,
      boolean checkInvoicedAmount)
      throws AxelorException {

    BigDecimal amountInvoiced =
        this.getInvoicedAmount(saleOrder, currentInvoiceId, excludeCurrentInvoice);
    if (checkInvoicedAmount && amountInvoiced.compareTo(saleOrder.getExTaxTotal()) > 0) {
      throw new AxelorException(
          saleOrder,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.SO_INVOICE_TOO_MUCH_INVOICED),
          saleOrder.getSaleOrderSeq());
    }
    saleOrder.setAmountInvoiced(amountInvoiced);

    if (appSupplychainService.getAppSupplychain().getCompleteSaleOrderOnInvoicing()
        && amountInvoiced.compareTo(saleOrder.getExTaxTotal()) == 0
        && saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_ORDER_CONFIRMED) {
      saleOrderWorkflowService.completeSaleOrder(saleOrder);
    }
  }

  @Override
  public BigDecimal getInvoicedAmount(SaleOrder saleOrder) {
    return this.getInvoicedAmount(saleOrder, null, true);
  }

  /**
   * Return the remaining amount to invoice for the saleOrder in parameter
   *
   * @param saleOrder
   * @param currentInvoiceId In the case of invoice ventilation or cancellation, the invoice status
   *     isn't modify in database but it will be integrated in calculation For ventilation, the
   *     invoice should be integrated in calculation For cancellation, the invoice shouldn't be
   *     integrated in calculation
   * @param excludeCurrentInvoice To know if the invoice should be or not integrated in calculation
   */
  @Override
  public BigDecimal getInvoicedAmount(
      SaleOrder saleOrder, Long currentInvoiceId, boolean excludeCurrentInvoice) {

    BigDecimal invoicedAmount = BigDecimal.ZERO;

    BigDecimal saleAmount =
        this.getAmountVentilated(
            saleOrder,
            currentInvoiceId,
            excludeCurrentInvoice,
            InvoiceRepository.OPERATION_TYPE_CLIENT_SALE);
    BigDecimal refundAmount =
        this.getAmountVentilated(
            saleOrder,
            currentInvoiceId,
            excludeCurrentInvoice,
            InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND);

    if (saleAmount != null) {
      invoicedAmount = invoicedAmount.add(saleAmount);
    }
    if (refundAmount != null) {
      invoicedAmount = invoicedAmount.subtract(refundAmount);
    }

    if (!saleOrder.getCurrency().equals(saleOrder.getCompany().getCurrency())
        && saleOrder.getCompanyExTaxTotal().compareTo(BigDecimal.ZERO) != 0) {
      BigDecimal rate =
          invoicedAmount.divide(saleOrder.getCompanyExTaxTotal(), 4, RoundingMode.HALF_UP);
      invoicedAmount = saleOrder.getExTaxTotal().multiply(rate);
    }

    log.debug(
        "Compute the invoiced amount ({}) of the sale order : {}",
        invoicedAmount,
        saleOrder.getSaleOrderSeq());

    return invoicedAmount;
  }

  @Override
  public List<Invoice> getInvoices(SaleOrder saleOrder) {
    return invoiceRepo
        .all()
        .filter(
            "self.saleOrder.id = ? OR (self.saleOrder.id IS NULL AND EXISTS(SELECT 1 FROM self.invoiceLineList inli WHERE inli.saleOrderLine.id IN (?)))",
            saleOrder.getId(),
            saleOrder.getSaleOrderLineList().stream()
                .map(SaleOrderLine::getId)
                .collect(Collectors.toList()))
        .fetch();
  }

  protected BigDecimal getAmountVentilated(
      SaleOrder saleOrder,
      Long currentInvoiceId,
      boolean excludeCurrentInvoice,
      int invoiceOperationTypeSelect) {

    String query = "SELECT SUM(self.companyExTaxTotal)" + " FROM InvoiceLine as self";
    query += " WHERE self.saleOrderLine.saleOrder.id = :saleOrderId";
    query +=
        " AND self.invoice.operationTypeSelect = :invoiceOperationTypeSelect"
            + " AND self.invoice.statusSelect = :statusVentilated";

    // exclude invoices that are advance payments
    boolean invoiceIsNotAdvancePayment =
        (currentInvoiceId != null
            && invoiceRepo.find(currentInvoiceId).getOperationSubTypeSelect()
                != InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE);

    if (invoiceIsNotAdvancePayment) {
      if (excludeCurrentInvoice) {
        query += " AND self.invoice.id <> :invoiceId";
      } else {
        query +=
            " OR (self.invoice.id = :invoiceId AND self.invoice.operationTypeSelect = :invoiceOperationTypeSelect) ";
      }
    }

    javax.persistence.Query q = JPA.em().createQuery(query, BigDecimal.class);

    q.setParameter("saleOrderId", saleOrder.getId());
    q.setParameter("statusVentilated", InvoiceRepository.STATUS_VENTILATED);
    q.setParameter("invoiceOperationTypeSelect", invoiceOperationTypeSelect);
    if (invoiceIsNotAdvancePayment) {
      q.setParameter("invoiceId", currentInvoiceId);
    }

    BigDecimal invoicedAmount = (BigDecimal) q.getSingleResult();

    if (invoicedAmount != null) {
      return invoicedAmount;
    } else {
      return BigDecimal.ZERO;
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice mergeInvoice(
      List<Invoice> invoiceList,
      Company company,
      Currency currency,
      Partner partner,
      Partner contactPartner,
      PriceList priceList,
      PaymentMode paymentMode,
      PaymentCondition paymentCondition,
      TradingName tradingName,
      FiscalPosition fiscalPosition,
      SaleOrder saleOrder)
      throws AxelorException {
    if (saleOrder != null) {
      StringBuilder numSeq = new StringBuilder();
      StringBuilder externalRef = new StringBuilder();

      for (Invoice invoiceLocal : invoiceList) {
        if (numSeq.length() > 0) {
          numSeq.append("-");
        }
        if (invoiceLocal.getInternalReference() != null) {
          numSeq.append(invoiceLocal.getInternalReference());
        }

        if (externalRef.length() > 0) {
          externalRef.append("|");
        }
        if (invoiceLocal.getExternalReference() != null) {
          externalRef.append(invoiceLocal.getExternalReference());
        }
      }
      InvoiceGenerator invoiceGenerator = this.createInvoiceGenerator(saleOrder);
      Invoice invoiceMerged = invoiceGenerator.generate();
      invoiceMerged.setExternalReference(externalRef.toString());
      invoiceMerged.setInternalReference(numSeq.toString());

      if (paymentMode != null) {
        invoiceMerged.setPaymentMode(paymentMode);
      }
      if (paymentCondition != null) {
        invoiceMerged.setPaymentCondition(paymentCondition);
      }

      List<InvoiceLine> invoiceLines = invoiceService.getInvoiceLinesFromInvoiceList(invoiceList);
      invoiceGenerator.populate(invoiceMerged, invoiceLines);
      invoiceService.setInvoiceForInvoiceLines(invoiceLines, invoiceMerged);
      invoiceMerged.setSaleOrder(null);
      invoiceRepo.save(invoiceMerged);
      invoiceService.swapStockMoveInvoices(invoiceList, invoiceMerged);
      invoiceService.deleteOldInvoices(invoiceList);
      return invoiceMerged;
    } else {

      Invoice invoiceMerged =
          invoiceService.mergeInvoice(
              invoiceList,
              company,
              currency,
              partner,
              contactPartner,
              priceList,
              paymentMode,
              paymentCondition,
              tradingName,
              fiscalPosition);
      invoiceService.swapStockMoveInvoices(invoiceList, invoiceMerged);
      invoiceService.deleteOldInvoices(invoiceList);
      return invoiceMerged;
    }
  }

  @Override
  public List<Integer> getInvoicingWizardOperationDomain(SaleOrder saleOrder) {
    boolean manageAdvanceInvoice =
        Beans.get(AppAccountService.class).getAppAccount().getManageAdvancePaymentInvoice();
    boolean allowTimetableInvoicing =
        appSupplychainService.getAppSupplychain().getAllowTimetableInvoicing();
    BigDecimal amountInvoiced = saleOrder.getAmountInvoiced();
    BigDecimal exTaxTotal = saleOrder.getExTaxTotal();
    Invoice invoice =
        Query.of(Invoice.class)
            .filter(
                " self.saleOrder.id = :saleOrderId "
                    + "AND self.statusSelect != :invoiceStatus "
                    + "AND self.operationSubTypeSelect != :advancePaymentSubType")
            .bind("saleOrderId", saleOrder.getId())
            .bind("invoiceStatus", InvoiceRepository.STATUS_CANCELED)
            .bind("advancePaymentSubType", InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE)
            .fetchOne();
    List<Integer> operationSelectList = new ArrayList<>();
    if (exTaxTotal.compareTo(BigDecimal.ZERO) != 0) {
      operationSelectList.add(SaleOrderRepository.INVOICE_LINES);
    }
    if (manageAdvanceInvoice && exTaxTotal.compareTo(BigDecimal.ZERO) != 0) {
      operationSelectList.add(SaleOrderRepository.INVOICE_ADVANCE_PAYMENT);
    }
    if (allowTimetableInvoicing) {
      operationSelectList.add(SaleOrderRepository.INVOICE_TIMETABLES);
    }
    if (invoice == null && amountInvoiced.compareTo(BigDecimal.ZERO) == 0
        || exTaxTotal.compareTo(BigDecimal.ZERO) == 0) {
      operationSelectList.add(SaleOrderRepository.INVOICE_ALL);
    }

    return operationSelectList;
  }

  @Override
  public void displayErrorMessageIfSaleOrderIsInvoiceable(
      SaleOrder saleOrder, BigDecimal amountToInvoice, boolean isPercent) throws AxelorException {
    List<Invoice> invoices =
        Query.of(Invoice.class)
            .filter(
                " self.saleOrder.id = :saleOrderId "
                    + "AND self.statusSelect != :invoiceStatus "
                    + "AND (self.operationTypeSelect = :saleOperationTypeSelect OR self.operationTypeSelect = :refundOperationTypeSelect)")
            .bind("saleOrderId", saleOrder.getId())
            .bind("invoiceStatus", InvoiceRepository.STATUS_CANCELED)
            .bind("saleOperationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)
            .bind("refundOperationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND)
            .fetch();

    BigDecimal sumInvoices = commonInvoiceService.computeSumInvoices(invoices);
    sumInvoices = sumInvoices.add(amountToInvoice);
    if (sumInvoices.compareTo(saleOrder.getExTaxTotal()) > 0) {
      throw new AxelorException(
          saleOrder,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.SO_INVOICE_TOO_MUCH_INVOICED),
          saleOrder.getSaleOrderSeq());
    }
  }

  @Override
  public void displayErrorMessageBtnGenerateInvoice(SaleOrder saleOrder) throws AxelorException {
    List<Invoice> invoices =
        Query.of(Invoice.class)
            .filter(
                " self.saleOrder.id = :saleOrderId AND self.operationSubTypeSelect = :operationSubTypeSelect"
                    + " AND self.statusSelect != :invoiceStatus")
            .bind("saleOrderId", saleOrder.getId())
            .bind("operationSubTypeSelect", InvoiceRepository.OPERATION_SUB_TYPE_DEFAULT)
            .bind("invoiceStatus", InvoiceRepository.STATUS_CANCELED)
            .fetch();
    BigDecimal sumInvoices = computeSumInvoices(invoices);
    if (sumInvoices.compareTo(saleOrder.getExTaxTotal()) >= 0) {
      throw new AxelorException(
          saleOrder,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.SO_INVOICE_GENERATE_ALL_INVOICES));
    }
  }

  @Override
  public int getSaleOrderInvoicingState(SaleOrder saleOrder) {
    return saleInvoicingStateService.getInvoicingState(
        saleOrder.getAmountInvoiced(),
        saleOrder.getExTaxTotal(),
        atLeastOneInvoiceIsVentilated(saleOrder));
  }

  @Override
  public boolean atLeastOneInvoiceIsVentilated(SaleOrder saleOrder) {
    if (saleOrder.getSaleOrderLineList() == null || saleOrder.getSaleOrderLineList().isEmpty()) {
      return false;
    }
    return invoiceRepo
            .all()
            .filter(
                "self.statusSelect = :statusSelect AND self.saleOrder.id = :saleOrder OR (self.saleOrder.id IS NULL AND EXISTS(SELECT 1 FROM self.invoiceLineList inli WHERE inli.saleOrderLine.id IN (:saleOrderLineList)))")
            .bind("statusSelect", InvoiceRepository.STATUS_VENTILATED)
            .bind("saleOrder", saleOrder.getId())
            .bind(
                "saleOrderLineList",
                saleOrder.getSaleOrderLineList().stream()
                    .map(SaleOrderLine::getId)
                    .collect(Collectors.toList()))
            .count()
        > 0;
  }

  public BigDecimal computeSumInvoices(List<Invoice> invoices) {
    BigDecimal sumInvoices = BigDecimal.ZERO;
    for (Invoice invoice : invoices) {
      if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND
          || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND) {
        sumInvoices = sumInvoices.subtract(invoice.getExTaxTotal());
      } else {
        sumInvoices = sumInvoices.add(invoice.getExTaxTotal());
      }
    }
    return sumInvoices;
  }
}
