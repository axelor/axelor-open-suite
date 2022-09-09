package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineInvoiceTermServiceImpl implements MoveLineInvoiceTermService {
  protected InvoiceTermService invoiceTermService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineToolService moveLineToolService;
  protected AccountingSituationService accountingSituationService;

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
    this.generateDefaultInvoiceTerm(moveLine, null, canCreateHolbackMoveLine);
  }

  @Override
  public void generateDefaultInvoiceTerm(
      MoveLine moveLine, LocalDate singleTermDueDate, boolean canCreateHolbackMoveLine)
      throws AxelorException {
    Move move = moveLine.getMove();

    if (move == null) {
      return;
    } else if (move.getPaymentCondition() == null
        || CollectionUtils.isEmpty(move.getPaymentCondition().getPaymentConditionLineList())) {
      this.computeInvoiceTerm(
          moveLine,
          move,
          move.getDate(),
          BigDecimal.valueOf(100),
          moveLine.getCurrencyAmount(),
          1,
          false);

      return;
    }

    moveLine.clearInvoiceTermList();

    boolean containsHoldback =
        move.getPaymentCondition().getPaymentConditionLineList().stream()
            .anyMatch(PaymentConditionLine::getIsHoldback);
    Account holdbackAccount = containsHoldback ? this.getHoldbackAccount(moveLine, move) : null;
    boolean isHoldback = moveLine.getAccount().equals(holdbackAccount);
    BigDecimal total =
        invoiceTermService.getTotalInvoiceTermsAmount(moveLine, holdbackAccount, containsHoldback);
    MoveLine holdbackMoveLine = null;

    for (PaymentConditionLine paymentConditionLine :
        move.getPaymentCondition().getPaymentConditionLineList().stream()
            .sorted(Comparator.comparing(PaymentConditionLine::getSequence))
            .collect(Collectors.toList())) {
      if (paymentConditionLine.getIsHoldback() == isHoldback) {
        this.computeInvoiceTerm(moveLine, move, paymentConditionLine, singleTermDueDate, total);
      } else if (paymentConditionLine.getIsHoldback()
          && !this.isHoldbackAlreadyGenerated(move, holdbackAccount)) {
        holdbackMoveLine =
            this.computeInvoiceTermWithHoldback(
                move,
                moveLine,
                paymentConditionLine,
                holdbackAccount,
                singleTermDueDate,
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
      this.setDueDateFromInvoiceTerms(holdbackMoveLine);
    }

    if (!containsHoldback) {
      MoveLine moveLineWithHoldbackAccount =
          this.getHoldbackMoveLine(moveLine, move, holdbackAccount);

      if (moveLineWithHoldbackAccount != null) {
        moveLineWithHoldbackAccount.clearInvoiceTermList();
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.MOVE_LINE_INVOICE_TERM_HOLDBACK_2));
      }
    }

    this.setDueDateFromInvoiceTerms(moveLine);
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
      LocalDate singleTermDueDate,
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
            I18n.get(AccountExceptionMessage.MOVE_LINE_INVOICE_TERM_HOLDBACK));
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
              null);
      holdbackMoveLine.setDescription(moveLine.getDescription());
      this.updateMoveLine(moveLine, holdbackAmount, true);

      moveLineToolService.setCurrencyAmount(moveLine);
      moveLineToolService.setCurrencyAmount(holdbackMoveLine);

      move.addMoveLineListItem(holdbackMoveLine);
    }

    this.computeInvoiceTerm(holdbackMoveLine, move, paymentConditionLine, singleTermDueDate, total);

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
      MoveLine moveLine,
      Move move,
      PaymentConditionLine paymentConditionLine,
      LocalDate singleTermDueDate,
      BigDecimal total)
      throws AxelorException {
    LocalDate dueDate =
        singleTermDueDate != null
            ? singleTermDueDate
            : invoiceTermService.computeDueDate(move, paymentConditionLine);

    InvoiceTerm invoiceTerm =
        this.computeInvoiceTerm(
            moveLine,
            move,
            dueDate,
            paymentConditionLine.getPaymentPercentage(),
            total,
            paymentConditionLine.getSequence() + 1,
            paymentConditionLine.getIsHoldback());

    invoiceTerm.setPaymentConditionLine(paymentConditionLine);
  }

  protected InvoiceTerm computeInvoiceTerm(
      MoveLine moveLine,
      Move move,
      LocalDate dueDate,
      BigDecimal percentage,
      BigDecimal total,
      int sequence,
      boolean isHoldback)
      throws AxelorException {
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
        sequence,
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

    if (Lists.newArrayList(
            MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE, MoveRepository.FUNCTIONAL_ORIGIN_FIXED_ASSET)
        .contains(moveLine.getMove().getFunctionalOriginSelect())) {
      return accountingSituationService.getHoldBackSupplierAccount(partner, move.getCompany());
    } else if (moveLine.getMove().getFunctionalOriginSelect()
        == MoveRepository.FUNCTIONAL_ORIGIN_SALE) {
      return accountingSituationService.getHoldBackCustomerAccount(partner, move.getCompany());
    } else {
      return null;
    }
  }

  protected boolean isHoldbackAlreadyGenerated(Move move, Account holdbackAccount) {
    if (holdbackAccount == null) {
      return true;
    }

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
            I18n.get(AccountExceptionMessage.MOVE_LINE_INVOICE_TERM_ACCOUNT_CHANGE));
      }

      moveLine.clearInvoiceTermList();
    }

    Move move = moveLine.getMove();
    if (move.getPaymentCondition() != null) {
      this.generateDefaultInvoiceTerm(moveLine, false);
    }
  }

  @Override
  public void setDueDateFromInvoiceTerms(MoveLine moveLine) {
    moveLine.setDueDate(
        invoiceTermService.getDueDate(moveLine.getInvoiceTermList(), moveLine.getDueDate()));
  }
}
