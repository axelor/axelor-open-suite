/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.TradingNameService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.app.service.AppService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IntercoServiceImpl implements IntercoService {

  protected PurchaseConfigService purchaseConfigService;
  protected BankDetailsService bankDetailsService;

  protected static int DEFAULT_INVOICE_COPY = 1;

  @Inject
  public IntercoServiceImpl(
      PurchaseConfigService purchaseConfigService, BankDetailsService bankDetailsService) {
    this.purchaseConfigService = purchaseConfigService;
    this.bankDetailsService = bankDetailsService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public SaleOrder generateIntercoSaleFromPurchase(PurchaseOrder purchaseOrder)
      throws AxelorException {

    SaleOrderCreateService saleOrderCreateService = Beans.get(SaleOrderCreateService.class);
    SaleOrderComputeService saleOrderComputeService = Beans.get(SaleOrderComputeService.class);
    Company intercoCompany = findIntercoCompany(purchaseOrder.getSupplierPartner());
    // create sale order
    SaleOrder saleOrder =
        saleOrderCreateService.createSaleOrder(
            null,
            intercoCompany,
            purchaseOrder.getContactPartner(),
            purchaseOrder.getCurrency(),
            purchaseOrder.getEstimatedReceiptDate(),
            null,
            null,
            purchaseOrder.getPriceList(),
            purchaseOrder.getCompany().getPartner(),
            null,
            null,
            null);

    // in ati
    saleOrder.setInAti(purchaseOrder.getInAti());

    // copy date
    saleOrder.setOrderDate(purchaseOrder.getOrderDate());

    // copy payments
    PaymentMode intercoPaymentMode =
        Beans.get(PaymentModeService.class).reverseInOut(purchaseOrder.getPaymentMode());
    saleOrder.setPaymentMode(intercoPaymentMode);
    saleOrder.setPaymentCondition(purchaseOrder.getPaymentCondition());

    // copy delivery info
    saleOrder.setShipmentMode(purchaseOrder.getShipmentMode());
    saleOrder.setFreightCarrierMode(purchaseOrder.getFreightCarrierMode());

    // get stock location
    saleOrder.setStockLocation(
        Beans.get(SaleOrderSupplychainService.class)
            .getStockLocation(purchaseOrder.getCompany().getPartner(), intercoCompany));

    // copy timetable info
    saleOrder.setExpectedRealisationDate(purchaseOrder.getExpectedRealisationDate());
    saleOrder.setAmountToBeSpreadOverTheTimetable(
        purchaseOrder.getAmountToBeSpreadOverTheTimetable());

    // create lines
    List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrder.getPurchaseOrderLineList();
    if (purchaseOrderLineList != null) {
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
        this.createIntercoSaleLineFromPurchaseLine(purchaseOrderLine, saleOrder);
      }
    }

    saleOrder.setPrintingSettings(intercoCompany.getPrintingSettings());

    // compute the sale order
    saleOrderComputeService.computeSaleOrder(saleOrder);

    saleOrder.setCreatedByInterco(true);
    Beans.get(SaleOrderRepository.class).save(saleOrder);
    if (Beans.get(AppSupplychainService.class)
        .getAppSupplychain()
        .getIntercoSaleOrderCreateFinalized()) {
      Beans.get(SaleOrderWorkflowService.class).finalizeQuotation(saleOrder);
    }
    purchaseOrder.setExternalReference(saleOrder.getSaleOrderSeq());
    saleOrder.setExternalReference(purchaseOrder.getPurchaseOrderSeq());
    return saleOrder;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public PurchaseOrder generateIntercoPurchaseFromSale(SaleOrder saleOrder) throws AxelorException {

    PurchaseOrderService purchaseOrderService = Beans.get(PurchaseOrderService.class);

    Company intercoCompany = findIntercoCompany(saleOrder.getClientPartner());
    // create purchase order
    PurchaseOrder purchaseOrder = new PurchaseOrder();
    purchaseOrder.setCompany(intercoCompany);
    purchaseOrder.setContactPartner(saleOrder.getContactPartner());
    purchaseOrder.setCurrency(saleOrder.getCurrency());
    purchaseOrder.setOrderDate(saleOrder.getCreationDate());
    purchaseOrder.setPriceList(saleOrder.getPriceList());
    purchaseOrder.setTradingName(saleOrder.getTradingName());
    purchaseOrder.setPurchaseOrderLineList(new ArrayList<>());
    purchaseOrder.setDisplayPriceOnQuotationRequest(
        purchaseConfigService
            .getPurchaseConfig(intercoCompany)
            .getDisplayPriceOnQuotationRequest());

    purchaseOrder.setPrintingSettings(
        Beans.get(TradingNameService.class).getDefaultPrintingSettings(null, intercoCompany));

    purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_DRAFT);
    purchaseOrder.setSupplierPartner(saleOrder.getCompany().getPartner());
    purchaseOrder.setTradingName(saleOrder.getTradingName());

    // in ati
    purchaseOrder.setInAti(saleOrder.getInAti());

    // copy payments
    PaymentMode intercoPaymentMode =
        Beans.get(PaymentModeService.class).reverseInOut(saleOrder.getPaymentMode());
    purchaseOrder.setPaymentMode(intercoPaymentMode);
    purchaseOrder.setPaymentCondition(saleOrder.getPaymentCondition());

    // copy delivery info
    purchaseOrder.setStockLocation(
        Beans.get(PurchaseOrderSupplychainService.class)
            .getStockLocation(saleOrder.getCompany().getPartner(), intercoCompany));
    purchaseOrder.setShipmentMode(saleOrder.getShipmentMode());
    purchaseOrder.setFreightCarrierMode(saleOrder.getFreightCarrierMode());

    // copy timetable info
    purchaseOrder.setExpectedRealisationDate(saleOrder.getExpectedRealisationDate());
    purchaseOrder.setAmountToBeSpreadOverTheTimetable(
        saleOrder.getAmountToBeSpreadOverTheTimetable());

    purchaseOrder.setEstimatedReceiptDate(saleOrder.getEstimatedDeliveryDate());

    // create lines
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (saleOrderLineList != null) {
      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        this.createIntercoPurchaseLineFromSaleLine(saleOrderLine, purchaseOrder);
      }
    }
    purchaseOrder.setPrintingSettings(intercoCompany.getPrintingSettings());

    // compute the purchase order
    purchaseOrderService.computePurchaseOrder(purchaseOrder);

    purchaseOrder.setCreatedByInterco(true);
    Beans.get(PurchaseOrderRepository.class).save(purchaseOrder);
    if (Beans.get(AppSupplychainService.class)
        .getAppSupplychain()
        .getIntercoPurchaseOrderCreateRequested()) {
      Beans.get(PurchaseOrderService.class).requestPurchaseOrder(purchaseOrder);
    }
    saleOrder.setExternalReference(purchaseOrder.getPurchaseOrderSeq());
    purchaseOrder.setExternalReference(saleOrder.getSaleOrderSeq());
    return purchaseOrder;
  }

  /**
   * @param saleOrderLine the sale order line needed to create the purchase order line
   * @param purchaseOrder the purchase order line belongs to this purchase order
   * @return the created purchase order line
   */
  protected PurchaseOrderLine createIntercoPurchaseLineFromSaleLine(
      SaleOrderLine saleOrderLine, PurchaseOrder purchaseOrder) throws AxelorException {
    PurchaseOrderLine purchaseOrderLine =
        Beans.get(PurchaseOrderLineService.class)
            .createPurchaseOrderLine(
                purchaseOrder,
                saleOrderLine.getProduct(),
                saleOrderLine.getProductName(),
                saleOrderLine.getDescription(),
                saleOrderLine.getQty(),
                saleOrderLine.getUnit());
    // compute amount
    purchaseOrderLine.setPrice(saleOrderLine.getPrice());
    purchaseOrderLine.setInTaxPrice(saleOrderLine.getInTaxPrice());
    purchaseOrderLine.setExTaxTotal(saleOrderLine.getExTaxTotal());
    purchaseOrderLine.setDiscountTypeSelect(saleOrderLine.getDiscountTypeSelect());
    purchaseOrderLine.setDiscountAmount(saleOrderLine.getDiscountAmount());

    // delivery
    purchaseOrderLine.setEstimatedReceiptDate(saleOrderLine.getEstimatedDeliveryDate());
    purchaseOrderLine.setDesiredReceiptDate(saleOrderLine.getDesiredDeliveryDate());

    // compute price discounted
    BigDecimal priceDiscounted =
        Beans.get(PurchaseOrderLineService.class)
            .computeDiscount(purchaseOrderLine, purchaseOrder.getInAti());
    purchaseOrderLine.setPriceDiscounted(priceDiscounted);

    // tax
    purchaseOrderLine.setTaxLine(saleOrderLine.getTaxLine());

    // analyticalDistribution
    purchaseOrderLine =
        Beans.get(PurchaseOrderLineServiceSupplychainImpl.class)
            .getAndComputeAnalyticDistribution(purchaseOrderLine, purchaseOrder);

    purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLine);
    return purchaseOrderLine;
  }

  /**
   * @param purchaseOrderLine the purchase order line needed to create the sale order line
   * @param saleOrder the sale order line belongs to this purchase order
   * @return the created purchase order line
   * @throws AxelorException
   */
  protected SaleOrderLine createIntercoSaleLineFromPurchaseLine(
      PurchaseOrderLine purchaseOrderLine, SaleOrder saleOrder) throws AxelorException {
    SaleOrderLine saleOrderLine = new SaleOrderLine();

    saleOrderLine.setSaleOrder(saleOrder);
    saleOrderLine.setProduct(purchaseOrderLine.getProduct());
    saleOrderLine.setProductName(purchaseOrderLine.getProductName());

    saleOrderLine.setDescription(purchaseOrderLine.getDescription());
    saleOrderLine.setQty(purchaseOrderLine.getQty());
    saleOrderLine.setUnit(purchaseOrderLine.getUnit());

    // compute amount
    saleOrderLine.setPrice(purchaseOrderLine.getPrice());
    saleOrderLine.setInTaxPrice(purchaseOrderLine.getInTaxPrice());
    saleOrderLine.setExTaxTotal(purchaseOrderLine.getExTaxTotal());
    saleOrderLine.setDiscountTypeSelect(purchaseOrderLine.getDiscountTypeSelect());
    saleOrderLine.setDiscountAmount(purchaseOrderLine.getDiscountAmount());

    // compute price discounted
    BigDecimal priceDiscounted =
        Beans.get(SaleOrderLineService.class).computeDiscount(saleOrderLine, saleOrder.getInAti());
    saleOrderLine.setPriceDiscounted(priceDiscounted);

    // delivery
    saleOrderLine.setDesiredDeliveryDate(purchaseOrderLine.getDesiredReceiptDate());
    saleOrderLine.setEstimatedDeliveryDate(purchaseOrderLine.getEstimatedReceiptDate());

    // tax
    saleOrderLine.setTaxLine(purchaseOrderLine.getTaxLine());

    // analyticDistribution
    saleOrderLine =
        Beans.get(SaleOrderLineServiceSupplyChainImpl.class)
            .getAndComputeAnalyticDistribution(saleOrderLine, saleOrder);
    if (saleOrderLine.getAnalyticMoveLineList() != null) {
      for (AnalyticMoveLine obj : saleOrderLine.getAnalyticMoveLineList()) {
        obj.setSaleOrderLine(saleOrderLine);
      }
    }

    saleOrder.addSaleOrderLineListItem(saleOrderLine);
    return saleOrderLine;
  }

  @Override
  public Invoice generateIntercoInvoice(Invoice invoice) throws AxelorException {
    PartnerService partnerService = Beans.get(PartnerService.class);
    InvoiceRepository invoiceRepository = Beans.get(InvoiceRepository.class);
    InvoiceService invoiceService = Beans.get(InvoiceService.class);

    boolean isPurchase;
    // set the status
    int generatedOperationTypeSelect;
    int priceListRepositoryType;
    switch (invoice.getOperationTypeSelect()) {
      case InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE:
        generatedOperationTypeSelect = InvoiceRepository.OPERATION_TYPE_CLIENT_SALE;
        priceListRepositoryType = PriceListRepository.TYPE_SALE;
        isPurchase = false;
        break;
      case InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND:
        generatedOperationTypeSelect = InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND;
        priceListRepositoryType = PriceListRepository.TYPE_SALE;
        isPurchase = false;
        break;
      case InvoiceRepository.OPERATION_TYPE_CLIENT_SALE:
        generatedOperationTypeSelect = InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE;
        priceListRepositoryType = PriceListRepository.TYPE_PURCHASE;
        isPurchase = true;
        break;
      case InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND:
        generatedOperationTypeSelect = InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND;
        priceListRepositoryType = PriceListRepository.TYPE_PURCHASE;
        isPurchase = true;
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(SupplychainExceptionMessage.INVOICE_MISSING_TYPE),
            invoice);
    }
    Company intercoCompany = findIntercoCompany(invoice.getPartner());
    Partner intercoPartner = invoice.getCompany().getPartner();
    PaymentMode intercoPaymentMode =
        Beans.get(PaymentModeService.class).reverseInOut(invoice.getPaymentMode());
    Address intercoAddress = partnerService.getInvoicingAddress(intercoPartner);
    BankDetails intercoBankDetails = partnerService.getDefaultBankDetails(intercoPartner);
    AccountingSituation accountingSituation =
        Beans.get(AccountingSituationService.class)
            .getAccountingSituation(intercoPartner, intercoCompany);
    PriceList intercoPriceList =
        Beans.get(PartnerPriceListService.class)
            .getDefaultPriceList(intercoPartner, priceListRepositoryType);

    Invoice intercoInvoice = invoiceRepository.copy(invoice, true);
    intercoInvoice.setOperationTypeSelect(generatedOperationTypeSelect);
    intercoInvoice.setCompany(intercoCompany);
    intercoInvoice.setPartner(intercoPartner);
    intercoInvoice.setAddress(intercoAddress);
    intercoInvoice.setAddressStr(Beans.get(AddressService.class).computeAddressStr(intercoAddress));
    intercoInvoice.setPaymentMode(intercoPaymentMode);
    intercoInvoice.setBankDetails(intercoBankDetails);
    Set<Invoice> invoices = invoiceService.getDefaultAdvancePaymentInvoice(intercoInvoice);
    intercoInvoice.setAdvancePaymentInvoiceSet(invoices);
    if (accountingSituation != null) {
      intercoInvoice.setInvoiceAutomaticMail(accountingSituation.getInvoiceAutomaticMail());
      intercoInvoice.setInvoiceMessageTemplate(accountingSituation.getInvoiceMessageTemplate());
      intercoInvoice.setPfpValidatorUser(accountingSituation.getPfpValidatorUser());
    }
    intercoInvoice.setPriceList(intercoPriceList);
    intercoInvoice.setInvoicesCopySelect(
        (intercoPartner.getInvoicesCopySelect() == 0)
            ? DEFAULT_INVOICE_COPY
            : intercoPartner.getInvoicesCopySelect());
    intercoInvoice.setCreatedByInterco(true);
    intercoInvoice.setInterco(false);

    if (isPurchase) {
      intercoInvoice.setOriginDate(invoice.getInvoiceDate());
      intercoInvoice.setSupplierInvoiceNb(invoice.getInvoiceId());
    }

    intercoInvoice.setPrintingSettings(intercoCompany.getPrintingSettings());

    if (intercoInvoice.getInvoiceLineList() != null) {
      for (InvoiceLine invoiceLine : intercoInvoice.getInvoiceLineList()) {
        invoiceLine.setInvoice(intercoInvoice);
        createIntercoInvoiceLine(invoiceLine, isPurchase);
      }
    }

    invoiceService.compute(intercoInvoice);
    intercoInvoice.setExternalReference(invoice.getInvoiceId());
    intercoInvoice.setCompanyBankDetails(
        bankDetailsService.getDefaultCompanyBankDetails(
            intercoCompany, intercoPaymentMode, intercoPartner, generatedOperationTypeSelect));
    intercoInvoice = invoiceRepository.save(intercoInvoice);

    // the interco invoice needs to be saved before we can attach files to it
    if (invoice.getPrintedPDF() != null) {
      copyInvoicePdfToIntercoDMS(invoice.getPrintedPDF(), intercoInvoice);
    }
    if (Beans.get(AppSupplychainService.class)
        .getAppSupplychain()
        .getIntercoInvoiceCreateValidated()) {
      invoiceService.validate(intercoInvoice);
    }
    invoice.setExternalReference(intercoInvoice.getInvoiceId());
    return intercoInvoice;
  }

  protected void copyInvoicePdfToIntercoDMS(MetaFile printedPdf, Invoice intercoInvoice)
      throws AxelorException {
    MetaFiles metaFiles = Beans.get(MetaFiles.class);
    try {
      String printedPdfPath = AppService.getFileUploadDir() + printedPdf.getFilePath();
      MetaFile printedPdfCopy = metaFiles.upload(new File(printedPdfPath));
      metaFiles.attach(printedPdfCopy, printedPdf.getFileName(), intercoInvoice);
    } catch (IOException e) {
      TraceBackService.trace(e);
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    }
  }

  protected InvoiceLine createIntercoInvoiceLine(InvoiceLine invoiceLine, boolean isPurchase)
      throws AxelorException {
    AccountManagementAccountService accountManagementAccountService =
        Beans.get(AccountManagementAccountService.class);
    InvoiceLineService invoiceLineService = Beans.get(InvoiceLineService.class);
    InvoiceLineAnalyticService invoiceLineAnalyticService =
        Beans.get(InvoiceLineAnalyticService.class);
    Invoice intercoInvoice = invoiceLine.getInvoice();
    if (intercoInvoice.getCompany() != null) {
      FiscalPosition fiscalPosition = intercoInvoice.getFiscalPosition();

      Account account =
          accountManagementAccountService.getProductAccount(
              invoiceLine.getProduct(),
              intercoInvoice.getCompany(),
              fiscalPosition,
              isPurchase,
              false);
      invoiceLine.setAccount(account);

      TaxLine taxLine = invoiceLineService.getTaxLine(intercoInvoice, invoiceLine, isPurchase);
      invoiceLine.setTaxLine(taxLine);
      invoiceLine.setTaxRate(taxLine.getValue());
      invoiceLine.setTaxCode(taxLine.getTax().getCode());
      TaxEquiv taxEquiv =
          accountManagementAccountService.getProductTaxEquiv(
              invoiceLine.getProduct(), intercoInvoice.getCompany(), fiscalPosition, isPurchase);
      invoiceLine.setTaxEquiv(taxEquiv);
      invoiceLine.setCompanyExTaxTotal(
          invoiceLineService.getCompanyExTaxTotal(invoiceLine.getExTaxTotal(), intercoInvoice));
      invoiceLine.setCompanyInTaxTotal(
          invoiceLineService.getCompanyExTaxTotal(invoiceLine.getInTaxTotal(), intercoInvoice));

      if (invoiceLine.getAnalyticDistributionTemplate() != null) {
        invoiceLine.setAnalyticDistributionTemplate(
            accountManagementAccountService.getAnalyticDistributionTemplate(
                invoiceLine.getProduct(), intercoInvoice.getCompany(), isPurchase));
        List<AnalyticMoveLine> analyticMoveLineList =
            invoiceLineAnalyticService.createAnalyticDistributionWithTemplate(invoiceLine);
        analyticMoveLineList.forEach(
            analyticMoveLine -> analyticMoveLine.setInvoiceLine(invoiceLine));
        invoiceLine.setAnalyticMoveLineList(analyticMoveLineList);
      }
    }
    return invoiceLine;
  }

  @Override
  public Company findIntercoCompany(Partner partner) {
    return Beans.get(CompanyRepository.class).all().filter("self.partner = ?", partner).fetchOne();
  }
}
