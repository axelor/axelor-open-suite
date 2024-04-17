package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceTermToolServiceImpl implements InvoiceTermToolService {

  protected InvoiceTermFilterService invoiceTermFilterService;
  protected InvoiceTermRepository invoiceTermRepository;

  @Inject
  public InvoiceTermToolServiceImpl(
      InvoiceTermFilterService invoiceTermFilterService,
      InvoiceTermRepository invoiceTermRepository) {
    this.invoiceTermFilterService = invoiceTermFilterService;
    this.invoiceTermRepository = invoiceTermRepository;
  }

  @Override
  public boolean isPartiallyPaid(InvoiceTerm invoiceTerm) {
    return invoiceTerm.getAmount().compareTo(invoiceTerm.getAmountRemaining()) != 0;
  }

  @Override
  public boolean isEnoughAmountToPay(
      List<InvoiceTerm> invoiceTermList, BigDecimal amount, LocalDate date) {
    BigDecimal amountToPay =
        invoiceTermList.stream()
            .filter(this::isNotReadonly)
            .map(it -> this.getAmountRemaining(it, date, false))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    return amountToPay.compareTo(amount) >= 0;
  }

  @Override
  public boolean isNotReadonly(InvoiceTerm invoiceTerm) {
    return this.isNotReadonlyExceptPfp(invoiceTerm)
        && invoiceTerm.getPfpValidateStatusSelect() <= InvoiceTermRepository.PFP_STATUS_AWAITING;
  }

  @Override
  public boolean isNotReadonlyExceptPfp(InvoiceTerm invoiceTerm) {
    return !invoiceTerm.getIsPaid()
        && invoiceTerm.getAmount().compareTo(invoiceTerm.getAmountRemaining()) == 0
        && invoiceTermFilterService.isNotAwaitingPayment(invoiceTerm);
  }

  @Override
  public BigDecimal getAmountRemaining(
      InvoiceTerm invoiceTerm, LocalDate date, boolean isCompanyCurrency) {
    BigDecimal amountRemaining;

    boolean applyFinancialDiscount =
        invoiceTerm.getApplyFinancialDiscount()
            && invoiceTerm.getFinancialDiscountDeadlineDate() != null
            && date != null
            && !invoiceTerm.getFinancialDiscountDeadlineDate().isBefore(date);
    if (applyFinancialDiscount) {
      amountRemaining = invoiceTerm.getAmountRemainingAfterFinDiscount();
    } else if (isCompanyCurrency) {
      amountRemaining = invoiceTerm.getCompanyAmountRemaining();
    } else {
      amountRemaining = invoiceTerm.getAmountRemaining();
    }
    return amountRemaining;
  }

  @Override
  public boolean isThresholdNotOnLastUnpaidInvoiceTerm(
      MoveLine moveLine, BigDecimal thresholdDistanceFromRegulation) {
    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())
        && moveLine.getAmountRemaining().abs().compareTo(thresholdDistanceFromRegulation) <= 0) {
      BigDecimal reconcileAmount = this.getReconcileAmount(moveLine);
      List<InvoiceTerm> unpaidInvoiceTermList =
          moveLine.getInvoiceTermList().stream()
              .filter(it -> !it.getIsPaid())
              .collect(Collectors.toList());

      for (InvoiceTerm invoiceTerm : unpaidInvoiceTermList) {
        reconcileAmount = reconcileAmount.subtract(invoiceTerm.getCompanyAmountRemaining());

        if (reconcileAmount.signum() <= 0) {
          return unpaidInvoiceTermList.indexOf(invoiceTerm) != unpaidInvoiceTermList.size() - 1;
        }
      }
    }

    return true;
  }

  protected BigDecimal getReconcileAmount(MoveLine moveLine) {
    List<Reconcile> reconcileList =
        moveLine.getDebit().signum() > 0
            ? moveLine.getDebitReconcileList()
            : moveLine.getCreditReconcileList();

    if (reconcileList == null) {
      return BigDecimal.ZERO;
    }

    return reconcileList.stream()
        .sorted(Comparator.comparing(Reconcile::getCreatedOn))
        .reduce((first, second) -> second)
        .map(Reconcile::getAmount)
        .orElse(BigDecimal.ZERO);
  }

  @Override
  public BigDecimal computeCustomizedPercentage(BigDecimal amount, BigDecimal inTaxTotal) {
    return this.computeCustomizedPercentageUnscaled(amount, inTaxTotal)
        .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal computeCustomizedPercentageUnscaled(BigDecimal amount, BigDecimal inTaxTotal) {
    BigDecimal percentage = BigDecimal.ZERO;
    if (inTaxTotal.compareTo(BigDecimal.ZERO) != 0) {
      percentage =
          amount
              .multiply(new BigDecimal(100))
              .divide(inTaxTotal, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    }
    return percentage;
  }

  @Override
  public List<InvoiceTerm> getInvoiceTerms(List<Long> invoiceTermIds) {
    return invoiceTermRepository
        .all()
        .filter("self.id IN :invoiceTermIds AND self.pfpValidateStatusSelect = :pfpStatusAwaiting")
        .bind("invoiceTermIds", invoiceTermIds)
        .bind("pfpStatusAwaiting", InvoiceRepository.PFP_STATUS_AWAITING)
        .fetch();
  }
}
