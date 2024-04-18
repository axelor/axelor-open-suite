package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class ReconcileCheckServiceImpl implements ReconcileCheckService {

  protected CurrencyScaleService currencyScaleService;
  protected InvoiceTermPfpService invoiceTermPfpService;
  protected InvoiceTermToolService invoiceTermToolService;
  protected MoveLineToolService moveLineToolService;

  @Inject
  public ReconcileCheckServiceImpl(
      CurrencyScaleService currencyScaleService,
      InvoiceTermPfpService invoiceTermPfpService,
      InvoiceTermToolService invoiceTermToolService,
      MoveLineToolService moveLineToolService) {
    this.currencyScaleService = currencyScaleService;
    this.invoiceTermPfpService = invoiceTermPfpService;
    this.invoiceTermToolService = invoiceTermToolService;
    this.moveLineToolService = moveLineToolService;
  }

  @Override
  public void reconcilePreconditions(
      Reconcile reconcile, boolean updateInvoicePayments, boolean updateInvoiceTerms)
      throws AxelorException {

    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();

    if (debitMoveLine == null || creditMoveLine == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.RECONCILE_1),
          I18n.get(BaseExceptionMessage.EXCEPTION));
    }

    // Check if move lines companies are the same as the reconcile company
    Company reconcileCompany = reconcile.getCompany();
    Company debitMoveLineCompany = debitMoveLine.getMove().getCompany();
    Company creditMoveLineCompany = creditMoveLine.getMove().getCompany();
    if (!debitMoveLineCompany.equals(reconcileCompany)
        && !creditMoveLineCompany.equals(reconcileCompany)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(AccountExceptionMessage.RECONCILE_7),
              I18n.get(BaseExceptionMessage.EXCEPTION),
              debitMoveLineCompany,
              creditMoveLineCompany,
              reconcileCompany));
    }

    // Check if the amount to reconcile is != zero
    if (reconcile.getAmount() == null || reconcile.getAmount().compareTo(BigDecimal.ZERO) == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.RECONCILE_4),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          reconcile.getReconcileSeq(),
          debitMoveLine.getName(),
          debitMoveLine.getAccount().getLabel(),
          creditMoveLine.getName(),
          creditMoveLine.getAccount().getLabel());
    }

    if ((currencyScaleService.isGreaterThan(
                reconcile.getAmount(),
                creditMoveLine.getCredit().subtract(creditMoveLine.getAmountPaid()),
                creditMoveLine,
                false)
            || currencyScaleService.isGreaterThan(
                reconcile.getAmount(),
                debitMoveLine.getDebit().subtract(debitMoveLine.getAmountPaid()),
                debitMoveLine,
                false))
        && reconcile.getForeignExchangeMove() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.RECONCILE_5)
              + " "
              + I18n.get(AccountExceptionMessage.RECONCILE_3),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          reconcile.getReconcileSeq(),
          reconcile.getAmount(),
          debitMoveLine.getName(),
          debitMoveLine.getAccount().getLabel(),
          debitMoveLine.getDebit().subtract(debitMoveLine.getAmountPaid()),
          creditMoveLine.getName(),
          creditMoveLine.getAccount().getLabel(),
          creditMoveLine.getCredit().subtract(creditMoveLine.getAmountPaid()));
    }

    // Check tax lines
    this.taxLinePrecondition(creditMoveLine.getMove());
    this.taxLinePrecondition(debitMoveLine.getMove());

    if (updateInvoiceTerms && updateInvoicePayments) {
      invoiceTermPfpService.validatePfpValidatedAmount(
          debitMoveLine, creditMoveLine, reconcile.getAmount(), reconcileCompany);
    }
  }

  @Override
  public void checkCurrencies(MoveLine debitMoveLine, MoveLine creditMoveLine)
      throws AxelorException {
    Currency debitCurrency = debitMoveLine.getMove().getCurrency();
    Currency creditCurrency = creditMoveLine.getMove().getCurrency();
    Currency companyCurrency = debitMoveLine.getMove().getCompanyCurrency();

    if (!Objects.equals(debitCurrency, creditCurrency)
        && !Objects.equals(debitCurrency, companyCurrency)
        && !Objects.equals(creditCurrency, companyCurrency)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.RECONCILE_WRONG_CURRENCY));
    }
  }

  @Override
  public boolean isCompanyCurrency(
      Reconcile reconcile, InvoicePayment invoicePayment, Move otherMove) {
    Currency currency;
    if (invoicePayment != null) {
      currency = invoicePayment.getCurrency();
    } else {
      currency = otherMove.getCurrency();
      if (currency == null) {
        currency = otherMove.getCompanyCurrency();
      }
    }

    return currency.equals(reconcile.getCompany().getCurrency());
  }

  @Override
  public void checkReconcile(Reconcile reconcile) throws AxelorException {
    this.checkMoveLine(reconcile, reconcile.getCreditMoveLine());
    this.checkMoveLine(reconcile, reconcile.getDebitMoveLine());
  }

  protected void checkMoveLine(Reconcile reconcile, MoveLine moveLine) throws AxelorException {
    LocalDate reconciliationDateTime =
        Optional.ofNullable(reconcile.getReconciliationDateTime())
            .map(LocalDateTime::toLocalDate)
            .orElse(null);
    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())
        && !invoiceTermToolService.isEnoughAmountToPay(
            moveLine.getInvoiceTermList(), reconcile.getAmount(), reconciliationDateTime)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.RECONCILE_NOT_ENOUGH_AMOUNT));
    }
  }

  protected void taxLinePrecondition(Move move) throws AxelorException {
    if (move.getMoveLineList().stream()
        .anyMatch(
            it ->
                ObjectUtils.isEmpty(it.getTaxLineSet())
                    && moveLineToolService.isMoveLineTaxAccount(it))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          AccountExceptionMessage.RECONCILE_MISSING_TAX,
          move.getReference());
    }
  }
}
