package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineInvoiceTermServiceImpl implements MoveLineInvoiceTermService {
  protected InvoiceTermService invoiceTermService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineToolService moveLineToolService;
  protected AccountingSituationService accountingSituationService;

  protected int counter = 1;
  protected int holdbackCounter = 1;

  @Inject
  public MoveLineInvoiceTermServiceImpl(
      InvoiceTermService invoiceTermService,
      MoveLineCreateService moveLineCreateService,
      MoveLineToolService moveLineToolService,
      AccountingSituationService accountingSituationService) {
    this.invoiceTermService = invoiceTermService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveLineToolService = moveLineToolService;
    this.accountingSituationService = accountingSituationService;
  }

  @Override
  public void generateDefaultInvoiceTerm(MoveLine moveLine, boolean canCreateHolbackMoveLine)
      throws AxelorException {
    Move move = moveLine.getMove();

    if (move == null) {
      return;
    } else if (move.getPaymentCondition() == null
        || CollectionUtils.isEmpty(move.getPaymentCondition().getPaymentConditionLineList())) {
      BigDecimal amount =
          moveLine.getCredit().signum() == 0 ? moveLine.getDebit() : moveLine.getCredit();

      this.computeInvoiceTerm(
          moveLine, move, move.getDate(), BigDecimal.valueOf(100), amount, false);

      return;
    }

    moveLine.clearInvoiceTermList();

    Account holdbackAccount = this.getHoldbackAccount(moveLine, move);
    boolean isHoldback = moveLine.getAccount().equals(holdbackAccount);
    boolean containsHoldback =
        move.getPaymentCondition().getPaymentConditionLineList().stream()
            .anyMatch(PaymentConditionLine::getIsHoldback);
    BigDecimal total =
        invoiceTermService.getTotalInvoiceTermsAmount(moveLine, holdbackAccount, containsHoldback);
    MoveLine holdbackMoveLine = null;
    for (PaymentConditionLine paymentConditionLine :
        move.getPaymentCondition().getPaymentConditionLineList()) {
      if (paymentConditionLine.getIsHoldback() == isHoldback) {
        this.computeInvoiceTerm(moveLine, move, paymentConditionLine, total);
      } else if (paymentConditionLine.getIsHoldback()
          && !this.isHoldbackAlreadyGenerated(move, holdbackAccount)) {
        holdbackMoveLine =
            this.computeInvoiceTermWithHoldback(
                move,
                moveLine,
                paymentConditionLine,
                holdbackAccount,
                total,
                canCreateHolbackMoveLine);
      }
    }

    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      moveLine.getInvoiceTermList().forEach(it -> this.recomputePercentages(it, total));
    }

    if (holdbackMoveLine != null
        && CollectionUtils.isNotEmpty(holdbackMoveLine.getInvoiceTermList())) {
      holdbackMoveLine.getInvoiceTermList().forEach(it -> this.recomputePercentages(it, total));
    }

    if (!containsHoldback) {
      MoveLine moveLineWithHoldbackAccount =
          this.getHoldbackMoveLine(moveLine, move, holdbackAccount);

      if (moveLineWithHoldbackAccount != null) {
        moveLineWithHoldbackAccount.clearInvoiceTermList();
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.MOVE_LINE_INVOICE_TERM_HOLDBACK));
      }
    }
  }

  public void updateInvoiceTermsParentFields(MoveLine moveLine) {
    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      moveLine
          .getInvoiceTermList()
          .forEach(it -> invoiceTermService.setParentFields(it, moveLine, it.getInvoice()));
    }
  }

  protected MoveLine computeInvoiceTermWithHoldback(
      Move move,
      MoveLine moveLine,
      PaymentConditionLine paymentConditionLine,
      Account holdbackAccount,
      BigDecimal total,
      boolean canCreateHolbackMoveLine)
      throws AxelorException {
    MoveLine holdbackMoveLine = this.getHoldbackMoveLine(moveLine, move, holdbackAccount);

    BigDecimal holdbackAmount =
        total
            .multiply(paymentConditionLine.getPaymentPercentage())
            .divide(
                BigDecimal.valueOf(100),
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                RoundingMode.HALF_UP);

    if (holdbackMoveLine == null) {
      if (!canCreateHolbackMoveLine) {
        moveLine.clearInvoiceTermList();
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.MOVE_LINE_INVOICE_TERM_HOLDBACK));
      }

      holdbackMoveLine =
          moveLineCreateService.createMoveLine(
              move,
              moveLine.getPartner(),
              holdbackAccount,
              BigDecimal.ZERO,
              holdbackAmount,
              BigDecimal.ZERO,
              moveLine.getDebit().signum() > 0,
              moveLine.getDate(),
              moveLine.getDueDate(),
              moveLine.getOriginDate(),
              move.getMoveLineList().size() + 1,
              moveLine.getOrigin(),
              moveLine.getDescription());
      this.updateMoveLine(moveLine, holdbackAmount, true);

      moveLineToolService.setCurrencyAmount(moveLine);
      moveLineToolService.setCurrencyAmount(holdbackMoveLine);

      move.addMoveLineListItem(holdbackMoveLine);
    }

    this.computeInvoiceTerm(holdbackMoveLine, move, paymentConditionLine, total);

    return holdbackMoveLine;
  }

  protected MoveLine getHoldbackMoveLine(MoveLine moveLine, Move move, Account holdbackAccount) {
    return move.getMoveLineList().stream()
        .filter(
            it ->
                it.getAccount().equals(holdbackAccount)
                    && it.getCredit().signum() == moveLine.getCredit().signum())
        .findFirst()
        .orElse(null);
  }

  protected void computeInvoiceTerm(
      MoveLine moveLine, Move move, PaymentConditionLine paymentConditionLine, BigDecimal total) {
    InvoiceTerm invoiceTerm =
        this.computeInvoiceTerm(
            moveLine,
            move,
            invoiceTermService.computeDueDate(move, paymentConditionLine),
            paymentConditionLine.getPaymentPercentage(),
            total,
            paymentConditionLine.getIsHoldback());

    invoiceTerm.setPaymentConditionLine(paymentConditionLine);
  }

  protected InvoiceTerm computeInvoiceTerm(
      MoveLine moveLine,
      Move move,
      LocalDate dueDate,
      BigDecimal percentage,
      BigDecimal total,
      boolean isHoldback) {
    BigDecimal amount =
        isHoldback && total.compareTo(moveLine.getAmountRemaining()) == 0
            ? total
            : total
                .multiply(percentage)
                .divide(
                    BigDecimal.valueOf(100),
                    AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                    RoundingMode.HALF_UP);

    return invoiceTermService.createInvoiceTerm(
        null,
        moveLine,
        move.getPartnerBankDetails(),
        null,
        move.getPaymentMode(),
        dueDate,
        null,
        amount,
        percentage,
        isHoldback ? this.holdbackCounter++ : this.counter++,
        isHoldback);
  }

  protected void recomputePercentages(InvoiceTerm invoiceTerm, BigDecimal total) {
    invoiceTerm.setPercentage(
        invoiceTermService.computeCustomizedPercentage(invoiceTerm.getAmount(), total));
  }

  protected void updateMoveLine(MoveLine moveLine, BigDecimal amount, boolean subtract) {
    if (subtract) {
      amount = amount.negate();
    }

    if (moveLine.getCredit().signum() > 0) {
      moveLine.setCredit(moveLine.getCredit().add(amount));
    } else {
      moveLine.setDebit(moveLine.getDebit().add(amount));
    }

    moveLine.setAmountRemaining(moveLine.getAmountRemaining().add(amount));
  }

  protected Account getHoldbackAccount(MoveLine moveLine, Move move) throws AxelorException {
    Partner partner = moveLine.getPartner() != null ? moveLine.getPartner() : move.getPartner();

    if (moveLine.getMove().getFunctionalOriginSelect()
        == MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE) {
      return accountingSituationService.getHoldBackSupplierAccount(partner, move.getCompany());
    } else if (moveLine.getMove().getFunctionalOriginSelect()
        == MoveRepository.FUNCTIONAL_ORIGIN_SALE) {
      return accountingSituationService.getHoldBackCustomerAccount(partner, move.getCompany());
    } else {
      return moveLine.getAccount();
    }
  }

  protected boolean isHoldbackAlreadyGenerated(Move move, Account holdbackAccount) {
    return move.getMoveLineList().stream()
        .anyMatch(
            it ->
                it.getAccount().equals(holdbackAccount)
                    && CollectionUtils.isNotEmpty(it.getInvoiceTermList()));
  }

  @Override
  public void recreateInvoiceTerms(MoveLine moveLine) throws AxelorException {
    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      if (!moveLine.getInvoiceTermList().stream().allMatch(invoiceTermService::isNotReadonly)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.MOVE_LINE_INVOICE_TERM_ACCOUNT_CHANGE));
      }

      moveLine.clearInvoiceTermList();
    }

    Move move = moveLine.getMove();
    if (move.getPaymentCondition() != null) {
      this.generateDefaultInvoiceTerm(moveLine, false);
    }
  }
}
