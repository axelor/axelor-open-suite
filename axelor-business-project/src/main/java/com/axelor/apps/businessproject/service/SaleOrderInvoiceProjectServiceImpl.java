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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
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
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SaleOrderInvoiceProjectServiceImpl extends SaleOrderInvoiceServiceImpl {

  protected AppBusinessProjectService appBusinessProjectService;

  @Inject
  public SaleOrderInvoiceProjectServiceImpl(
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
      SaleOrderDeliveryAddressService saleOrderDeliveryAddressService,
      AppBusinessProjectService appBusinessProjectService) {
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
    this.appBusinessProjectService = appBusinessProjectService;
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
      SaleOrder saleOrder,
      Project project)
      throws AxelorException {
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
    if (project != null
        && !appBusinessProjectService.getAppBusinessProject().getProjectInvoiceLines()) {
      invoiceMerged.setProject(project);
      for (InvoiceLine invoiceLine : invoiceMerged.getInvoiceLineList()) {
        invoiceLine.setProject(project);
      }
    }
    return invoiceMerged;
  }

  @Override
  public List<InvoiceLine> createInvoiceLine(
      Invoice invoice, SaleOrderLine saleOrderLine, BigDecimal qtyToInvoice)
      throws AxelorException {
    if (saleOrderLine.getInvoicingModeSelect() != SaleOrderLineRepository.INVOICING_MODE_STANDARD) {
      return List.of();
    }
    List<InvoiceLine> invoiceLines = super.createInvoiceLine(invoice, saleOrderLine, qtyToInvoice);

    if (!appBusinessProjectService.isApp("business-project")) {
      return invoiceLines;
    }

    for (InvoiceLine invoiceLine : invoiceLines) {
      if (saleOrderLine != null) {
        invoiceLine.setProject(saleOrderLine.getProject());
      }
    }
    return invoiceLines;
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
    Invoice invoice =
        super.generateInvoice(
            saleOrder, operationSelect, amount, isPercent, qtyToInvoiceMap, timetableIdList);
    Project project = saleOrder.getProject();
    if (project != null) {
      invoice.setProject(project);
    }
    invoiceRepo.save(invoice);
    return invoice;
  }

  @Override
  public List<Map<String, Object>> getSaleOrderLineList(SaleOrder saleOrder) {
    List<Map<String, Object>> saleOrderLineList = super.getSaleOrderLineList(saleOrder);
    List<Map<String, Object>> filteredSaleOrderLineList = new ArrayList<>();
    for (Map<String, Object> saleOrderLineMap : saleOrderLineList) {
      int invoicingModeSelect = (int) saleOrderLineMap.get("invoicingModeSelect");
      if (invoicingModeSelect == SaleOrderLineRepository.INVOICING_MODE_STANDARD) {
        filteredSaleOrderLineList.add(saleOrderLineMap);
      }
    }
    return filteredSaleOrderLineList;
  }
}
