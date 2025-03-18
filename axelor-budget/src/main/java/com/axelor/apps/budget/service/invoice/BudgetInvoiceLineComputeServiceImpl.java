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
package com.axelor.apps.budget.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticService;
import com.axelor.apps.account.service.invoice.InvoiceLineCheckService;
import com.axelor.apps.account.service.invoice.attributes.InvoiceLineAttrsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductPriceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.businessproject.service.InvoiceLineProjectServiceImpl;
import com.axelor.apps.purchase.service.SupplierCatalogService;
import com.axelor.apps.supplychain.service.InvoiceLineSupplierCatalogService;
import com.google.inject.Inject;
import java.util.Map;

public class BudgetInvoiceLineComputeServiceImpl extends InvoiceLineProjectServiceImpl {

  protected BudgetToolsService budgetToolsService;
  protected AppBudgetService appBudgetService;

  @Inject
  public BudgetInvoiceLineComputeServiceImpl(
      CurrencyService currencyService,
      PriceListService priceListService,
      AppAccountService appAccountService,
      AccountManagementAccountService accountManagementAccountService,
      ProductCompanyService productCompanyService,
      InvoiceLineRepository invoiceLineRepo,
      AppBaseService appBaseService,
      AccountConfigService accountConfigService,
      InvoiceLineAnalyticService invoiceLineAnalyticService,
      SupplierCatalogService supplierCatalogService,
      TaxAccountService taxAccountService,
      InternationalService internationalService,
      InvoiceLineAttrsService invoiceLineAttrsService,
      CurrencyScaleService currencyScaleService,
      ProductPriceService productPriceService,
      FiscalPositionService fiscalPositionService,
      InvoiceLineCheckService invoiceLineCheckService,
      InvoiceLineSupplierCatalogService invoiceLineSupplierCatalogService,
      BudgetToolsService budgetToolsService,
      AppBudgetService appBudgetService) {
    super(
        currencyService,
        priceListService,
        appAccountService,
        accountManagementAccountService,
        productCompanyService,
        invoiceLineRepo,
        appBaseService,
        accountConfigService,
        invoiceLineAnalyticService,
        supplierCatalogService,
        taxAccountService,
        internationalService,
        invoiceLineAttrsService,
        currencyScaleService,
        productPriceService,
        fiscalPositionService,
        invoiceLineCheckService,
        invoiceLineSupplierCatalogService);
    this.budgetToolsService = budgetToolsService;
    this.appBudgetService = appBudgetService;
  }

  @Override
  public Map<String, Object> compute(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {
    Map<String, Object> invoiceLineMap = super.compute(invoice, invoiceLine);

    if (appBudgetService.isApp("budget")) {
      invoiceLine.setBudgetRemainingAmountToAllocate(
          budgetToolsService.getBudgetRemainingAmountToAllocate(
              invoiceLine.getBudgetDistributionList(), invoiceLine.getCompanyExTaxTotal()));
      invoiceLineMap.put(
          "budgetRemainingAmountToAllocate", invoiceLine.getBudgetRemainingAmountToAllocate());
    }

    return invoiceLineMap;
  }
}
