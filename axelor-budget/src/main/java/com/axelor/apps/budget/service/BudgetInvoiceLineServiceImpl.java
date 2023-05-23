package com.axelor.apps.budget.service;

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
import com.axelor.apps.budget.exception.IExceptionMessage;
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

  protected BudgetBudgetService budgetBudgetService;
  protected BudgetRepository budgetRepository;
  protected BudgetBudgetDistributionService budgetBudgetDistributionService;

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
      BudgetBudgetService budgetBudgetService,
      BudgetRepository budgetRepository,
      BudgetBudgetDistributionService budgetBudgetDistributionService) {
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
    this.budgetBudgetService = budgetBudgetService;
    this.budgetRepository = budgetRepository;
    this.budgetBudgetDistributionService = budgetBudgetDistributionService;
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(InvoiceLine invoiceLine) {
    if (invoiceLine == null || invoiceLine.getInvoice() == null) {
      return "";
    }
    invoiceLine.clearBudgetDistributionList();
    String alertMessage =
        budgetBudgetDistributionService.createBudgetDistribution(
            invoiceLine.getAnalyticMoveLineList(),
            invoiceLine.getAccount(),
            invoiceLine.getInvoice().getCompany(),
            invoiceLine.getInvoice().getInvoiceDate() != null
                ? invoiceLine.getInvoice().getInvoiceDate()
                : invoiceLine.getInvoice().getCreatedOn().toLocalDate(),
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
              I18n.get(IExceptionMessage.BUDGET_DISTRIBUTION_LINE_SUM_GREATER_INVOICE),
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
        budgetBudgetDistributionService.computeBudgetDistributionSumAmount(
            budgetDistribution, computeDate);
      }
    }
    invoiceLine.setBudgetDistributionSumAmount(budgetDistributionSumAmount);
  }
}
