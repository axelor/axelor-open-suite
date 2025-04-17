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
package com.axelor.apps.budget.service.purchaseorder;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.address.AddressService;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.repo.BudgetDistributionRepository;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.businessproject.service.PurchaseOrderInvoiceProjectServiceImpl;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.supplychain.db.repo.TimetableRepository;
import com.axelor.apps.supplychain.service.CommonInvoiceService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychain;
import com.axelor.apps.supplychain.service.invoice.InvoiceTaxService;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineOrderService;
import com.axelor.apps.supplychain.service.order.OrderInvoiceService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;

public class PurchaseOrderInvoiceBudgetServiceImpl extends PurchaseOrderInvoiceProjectServiceImpl {

  protected BudgetDistributionRepository budgetDistributionRepository;
  protected BudgetToolsService budgetToolsService;
  protected AppBudgetService appBudgetService;

  @Inject
  public PurchaseOrderInvoiceBudgetServiceImpl(
      InvoiceServiceSupplychain invoiceServiceSupplychain,
      InvoiceService invoiceService,
      InvoiceRepository invoiceRepo,
      TimetableRepository timetableRepo,
      AppSupplychainService appSupplychainService,
      AccountConfigService accountConfigService,
      CommonInvoiceService commonInvoiceService,
      AddressService addressService,
      InvoiceLineOrderService invoiceLineOrderService,
      CurrencyService currencyService,
      CurrencyScaleService currencyScaleService,
      OrderInvoiceService orderInvoiceService,
      InvoiceTaxService invoiceTaxService,
      PriceListService priceListService,
      PurchaseOrderLineService purchaseOrderLineService,
      AppBusinessProjectService appBusinessProjectService,
      ProductCompanyService productCompanyService,
      BudgetDistributionRepository budgetDistributionRepository,
      BudgetToolsService budgetToolsService,
      AppBudgetService appBudgetService) {
    super(
        invoiceServiceSupplychain,
        invoiceService,
        invoiceRepo,
        timetableRepo,
        appSupplychainService,
        accountConfigService,
        commonInvoiceService,
        addressService,
        invoiceLineOrderService,
        currencyService,
        currencyScaleService,
        orderInvoiceService,
        invoiceTaxService,
        priceListService,
        purchaseOrderLineService,
        appBusinessProjectService,
        productCompanyService);
    this.budgetDistributionRepository = budgetDistributionRepository;
    this.budgetToolsService = budgetToolsService;
    this.appBudgetService = appBudgetService;
  }

  @Override
  public void processPurchaseOrderLine(
      Invoice invoice, List<InvoiceLine> invoiceLineList, PurchaseOrderLine purchaseOrderLine)
      throws AxelorException {
    super.processPurchaseOrderLine(invoice, invoiceLineList, purchaseOrderLine);

    if (appBudgetService.isApp("budget")) {
      invoiceLineList = copyBudgetDistribution(invoiceLineList, purchaseOrderLine);
    }
  }

  protected List<InvoiceLine> copyBudgetDistribution(
      List<InvoiceLine> invoiceLineList, PurchaseOrderLine purchaseOrderLine) {
    if (ObjectUtils.isEmpty(invoiceLineList)) {
      return invoiceLineList;
    }

    for (InvoiceLine invoiceLine : invoiceLineList) {
      if (invoiceLine.getPurchaseOrderLine() != null
          && Objects.equals(invoiceLine.getPurchaseOrderLine(), purchaseOrderLine)) {
        invoiceLine.setBudget(purchaseOrderLine.getBudget());
        invoiceLine.setBudgetRemainingAmountToAllocate(
            purchaseOrderLine.getBudgetRemainingAmountToAllocate());
        invoiceLine.setBudgetFromDate(purchaseOrderLine.getBudgetFromDate());
        invoiceLine.setBudgetToDate(purchaseOrderLine.getBudgetToDate());

        if (!ObjectUtils.isEmpty(purchaseOrderLine.getBudgetDistributionList())) {
          for (BudgetDistribution budgetDistribution :
              purchaseOrderLine.getBudgetDistributionList()) {
            BudgetDistribution copyBudgetDistribution = new BudgetDistribution();
            copyBudgetDistribution.setBudget(budgetDistribution.getBudget());
            copyBudgetDistribution.setAmount(budgetDistribution.getAmount());
            copyBudgetDistribution.setBudgetAmountAvailable(
                budgetDistribution.getBudgetAmountAvailable());
            invoiceLine.addBudgetDistributionListItem(copyBudgetDistribution);
          }
          invoiceLine.setBudgetRemainingAmountToAllocate(
              budgetToolsService.getBudgetRemainingAmountToAllocate(
                  invoiceLine.getBudgetDistributionList(), invoiceLine.getCompanyExTaxTotal()));
        }
      }
    }
    return invoiceLineList;
  }
}
