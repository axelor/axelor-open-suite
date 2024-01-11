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

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
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

  @Inject
  public IntercoServiceImpl(PurchaseConfigService purchaseConfigService) {
    this.purchaseConfigService = purchaseConfigService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public SaleOrder generateIntercoSaleFromPurchase(PurchaseOrder purchaseOrder)
      throws AxelorException {

    SaleOrderCreateService saleOrderCreateService = Beans.get(SaleOrderCreateService.class);
    SaleOrderComputeService saleOrderComputeService = Beans.get(SaleOrderComputeService.class);
    Company intercoCompany = findIntercoCompany(purchaseOrder.getSupplierPartner());
    Partner clientPartner = purchaseOrder.getCompany().getPartner();

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
            clientPartner,
            null,
            null,
            clientPartner.getFiscalPosition());

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
            .getStockLocation(clientPartner, intercoCompany));

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
    Partner supplierPartner = saleOrder.getCompany().getPartner();
    purchaseOrder.setSupplierPartner(supplierPartner);
    purchaseOrder.setFiscalPosition(supplierPartner.getFiscalPosition());
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
            .getStockLocation(supplierPartner, intercoCompany));
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
    InvoiceRepository invoiceRepository = Beans.get(InvoiceRepository.class);
    InvoiceService invoiceService = Beans.get(InvoiceService.class);

    int generatedOperationTypeSelect = getIntercoInvoiceOperationTypeSelect(invoice);
    Company intercoCompany = findIntercoCompany(invoice.getPartner());
    Partner intercoPartner = invoice.getCompany().getPartner();
    PaymentMode intercoPaymentMode =
        Beans.get(PaymentModeService.class).reverseInOut(invoice.getPaymentMode());
    int priceListRepositoryType =
        InvoiceToolService.isPurchase(invoice)
            ? PriceListRepository.TYPE_SALE
            : PriceListRepository.TYPE_PURCHASE;
    PriceList intercoPriceList =
        Beans.get(PartnerPriceListService.class)
            .getDefaultPriceList(intercoPartner, priceListRepositoryType);

    InvoiceGenerator invoiceGenerator =
        new InvoiceGenerator(
            generatedOperationTypeSelect,
            intercoCompany,
            invoice.getPaymentCondition(),
            intercoPaymentMode,
            null,
            intercoPartner,
            null,
            invoice.getCurrency(),
            intercoPriceList,
            null,
            invoice.getInvoiceId(),
            null,
            null,
            invoice.getTradingName(),
            invoice.getGroupProductsOnPrintings()) {

          @Override
          public Invoice generate() throws AxelorException {

            Invoice intercoInvoice = super.createInvoiceHeader();
            Set<Invoice> invoices = invoiceService.getDefaultAdvancePaymentInvoice(intercoInvoice);
            intercoInvoice.setAdvancePaymentInvoiceSet(invoices);
            intercoInvoice.setCreatedByInterco(true);

            if (InvoiceToolService.isPurchase(intercoInvoice)) {
              intercoInvoice.setOriginDate(invoice.getInvoiceDate());
              intercoInvoice.setSupplierInvoiceNb(intercoInvoice.getExternalReference());
            }
            return intercoInvoice;
          }
        };
    Invoice intercoInvoice = invoiceGenerator.generate();

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    if (invoice.getInvoiceLineList() != null) {
      for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
        invoiceLineList.addAll(createIntercoInvoiceLine(intercoInvoice, invoiceLine));
      }
    }
    intercoInvoice.setInvoiceLineList(invoiceLineList);
    invoiceService.compute(intercoInvoice);
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
    return intercoInvoice;
  }

  protected int getIntercoInvoiceOperationTypeSelect(Invoice invoice) throws AxelorException {
    int generatedOperationTypeSelect;
    switch (invoice.getOperationTypeSelect()) {
      case InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE:
        generatedOperationTypeSelect = InvoiceRepository.OPERATION_TYPE_CLIENT_SALE;
        break;
      case InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND:
        generatedOperationTypeSelect = InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND;
        break;
      case InvoiceRepository.OPERATION_TYPE_CLIENT_SALE:
        generatedOperationTypeSelect = InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE;
        break;
      case InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND:
        generatedOperationTypeSelect = InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND;
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(SupplychainExceptionMessage.INVOICE_MISSING_TYPE),
            invoice);
    }
    return generatedOperationTypeSelect;
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

  protected List<InvoiceLine> createIntercoInvoiceLine(
      Invoice intercoInvoice, InvoiceLine invoiceLine) throws AxelorException {
    AccountManagementAccountService accountManagementAccountService =
        Beans.get(AccountManagementAccountService.class);
    InvoiceLineAnalyticService invoiceLineAnalyticService =
        Beans.get(InvoiceLineAnalyticService.class);
    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            intercoInvoice,
            invoiceLine.getProduct(),
            invoiceLine.getProductName(),
            invoiceLine.getPrice(),
            invoiceLine.getInTaxPrice(),
            invoiceLine.getPriceDiscounted(),
            invoiceLine.getDescription(),
            invoiceLine.getQty(),
            invoiceLine.getUnit(),
            null,
            invoiceLine.getSequence(),
            invoiceLine.getDiscountAmount(),
            invoiceLine.getDiscountTypeSelect(),
            invoiceLine.getExTaxTotal(),
            invoiceLine.getInTaxTotal(),
            false) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();
            invoiceLine.setAnalyticDistributionTemplate(
                accountManagementAccountService.getAnalyticDistributionTemplate(
                    invoiceLine.getProduct(),
                    intercoInvoice.getCompany(),
                    InvoiceToolService.isPurchase(intercoInvoice)));
            if (invoiceLine.getAnalyticDistributionTemplate() != null) {
              List<AnalyticMoveLine> analyticMoveLineList =
                  invoiceLineAnalyticService.createAnalyticDistributionWithTemplate(invoiceLine);
              analyticMoveLineList.forEach(invoiceLine::addAnalyticMoveLineListItem);
            }
            return List.of(invoiceLine);
          }
        };

    return invoiceLineGenerator.creates();
  }

  @Override
  public Company findIntercoCompany(Partner partner) {
    return Beans.get(CompanyRepository.class).all().filter("self.partner = ?", partner).fetchOne();
  }
}
