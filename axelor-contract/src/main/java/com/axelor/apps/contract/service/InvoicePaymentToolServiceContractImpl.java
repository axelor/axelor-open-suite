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
package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermFilterService;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentFinancialDiscountService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentToolService;
import com.axelor.apps.account.service.reconcile.foreignexchange.ForeignExchangeGapToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.supplychain.service.InvoicePaymentToolServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PartnerSupplychainService;
import com.google.inject.Inject;

public class InvoicePaymentToolServiceContractImpl
    extends InvoicePaymentToolServiceSupplychainImpl {

  protected AppBaseService appBaseService;
  protected ContractVersionService contractVersionService;

  @Inject
  public InvoicePaymentToolServiceContractImpl(
      InvoiceRepository invoiceRepo,
      MoveToolService moveToolService,
      InvoicePaymentRepository invoicePaymentRepo,
      InvoiceTermPaymentService invoiceTermPaymentService,
      CurrencyService currencyService,
      PartnerSupplychainService partnerSupplychainService,
      SaleOrderComputeService saleOrderComputeService,
      PurchaseOrderService purchaseOrderService,
      AppAccountService appAccountService,
      InvoicePaymentFinancialDiscountService invoicePaymentFinancialDiscountService,
      CurrencyScaleService currencyScaleService,
      InvoiceTermFilterService invoiceTermFilterService,
      InvoiceTermToolService invoiceTermToolService,
      InvoiceTermPaymentToolService invoiceTermPaymentToolService,
      ForeignExchangeGapToolService foreignExchangeGapToolService,
      AppBaseService appBaseService,
      ContractVersionService contractVersionService) {
    super(
        invoiceRepo,
        moveToolService,
        invoicePaymentRepo,
        invoiceTermPaymentService,
        currencyService,
        partnerSupplychainService,
        saleOrderComputeService,
        purchaseOrderService,
        appAccountService,
        invoicePaymentFinancialDiscountService,
        currencyScaleService,
        invoiceTermFilterService,
        invoiceTermToolService,
        invoiceTermPaymentToolService,
        foreignExchangeGapToolService);
    this.appBaseService = appBaseService;
    this.contractVersionService = contractVersionService;
  }

  @Override
  public void updateAmountPaid(Invoice invoice) throws AxelorException {
    super.updateAmountPaid(invoice);
    if (!appBaseService.isApp("contract")) {
      return;
    }
    ContractVersion contractVersion = contractVersionService.getContractVersion(invoice);
    if (contractVersion == null) {
      return;
    }
    contractVersionService.computeTotalPaidAmount(contractVersion);
  }
}
