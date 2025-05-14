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
package com.axelor.apps.supplychain.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.service.invoice.tax.InvoiceLineTaxRecordService;
import com.axelor.apps.account.service.invoice.tax.InvoiceTaxComputeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.interfaces.OrderLineTax;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import com.axelor.apps.purchase.service.PurchaseOrderLineTaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLineTax;
import com.axelor.apps.sale.service.saleorderline.tax.SaleOrderLineCreateTaxLineService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;

public class InvoiceTaxServiceImpl implements InvoiceTaxService {

  protected SaleOrderLineCreateTaxLineService saleOrderLineCreateTaxLineService;
  protected PurchaseOrderLineTaxService purchaseOrderLineTaxService;
  protected InvoiceLineTaxRecordService invoiceLineTaxRecordService;
  protected InvoiceTaxComputeService invoiceTaxComputeService;

  @Inject
  public InvoiceTaxServiceImpl(
      SaleOrderLineCreateTaxLineService saleOrderLineCreateTaxLineService,
      PurchaseOrderLineTaxService purchaseOrderLineTaxService,
      InvoiceLineTaxRecordService invoiceLineTaxRecordService,
      InvoiceTaxComputeService invoiceTaxComputeService) {
    this.saleOrderLineCreateTaxLineService = saleOrderLineCreateTaxLineService;
    this.purchaseOrderLineTaxService = purchaseOrderLineTaxService;
    this.invoiceLineTaxRecordService = invoiceLineTaxRecordService;
    this.invoiceTaxComputeService = invoiceTaxComputeService;
  }

  @Override
  public void manageTaxByAmount(SaleOrder saleOrder, Invoice invoice) throws AxelorException {
    if (saleOrder == null
        || invoice == null
        || ObjectUtils.isEmpty(invoice.getInvoiceLineTaxList())) {
      return;
    }

    List<SaleOrderLineTax> updatedSaleOrderLineTaxList =
        saleOrderLineCreateTaxLineService.getUpdatedSaleOrderLineTax(saleOrder);
    if (ObjectUtils.isEmpty(updatedSaleOrderLineTaxList)) {
      return;
    }

    boolean isInvoiceLineTaxUpdated = false;
    for (SaleOrderLineTax saleOrderLineTax : updatedSaleOrderLineTaxList) {
      if (updateInvoiceLineTaxAmounts(invoice, saleOrderLineTax)) {
        isInvoiceLineTaxUpdated = true;
      }
    }

    if (isInvoiceLineTaxUpdated) {
      invoiceTaxComputeService.recomputeInvoiceTaxAmounts(invoice);
    }
  }

  @Override
  public void manageTaxByAmount(PurchaseOrder purchaseOrder, Invoice invoice)
      throws AxelorException {
    if (purchaseOrder == null
        || invoice == null
        || ObjectUtils.isEmpty(invoice.getInvoiceLineTaxList())) {
      return;
    }

    List<PurchaseOrderLineTax> updatedPurchaseOrderLineTaxList =
        purchaseOrderLineTaxService.getUpdatedPurchaseOrderLineTax(purchaseOrder);
    if (ObjectUtils.isEmpty(updatedPurchaseOrderLineTaxList)) {
      return;
    }

    boolean isInvoiceLineTaxUpdated = false;
    for (PurchaseOrderLineTax purchaseOrderLineTax : updatedPurchaseOrderLineTaxList) {
      if (updateInvoiceLineTaxAmounts(invoice, purchaseOrderLineTax)) {
        isInvoiceLineTaxUpdated = true;
      }
    }

    if (isInvoiceLineTaxUpdated) {
      invoiceTaxComputeService.recomputeInvoiceTaxAmounts(invoice);
    }
  }

  protected boolean updateInvoiceLineTaxAmounts(Invoice invoice, OrderLineTax orderLineTax)
      throws AxelorException {
    InvoiceLineTax invoiceLineTax = getExistingInvoiceLineTax(orderLineTax, invoice);
    if (invoiceLineTax != null) {
      invoiceLineTax.setTaxTotal(orderLineTax.getTaxTotal());
      invoiceLineTaxRecordService.recomputeAmounts(invoiceLineTax, invoice);
      return true;
    }

    return false;
  }

  protected InvoiceLineTax getExistingInvoiceLineTax(OrderLineTax orderLineTax, Invoice invoice) {
    if (ObjectUtils.isEmpty(invoice.getInvoiceLineTaxList()) || orderLineTax == null) {
      return null;
    }

    for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()) {
      if (Objects.equals(invoiceLineTax.getTaxLine(), orderLineTax.getTaxLine())
          && orderLineTax.getPercentageTaxTotal().compareTo(invoiceLineTax.getTaxTotal()) == 0
          && invoiceLineTax.getExTaxBase().compareTo(orderLineTax.getExTaxBase()) == 0) {
        return invoiceLineTax;
      }
    }

    return null;
  }
}
