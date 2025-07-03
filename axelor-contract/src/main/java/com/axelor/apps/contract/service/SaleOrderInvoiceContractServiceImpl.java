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

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderDeliveryAddressService;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderWorkflowService;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.service.CommonInvoiceService;
import com.axelor.apps.supplychain.service.SaleInvoicingStateService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceTaxService;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineOrderService;
import com.axelor.apps.supplychain.service.order.OrderInvoiceService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderInvoiceServiceImpl;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SaleOrderInvoiceContractServiceImpl extends SaleOrderInvoiceServiceImpl {
  @Inject
  public SaleOrderInvoiceContractServiceImpl(
      AppBaseService appBaseService,
      AppStockService appStockService,
      AppSupplychainService appSupplychainService,
      SaleOrderRepository saleOrderRepo,
      InvoiceRepository invoiceRepo,
      InvoiceServiceSupplychainImpl invoiceService,
      StockMoveRepository stockMoveRepository,
      SaleOrderWorkflowService saleOrderWorkflowService,
      InvoiceTermService invoiceTermService,
      CommonInvoiceService commonInvoiceService,
      InvoiceLineOrderService invoiceLineOrderService,
      SaleInvoicingStateService saleInvoicingStateService,
      CurrencyScaleService currencyScaleService,
      OrderInvoiceService orderInvoiceService,
      InvoiceTaxService invoiceTaxService,
      SaleOrderDeliveryAddressService saleOrderDeliveryAddressService) {
    super(
        appBaseService,
        appStockService,
        appSupplychainService,
        saleOrderRepo,
        invoiceRepo,
        invoiceService,
        stockMoveRepository,
        saleOrderWorkflowService,
        invoiceTermService,
        commonInvoiceService,
        invoiceLineOrderService,
        saleInvoicingStateService,
        currencyScaleService,
        orderInvoiceService,
        invoiceTaxService,
        saleOrderDeliveryAddressService);
  }

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
    Set<Contract> contracts =
        invoiceList.stream()
            .map(Invoice::getContractSet)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    Invoice invoiceMerged =
        super.mergeInvoice(
            invoiceList,
            company,
            currency,
            partner,
            contactPartner,
            priceList,
            paymentMode,
            paymentCondition,
            tradingName,
            fiscalPosition,
            saleOrder);

    invoiceMerged.setContractSet(contracts);
    return invoiceMerged;
  }
}
