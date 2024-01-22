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
package com.axelor.apps.supplychain.service.workflow;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.workflow.ventilate.WorkflowVentilationServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.StockMoveInvoiceService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowVentilationServiceSupplychainImpl extends WorkflowVentilationServiceImpl {

  protected final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected SaleOrderInvoiceService saleOrderInvoiceService;

  protected PurchaseOrderInvoiceService purchaseOrderInvoiceService;

  protected SaleOrderRepository saleOrderRepository;

  protected PurchaseOrderRepository purchaseOrderRepository;

  protected AccountingSituationSupplychainService accountingSituationSupplychainService;

  protected AppSupplychainService appSupplychainService;

  protected StockMoveInvoiceService stockMoveInvoiceService;

  protected UnitConversionService unitConversionService;

  protected AppBaseService appBaseService;

  protected SupplyChainConfigService supplyChainConfigService;

  protected StockMoveLineRepository stockMoveLineRepository;

  @Inject
  public WorkflowVentilationServiceSupplychainImpl(
      AccountConfigService accountConfigService,
      InvoicePaymentRepository invoicePaymentRepo,
      InvoicePaymentCreateService invoicePaymentCreateService,
      InvoiceService invoiceService,
      SaleOrderInvoiceService saleOrderInvoiceService,
      PurchaseOrderInvoiceService purchaseOrderInvoiceService,
      SaleOrderRepository saleOrderRepository,
      PurchaseOrderRepository purchaseOrderRepository,
      AccountingSituationSupplychainService accountingSituationSupplychainService,
      AppSupplychainService appSupplychainService,
      StockMoveInvoiceService stockMoveInvoiceService,
      UnitConversionService unitConversionService,
      AppBaseService appBaseService,
      SupplyChainConfigService supplyChainConfigService,
      StockMoveLineRepository stockMoveLineRepository,
      AppAccountService appAccountService,
      InvoiceFinancialDiscountService invoiceFinancialDiscountService,
      InvoiceTermService invoiceTermService) {

    super(
        accountConfigService,
        invoicePaymentRepo,
        invoicePaymentCreateService,
        invoiceService,
        appAccountService,
        invoiceFinancialDiscountService,
        invoiceTermService);
    this.saleOrderInvoiceService = saleOrderInvoiceService;
    this.purchaseOrderInvoiceService = purchaseOrderInvoiceService;
    this.saleOrderRepository = saleOrderRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.accountingSituationSupplychainService = accountingSituationSupplychainService;
    this.appSupplychainService = appSupplychainService;
    this.stockMoveInvoiceService = stockMoveInvoiceService;
    this.unitConversionService = unitConversionService;
    this.appBaseService = appBaseService;
    this.supplyChainConfigService = supplyChainConfigService;
    this.stockMoveLineRepository = stockMoveLineRepository;
  }

  public void afterVentilation(Invoice invoice) throws AxelorException {
    super.afterVentilation(invoice);
    if (InvoiceToolService.isPurchase(invoice)) {

      // Update amount invoiced on PurchaseOrder
      this.purchaseOrderProcess(invoice);

    } else {

      // Update amount remaining to invoiced on SaleOrder
      this.saleOrderProcess(invoice);
    }
    if (invoice.getInterco() || invoice.getCreatedByInterco()) {
      updateIntercoReference(invoice);
    }
    if (invoice.getStockMoveSet() != null && !invoice.getStockMoveSet().isEmpty()) {
      stockMoveProcess(invoice);
    }
  }

  /**
   * Update external reference in the corresponding interco invoice.
   *
   * @param invoice
   */
  protected void updateIntercoReference(Invoice invoice) {
    Invoice intercoInvoice =
        Beans.get(InvoiceRepository.class)
            .all()
            .filter("self.invoiceId = :invoiceId")
            .bind("invoiceId", invoice.getExternalReference())
            .fetchOne();
    if (intercoInvoice != null) {
      intercoInvoice.setExternalReference(invoice.getInvoiceId());
    }
  }

  protected void saleOrderProcess(Invoice invoice) throws AxelorException {

    // Get all different saleOrders from invoice
    Set<SaleOrder> saleOrderSet = new HashSet<>();

    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      SaleOrder saleOrder = null;
      saleOrder = this.saleOrderLineProcess(invoice, invoiceLine);
      if (saleOrder != null) {
        saleOrderSet.add(saleOrder);
      }
    }

    for (SaleOrder saleOrder : saleOrderSet) {
      log.debug("Update the invoiced amount of the sale order : {}", saleOrder.getSaleOrderSeq());
      saleOrderInvoiceService.update(saleOrder, invoice.getId(), false);
      saleOrderRepository.save(saleOrder);
      accountingSituationSupplychainService.updateUsedCredit(saleOrder.getClientPartner());

      // determine if the invoice is a balance invoice.
      if (saleOrder.getAmountInvoiced().compareTo(saleOrder.getExTaxTotal()) == 0
          && invoice.getOperationSubTypeSelect()
              != InvoiceRepository.OPERATION_SUB_TYPE_SUBSCRIPTION) {
        invoice.setOperationSubTypeSelect(InvoiceRepository.OPERATION_SUB_TYPE_BALANCE);
      }
    }
  }

  protected SaleOrder saleOrderLineProcess(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {

    SaleOrderLine saleOrderLine = invoiceLine.getSaleOrderLine();

    if (saleOrderLine == null) {
      return null;
    }

    SaleOrder saleOrder = saleOrderLine.getSaleOrder();

    // Update invoiced amount on sale order line
    BigDecimal invoicedAmountToAdd = invoiceLine.getExTaxTotal();

    // If is it a refund invoice, so we negate the amount invoiced
    if (InvoiceToolService.isRefund(invoiceLine.getInvoice())) {
      invoicedAmountToAdd = invoicedAmountToAdd.negate();
    }

    if (!invoice.getCurrency().equals(saleOrder.getCurrency())
        && saleOrderLine.getCompanyExTaxTotal().compareTo(BigDecimal.ZERO) != 0) {
      // If the sale order currency is different from the invoice currency, use company currency to
      // calculate a rate. This rate will be applied to sale order line
      BigDecimal currentCompanyInvoicedAmount = invoiceLine.getCompanyExTaxTotal();
      BigDecimal rate =
          currentCompanyInvoicedAmount.divide(
              saleOrderLine.getCompanyExTaxTotal(), 4, RoundingMode.HALF_UP);
      invoicedAmountToAdd = rate.multiply(saleOrderLine.getExTaxTotal());
    }

    saleOrderLine.setAmountInvoiced(saleOrderLine.getAmountInvoiced().add(invoicedAmountToAdd));

    return saleOrder;
  }

  protected void purchaseOrderProcess(Invoice invoice) throws AxelorException {

    // Get all different purchaseOrders from invoice
    Set<PurchaseOrder> purchaseOrderSet = new HashSet<>();

    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      PurchaseOrder purchaseOrder = null;
      purchaseOrder = this.purchaseOrderLineProcess(invoice, invoiceLine);
      if (purchaseOrder != null) {
        purchaseOrderSet.add(purchaseOrder);
      }
    }

    for (PurchaseOrder purchaseOrder : purchaseOrderSet) {
      log.debug(
          "Update the invoiced amount of the purchase order : {}",
          purchaseOrder.getPurchaseOrderSeq());
      purchaseOrder.setAmountInvoiced(
          purchaseOrderInvoiceService.getInvoicedAmount(purchaseOrder, invoice.getId(), false));
      purchaseOrderRepository.save(purchaseOrder);
    }
  }

  protected PurchaseOrder purchaseOrderLineProcess(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {

    PurchaseOrderLine purchaseOrderLine = invoiceLine.getPurchaseOrderLine();

    if (purchaseOrderLine == null) {
      return null;
    }

    PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();

    BigDecimal invoicedAmountToAdd = invoiceLine.getExTaxTotal();

    // If is it a refund invoice, so we negate the amount invoiced
    if (InvoiceToolService.isRefund(invoiceLine.getInvoice())) {
      invoicedAmountToAdd = invoicedAmountToAdd.negate();
    }

    // Update invoiced amount on purchase order line
    if (!invoice.getCurrency().equals(purchaseOrder.getCurrency())
        && purchaseOrderLine.getCompanyExTaxTotal().compareTo(BigDecimal.ZERO) != 0) {
      // If the purchase order currency is different from the invoice currency, use company currency
      // to calculate a rate. This rate will be applied to purchase order line
      BigDecimal currentCompanyInvoicedAmount = invoiceLine.getCompanyExTaxTotal();
      BigDecimal rate =
          currentCompanyInvoicedAmount.divide(
              purchaseOrderLine.getCompanyExTaxTotal(), 4, RoundingMode.HALF_UP);
      invoicedAmountToAdd = rate.multiply(purchaseOrderLine.getExTaxTotal());
    }

    purchaseOrderLine.setAmountInvoiced(
        purchaseOrderLine.getAmountInvoiced().add(invoicedAmountToAdd));

    return purchaseOrder;
  }

  protected void stockMoveProcess(Invoice invoice) throws AxelorException {
    // update qty invoiced in stock move line
    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      StockMoveLine stockMoveLine = invoiceLine.getStockMoveLine();
      if (stockMoveLine == null) {
        continue;
      }
      if (isStockMoveInvoicingPartiallyActivated(invoice, stockMoveLine)) {
        BigDecimal qty = stockMoveLine.getQtyInvoiced();
        StockMove stockMove = stockMoveLine.getStockMove();

        if (stockMoveInvoiceService.isInvoiceRefundingStockMove(stockMove, invoice)) {
          qty = qty.subtract(invoiceLine.getQty());
        } else {
          qty = qty.add(invoiceLine.getQty());
        }

        Unit movUnit = stockMoveLine.getUnit(), invUnit = invoiceLine.getUnit();
        try {
          qty =
              unitConversionService.convert(
                  invUnit, movUnit, qty, appBaseService.getNbDecimalDigitForQty(), null);
        } catch (AxelorException e) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(SupplychainExceptionMessage.STOCK_MOVE_INVOICE_QTY_INVONVERTIBLE_UNIT)
                  + "\n"
                  + e.getMessage());
        }

        if (stockMoveLine.getRealQty().compareTo(qty) >= 0) {
          stockMoveLine.setQtyInvoiced(qty);
        } else {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              String.format(
                  I18n.get(SupplychainExceptionMessage.STOCK_MOVE_INVOICE_QTY_MAX),
                  qty.setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP)
                      .toString(),
                  stockMoveLine
                      .getRealQty()
                      .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP)
                      .toString(),
                  stockMoveLine.getProductName()));
        }
      } else {
        // set qty invoiced to the maximum (or emptying it if refund) for all stock move lines
        boolean invoiceIsRefund =
            stockMoveInvoiceService.isInvoiceRefundingStockMove(
                stockMoveLine.getStockMove(), invoice);

        // This case happens if you mix into a single invoice refund and non-refund stock moves.
        if (invoiceLine.getQty().compareTo(BigDecimal.ZERO) < 0) {
          stockMoveLine.setQtyInvoiced(
              invoiceIsRefund ? stockMoveLine.getRealQty() : BigDecimal.ZERO);
        }
        // This is the most general case
        else {
          stockMoveLine.setQtyInvoiced(
              invoiceIsRefund ? BigDecimal.ZERO : stockMoveLine.getRealQty());
        }

        // search in sale/purchase order lines to set split stock move lines to invoiced.
        if (stockMoveLine.getSaleOrderLine() != null) {
          stockMoveLineRepository
              .all()
              .filter(
                  "self.saleOrderLine.id = :saleOrderLineId AND self.stockMove.id = :stockMoveId")
              .bind("saleOrderLineId", stockMoveLine.getSaleOrderLine().getId())
              .bind("stockMoveId", stockMoveLine.getStockMove().getId())
              .fetch()
              .forEach(
                  stockMvLine ->
                      stockMvLine.setQtyInvoiced(
                          invoiceIsRefund ? BigDecimal.ZERO : stockMvLine.getRealQty()));
        }
        if (stockMoveLine.getPurchaseOrderLine() != null) {
          stockMoveLineRepository
              .all()
              .filter(
                  "self.purchaseOrderLine.id = :purchaseOrderLineId AND self.stockMove.id = :stockMoveId")
              .bind("purchaseOrderLineId", stockMoveLine.getPurchaseOrderLine().getId())
              .bind("stockMoveId", stockMoveLine.getStockMove().getId())
              .fetch()
              .forEach(
                  stockMvLine ->
                      stockMvLine.setQtyInvoiced(
                          invoiceIsRefund ? BigDecimal.ZERO : stockMvLine.getRealQty()));
        }
      }
    }

    // update stock moves invoicing status
    for (StockMove stockMove : invoice.getStockMoveSet()) {
      stockMoveInvoiceService.computeStockMoveInvoicingStatus(stockMove);
    }
  }

  protected boolean isStockMoveInvoicingPartiallyActivated(
      Invoice invoice, StockMoveLine stockMoveLine) throws AxelorException {
    SupplyChainConfig supplyChainConfig =
        supplyChainConfigService.getSupplyChainConfig(invoice.getCompany());
    return stockMoveLine.getSaleOrderLine() != null
            && supplyChainConfig.getActivateOutStockMovePartialInvoicing()
        || stockMoveLine.getPurchaseOrderLine() != null
            && supplyChainConfig.getActivateIncStockMovePartialInvoicing();
  }
}
