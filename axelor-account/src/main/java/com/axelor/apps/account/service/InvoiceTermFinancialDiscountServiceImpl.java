package com.axelor.apps.account.service;

import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermFinancialDiscountService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class InvoiceTermFinancialDiscountServiceImpl
    implements InvoiceTermFinancialDiscountService {
  protected AppAccountService appAccountService;

  @Inject
  public InvoiceTermFinancialDiscountServiceImpl(AppAccountService appAccountService) {
    this.appAccountService = appAccountService;
  }

  @Override
  public void computeFinancialDiscount(InvoiceTerm invoiceTerm, Invoice invoice) {
    this.computeFinancialDiscount(
        invoiceTerm,
        invoice.getInTaxTotal(),
        invoice.getFinancialDiscount(),
        invoice.getFinancialDiscountTotalAmount(),
        invoice.getRemainingAmountAfterFinDiscount());
  }

  @Override
  public void computeFinancialDiscount(
      InvoiceTerm invoiceTerm,
      BigDecimal totalAmount,
      FinancialDiscount financialDiscount,
      BigDecimal financialDiscountAmount,
      BigDecimal remainingAmountAfterFinDiscount) {
    if (appAccountService.getAppAccount().getManageFinancialDiscount()
        && financialDiscount != null) {
      BigDecimal percentage =
          this.computeCustomizedPercentageUnscaled(invoiceTerm.getAmount(), totalAmount)
              .divide(
                  BigDecimal.valueOf(100),
                  AppBaseService.COMPUTATION_SCALING,
                  RoundingMode.HALF_UP);

      invoiceTerm.setApplyFinancialDiscount(true);
      invoiceTerm.setFinancialDiscount(financialDiscount);
      invoiceTerm.setFinancialDiscountDeadlineDate(
          this.computeFinancialDiscountDeadlineDate(invoiceTerm));
      invoiceTerm.setRemainingAmountAfterFinDiscount(
          remainingAmountAfterFinDiscount
              .multiply(percentage)
              .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP));
      invoiceTerm.setFinancialDiscountAmount(
          invoiceTerm.getAmount().subtract(invoiceTerm.getRemainingAmountAfterFinDiscount()));

      this.computeAmountRemainingAfterFinDiscount(invoiceTerm);

      invoiceTerm.setFinancialDiscountDeadlineDate(
          this.computeFinancialDiscountDeadlineDate(invoiceTerm));
    } else {
      invoiceTerm.setApplyFinancialDiscount(false);
      invoiceTerm.setFinancialDiscount(null);
      invoiceTerm.setFinancialDiscountDeadlineDate(null);
      invoiceTerm.setFinancialDiscountAmount(BigDecimal.ZERO);
      invoiceTerm.setRemainingAmountAfterFinDiscount(BigDecimal.ZERO);
      invoiceTerm.setAmountRemainingAfterFinDiscount(BigDecimal.ZERO);
    }
  }

  @Override
  public BigDecimal computeCustomizedPercentageUnscaled(BigDecimal amount, BigDecimal inTaxTotal) {
    BigDecimal percentage = BigDecimal.ZERO;
    if (inTaxTotal.compareTo(BigDecimal.ZERO) != 0) {
      percentage =
          amount
              .multiply(new BigDecimal(100))
              .divide(inTaxTotal, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
    }
    return percentage;
  }

  @Override
  public void computeAmountRemainingAfterFinDiscount(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getAmount().signum() > 0) {
      invoiceTerm.setAmountRemainingAfterFinDiscount(
          invoiceTerm
              .getAmountRemaining()
              .multiply(invoiceTerm.getRemainingAmountAfterFinDiscount())
              .divide(
                  invoiceTerm.getAmount(),
                  AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                  RoundingMode.HALF_UP));
    }
  }

  @Override
  public LocalDate computeFinancialDiscountDeadlineDate(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getDueDate() == null || invoiceTerm.getFinancialDiscount() == null) {
      return null;
    }

    LocalDate deadlineDate =
        invoiceTerm.getDueDate().minusDays(invoiceTerm.getFinancialDiscount().getDiscountDelay());

    if (invoiceTerm.getInvoice() != null && invoiceTerm.getInvoice().getInvoiceDate() != null) {
      LocalDate invoiceDate = invoiceTerm.getInvoice().getInvoiceDate();
      deadlineDate = deadlineDate.isBefore(invoiceDate) ? invoiceDate : deadlineDate;
    } else if (invoiceTerm.getMoveLine() != null && invoiceTerm.getMoveLine().getDate() != null) {
      LocalDate moveDate = invoiceTerm.getMoveLine().getDate();
      deadlineDate = deadlineDate.isBefore(moveDate) ? moveDate : deadlineDate;
    }

    return deadlineDate;
  }

  @Override
  public BigDecimal getFinancialDiscountTaxAmount(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getFinancialDiscount() == null) {
      return BigDecimal.ZERO;
    }

    BigDecimal taxTotal = this.getTaxTotal(invoiceTerm);

    if (taxTotal.signum() == 0) {
      return BigDecimal.ZERO;
    } else if (invoiceTerm.getFinancialDiscount().getDiscountBaseSelect()
        == FinancialDiscountRepository.DISCOUNT_BASE_VAT) {
      return taxTotal
          .multiply(invoiceTerm.getPercentage())
          .multiply(invoiceTerm.getFinancialDiscount().getDiscountRate())
          .divide(
              BigDecimal.valueOf(10000),
              AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
              RoundingMode.HALF_UP);
    } else {
      BigDecimal exTaxTotal;

      if (invoiceTerm.getInvoice() != null) {
        exTaxTotal = invoiceTerm.getInvoice().getExTaxTotal();
      } else {
        exTaxTotal = invoiceTerm.getMoveLine().getCurrencyAmount().abs().subtract(taxTotal);
      }

      return taxTotal
          .multiply(exTaxTotal)
          .multiply(invoiceTerm.getPercentage())
          .multiply(invoiceTerm.getFinancialDiscount().getDiscountRate())
          .divide(
              taxTotal.add(exTaxTotal).multiply(BigDecimal.valueOf(10000)),
              AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
              RoundingMode.HALF_UP);
    }
  }

  protected BigDecimal getTaxTotal(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getInvoice() != null) {
      return invoiceTerm.getInvoice().getTaxTotal();
    } else {
      return invoiceTerm.getMoveLine().getMove().getMoveLineList().stream()
          .filter(
              it ->
                  it.getAccount()
                      .getAccountType()
                      .getTechnicalTypeSelect()
                      .equals(AccountTypeRepository.TYPE_TAX))
          .map(MoveLine::getCurrencyAmount)
          .map(BigDecimal::abs)
          .reduce(BigDecimal::add)
          .orElse(BigDecimal.ZERO);
    }
  }
}
