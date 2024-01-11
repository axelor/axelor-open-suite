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
package com.axelor.apps.supplychain.service.invoice.generator;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.inject.Beans;

public abstract class InvoiceGeneratorSupplyChain extends InvoiceGenerator {

  protected SaleOrder saleOrder;

  protected PurchaseOrder purchaseOrder;

  protected InvoiceGeneratorSupplyChain(SaleOrder saleOrder) throws AxelorException {
    this(saleOrder, false);
  }

  protected InvoiceGeneratorSupplyChain(SaleOrder saleOrder, boolean isRefund)
      throws AxelorException {
    super(
        isRefund
            ? InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND
            : InvoiceRepository.OPERATION_TYPE_CLIENT_SALE,
        saleOrder.getCompany(),
        saleOrder.getPaymentCondition(),
        isRefund ? saleOrder.getClientPartner().getOutPaymentMode() : saleOrder.getPaymentMode(),
        saleOrder.getMainInvoicingAddress(),
        saleOrder.getInvoicedPartner() != null
            ? saleOrder.getInvoicedPartner()
            : saleOrder.getClientPartner(),
        saleOrder.getContactPartner(),
        saleOrder.getCurrency(),
        saleOrder.getPriceList(),
        saleOrder.getSaleOrderSeq(),
        saleOrder.getExternalReference(),
        saleOrder.getInAti(),
        saleOrder.getCompanyBankDetails(),
        saleOrder.getTradingName(),
        saleOrder.getGroupProductsOnPrintings());
    this.saleOrder = saleOrder;
  }

  protected InvoiceGeneratorSupplyChain(PurchaseOrder purchaseOrder) throws AxelorException {
    this(purchaseOrder, false);
  }

  protected InvoiceGeneratorSupplyChain(PurchaseOrder purchaseOrder, boolean isRefund)
      throws AxelorException {
    super(
        isRefund
            ? InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND
            : InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE,
        purchaseOrder.getCompany(),
        purchaseOrder.getPaymentCondition(),
        isRefund
            ? purchaseOrder.getSupplierPartner().getInPaymentMode()
            : purchaseOrder.getPaymentMode(),
        null,
        purchaseOrder.getSupplierPartner(),
        purchaseOrder.getContactPartner(),
        purchaseOrder.getCurrency(),
        purchaseOrder.getPriceList(),
        purchaseOrder.getPurchaseOrderSeq(),
        purchaseOrder.getExternalReference(),
        purchaseOrder.getInAti(),
        purchaseOrder.getCompanyBankDetails(),
        purchaseOrder.getTradingName(),
        purchaseOrder.getGroupProductsOnPrintings());
    this.purchaseOrder = purchaseOrder;
  }

  /**
   * PaymentCondition, Paymentmode, MainInvoicingAddress, Currency récupérés du tiers
   *
   * @param operationType
   * @param company
   * @param partner
   * @param contactPartner
   * @throws AxelorException
   */
  protected InvoiceGeneratorSupplyChain(StockMove stockMove, int invoiceOperationType)
      throws AxelorException {

    super(
        invoiceOperationType,
        stockMove.getCompany(),
        stockMove.getInvoicedPartner() != null
            ? stockMove.getInvoicedPartner()
            : stockMove.getPartner(),
        null,
        null,
        stockMove.getStockMoveSeq(),
        stockMove.getTrackingNumber(),
        null,
        stockMove.getTradingName());

    this.groupProductsOnPrintings = stockMove.getGroupProductsOnPrintings();
    if (StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())
        && stockMove.getOriginId() != null) {
      saleOrder = Beans.get(SaleOrderRepository.class).find(stockMove.getOriginId());
    }
  }

  @Override
  protected Invoice createInvoiceHeader() throws AxelorException {

    Invoice invoice = super.createInvoiceHeader();

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return invoice;
    }

    if (saleOrder != null) {
      invoice.setPrintingSettings(saleOrder.getPrintingSettings());
      invoice.setFiscalPosition(saleOrder.getFiscalPosition());
    } else if (purchaseOrder != null) {
      invoice.setPrintingSettings(purchaseOrder.getPrintingSettings());
      invoice.setFiscalPosition(purchaseOrder.getFiscalPosition());
    }

    if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE
        || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND) {

      AccountConfig accountConfig = Beans.get(AccountConfigService.class).getAccountConfig(company);
      invoice.setDisplayStockMoveOnInvoicePrinting(
          accountConfig.getDisplayStockMoveOnInvoicePrinting());
    }

    return invoice;
  }
}
