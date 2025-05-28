package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.studio.db.AppBudget;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public class BudgetAmountToolServiceImpl implements BudgetAmountToolService {

  protected AppBudgetService appBudgetService;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public BudgetAmountToolServiceImpl(
      AppBudgetService appBudgetService, CurrencyScaleService currencyScaleService) {
    this.appBudgetService = appBudgetService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public BigDecimal getBudgetMaxAmount(PurchaseOrderLine purchaseOrderLine) {
    if (purchaseOrderLine == null) {
      return BigDecimal.ZERO;
    }

    return getPurchaseBudgetMaxAmount(
        purchaseOrderLine.getCompanyExTaxTotal(), purchaseOrderLine.getCompanyInTaxTotal());
  }

  @Override
  public BigDecimal getBudgetMaxAmount(SaleOrderLine saleOrderLine) {
    if (saleOrderLine == null) {
      return BigDecimal.ZERO;
    }

    return getSaleBudgetMaxAmount(
        saleOrderLine.getCompanyExTaxTotal(), saleOrderLine.getCompanyInTaxTotal());
  }

  @Override
  public BigDecimal getBudgetMaxAmount(InvoiceLine invoiceLine) throws AxelorException {
    if (invoiceLine == null || invoiceLine.getInvoice() == null) {
      return BigDecimal.ZERO;
    }

    if (InvoiceToolService.isPurchase(invoiceLine.getInvoice())) {
      return getPurchaseBudgetMaxAmount(
          invoiceLine.getCompanyExTaxTotal(), invoiceLine.getCompanyInTaxTotal());
    } else {
      return getSaleBudgetMaxAmount(
          invoiceLine.getCompanyExTaxTotal(), invoiceLine.getCompanyInTaxTotal());
    }
  }

  @Override
  public boolean manageTaxAmounts(Invoice invoice) throws AxelorException {
    if (invoice == null) {
      return false;
    }
    AppBudget appBudget = appBudgetService.getAppBudget();
    if (InvoiceToolService.isPurchase(invoice)) {
      return Optional.ofNullable(appBudget)
          .map(AppBudget::getIncludeTaxesOnPurchaseBudget)
          .orElse(false);
    } else {
      return Optional.ofNullable(appBudget)
          .map(AppBudget::getIncludeTaxesOnSaleBudget)
          .orElse(false);
    }
  }

  @Override
  public BigDecimal getBudgetMaxAmount(MoveLine moveLine) {
    String technicalTypeSelect =
        Optional.ofNullable(moveLine)
            .map(MoveLine::getAccount)
            .map(Account::getAccountType)
            .map(AccountType::getTechnicalTypeSelect)
            .orElse("");
    if (StringUtils.isEmpty(technicalTypeSelect)
        || !List.of(
                AccountTypeRepository.TYPE_CHARGE,
                AccountTypeRepository.TYPE_IMMOBILISATION,
                AccountTypeRepository.TYPE_INCOME)
            .contains(technicalTypeSelect)) {
      return BigDecimal.ZERO;
    }

    BigDecimal exTaxAmount = moveLine.getDebit().max(moveLine.getCredit());
    BigDecimal inTaxAmount = computeInTaxTotal(moveLine);

    if (AccountTypeRepository.TYPE_INCOME.equals(technicalTypeSelect)) {
      return getSaleBudgetMaxAmount(exTaxAmount, inTaxAmount);
    } else {
      return getPurchaseBudgetMaxAmount(exTaxAmount, inTaxAmount);
    }
  }

  protected BigDecimal computeInTaxTotal(MoveLine moveLine) {
    BigDecimal exTaxAmount = moveLine.getDebit().max(moveLine.getCredit());

    BigDecimal taxRate = BigDecimal.ONE;
    if (ObjectUtils.notEmpty(moveLine.getTaxLineSet())) {
      taxRate =
          taxRate.add(
              moveLine.getTaxLineSet().stream()
                  .map(TaxLine::getValue)
                  .map(value -> value.divide(new BigDecimal(100)))
                  .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
    return exTaxAmount
        .multiply(taxRate)
        .setScale(currencyScaleService.getScale(moveLine), RoundingMode.HALF_UP);
  }

  protected BigDecimal getSaleBudgetMaxAmount(BigDecimal exTaxTotal, BigDecimal inTaxTotal) {
    if (Optional.ofNullable(appBudgetService.getAppBudget())
        .map(AppBudget::getIncludeTaxesOnSaleBudget)
        .orElse(false)) {
      return inTaxTotal;
    }

    return exTaxTotal;
  }

  protected BigDecimal getPurchaseBudgetMaxAmount(BigDecimal exTaxTotal, BigDecimal inTaxTotal) {
    if (Optional.ofNullable(appBudgetService.getAppBudget())
        .map(AppBudget::getIncludeTaxesOnPurchaseBudget)
        .orElse(false)) {
      return inTaxTotal;
    }

    return exTaxTotal;
  }
}
