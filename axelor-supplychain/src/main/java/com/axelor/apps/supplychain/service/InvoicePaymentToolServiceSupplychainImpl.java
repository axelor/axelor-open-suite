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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class InvoicePaymentToolServiceSupplychainImpl extends InvoicePaymentToolServiceImpl {

  protected PartnerSupplychainService partnerSupplychainService;
  protected SaleOrderComputeService saleOrderComputeService;
  protected PurchaseOrderService purchaseOrderService;

  @Inject
  public InvoicePaymentToolServiceSupplychainImpl(
      InvoiceRepository invoiceRepo,
      MoveToolService moveToolService,
      InvoicePaymentRepository invoicePaymentRepo,
      InvoiceTermService invoiceTermService,
      InvoiceTermPaymentService invoiceTermPaymentService,
      CurrencyService currencyService,
      PartnerSupplychainService partnerSupplychainService,
      SaleOrderComputeService saleOrderComputeService,
      PurchaseOrderService purchaseOrderService,
      AppAccountService appAccountService) {
    super(
        invoiceRepo,
        moveToolService,
        invoicePaymentRepo,
        invoiceTermService,
        invoiceTermPaymentService,
        currencyService,
        appAccountService);
    this.partnerSupplychainService = partnerSupplychainService;
    this.saleOrderComputeService = saleOrderComputeService;
    this.purchaseOrderService = purchaseOrderService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateAmountPaid(Invoice invoice) throws AxelorException {
    super.updateAmountPaid(invoice);

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return;
    }
    SaleOrder saleOrder = invoice.getSaleOrder();
    PurchaseOrder purchaseOrder = invoice.getPurchaseOrder();
    if (saleOrder != null) {
      // compute sale order totals
      saleOrderComputeService._computeSaleOrder(saleOrder);
    }
    if (purchaseOrder != null) {
      purchaseOrderService._computePurchaseOrder(purchaseOrder);
    }
    if (invoice.getPartner().getHasBlockedAccount()
        && !invoice.getPartner().getHasManuallyBlockedAccount()) {
      partnerSupplychainService.updateBlockedAccount(invoice.getPartner());
    }
  }
}
