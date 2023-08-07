/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.businessproject.service.InvoiceLineProjectServiceImpl;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.SupplierCatalogService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RequestScoped
public class BudgetInvoiceLineServiceImpl extends InvoiceLineProjectServiceImpl
    implements BudgetInvoiceLineService {

  protected BudgetService budgetService;
  protected BudgetRepository budgetRepository;
  protected BudgetDistributionService budgetDistributionService;

  @Inject
  public BudgetInvoiceLineServiceImpl(
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
      TaxService taxService,
      InternationalService internationalService,
      BudgetService budgetService,
      BudgetRepository budgetRepository,
      BudgetDistributionService budgetDistributionService) {
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
        taxService,
        internationalService);
    this.budgetService = budgetService;
    this.budgetRepository = budgetRepository;
    this.budgetDistributionService = budgetDistributionService;
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(Invoice invoice, InvoiceLine invoiceLine) {
    if (invoice == null || invoiceLine == null) {
      return "";
    }
    invoiceLine.clearBudgetDistributionList();
    String alertMessage =
        budgetDistributionService.createBudgetDistribution(
            invoiceLine.getAnalyticMoveLineList(),
            invoiceLine.getAccount(),
            invoice.getCompany(),
            invoice.getInvoiceDate() != null
                ? invoice.getInvoiceDate()
                : invoice.getCreatedOn().toLocalDate(),
            invoiceLine.getCompanyExTaxTotal(),
            invoiceLine.getName(),
            invoiceLine);

    invoiceLineRepo.save(invoiceLine);
    return alertMessage;
  }

  @Override
  public void checkAmountForInvoiceLine(InvoiceLine invoiceLine) throws AxelorException {
    if (invoiceLine.getBudgetDistributionList() != null
        && !invoiceLine.getBudgetDistributionList().isEmpty()) {
      for (BudgetDistribution budgetDistribution : invoiceLine.getBudgetDistributionList()) {
        if (budgetDistribution.getAmount().compareTo(invoiceLine.getCompanyExTaxTotal()) > 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BudgetExceptionMessage.BUDGET_DISTRIBUTION_LINE_SUM_GREATER_INVOICE),
              budgetDistribution.getBudget().getCode(),
              invoiceLine.getProduct().getCode());
        }
      }
    }
  }

  @Override
  public void computeBudgetDistributionSumAmount(InvoiceLine invoiceLine, Invoice invoice) {
    List<BudgetDistribution> budgetDistributionList = invoiceLine.getBudgetDistributionList();
    PurchaseOrderLine purchaseOrderLine = invoiceLine.getPurchaseOrderLine();
    BigDecimal budgetDistributionSumAmount = BigDecimal.ZERO;
    LocalDate computeDate = invoice.getInvoiceDate();

    if (purchaseOrderLine != null && purchaseOrderLine.getPurchaseOrder().getOrderDate() != null) {
      computeDate = purchaseOrderLine.getPurchaseOrder().getOrderDate();
    }

    if (budgetDistributionList != null && !budgetDistributionList.isEmpty()) {

      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        budgetDistributionSumAmount =
            budgetDistributionSumAmount.add(budgetDistribution.getAmount());
        budgetDistributionService.computeBudgetDistributionSumAmount(
            budgetDistribution, computeDate);
      }
    }
    invoiceLine.setBudgetDistributionSumAmount(budgetDistributionSumAmount);
  }
}
