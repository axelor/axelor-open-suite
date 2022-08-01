package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineInvoiceTermServiceImpl implements MoveLineInvoiceTermService {
  protected InvoiceTermService invoiceTermService;
  protected MoveLineCreateService moveLineCreateService;

  @Inject
  public MoveLineInvoiceTermServiceImpl(
      InvoiceTermService invoiceTermService, MoveLineCreateService moveLineCreateService) {
    this.invoiceTermService = invoiceTermService;
    this.moveLineCreateService = moveLineCreateService;
  }

  @Override
  public void generateDefaultInvoiceTerm(MoveLine moveLine) throws AxelorException {
    Move move = moveLine.getMove();

    if (move == null) {
      return;
    } else if (move.getPaymentCondition() == null
        || CollectionUtils.isEmpty(move.getPaymentCondition().getPaymentConditionLineList())) {
      BigDecimal amount =
          moveLine.getCredit().signum() == 0 ? moveLine.getDebit() : moveLine.getCredit();

      invoiceTermService.createInvoiceTerm(
          null,
          moveLine,
          null,
          null,
          moveLine.getMove().getPaymentMode(),
          moveLine.getDate(),
          null,
          amount,
          BigDecimal.valueOf(100),
          false);

      return;
    }

    moveLine.clearInvoiceTermList();

    BigDecimal total = moveLine.getCredit().max(moveLine.getDebit());
    MoveLine holdbackMoveLine = null;
    for (PaymentConditionLine paymentConditionLine :
        move.getPaymentCondition().getPaymentConditionLineList()) {
      if (paymentConditionLine.getIsHoldback()) {
        holdbackMoveLine =
            this.computeInvoiceTermWithHoldback(move, moveLine, paymentConditionLine, total);
      } else {
        InvoiceTerm invoiceTerm =
            this.computeInvoiceTerm(moveLine, move, paymentConditionLine, total);
        moveLine.addInvoiceTermListItem(invoiceTerm);
      }
    }

    moveLine.getInvoiceTermList().forEach(it -> this.recomputePercentages(it, total));

    if (holdbackMoveLine != null) {
      holdbackMoveLine.getInvoiceTermList().forEach(it -> this.recomputePercentages(it, total));
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
      Move move, MoveLine moveLine, PaymentConditionLine paymentConditionLine, BigDecimal total)
      throws AxelorException {
    Account holdbackAccount = this.getHoldbackAccount(moveLine, move);
    MoveLine holdbackMoveLine =
        move.getMoveLineList().stream()
            .filter(
                it ->
                    it.getAccount().equals(holdbackAccount)
                        && it.getCredit().signum() == moveLine.getCredit().signum())
            .findFirst()
            .orElse(null);

    BigDecimal holdbackAmount = total.multiply(paymentConditionLine.getPaymentPercentage());

    if (holdbackMoveLine == null) {
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
    } else {
      this.updateMoveLine(holdbackMoveLine, holdbackAmount, false);
      this.updateMoveLine(moveLine, holdbackAmount, true);
    }

    InvoiceTerm invoiceTerm =
        this.computeInvoiceTerm(holdbackMoveLine, move, paymentConditionLine, total);
    holdbackMoveLine.addInvoiceTermListItem(invoiceTerm);

    return holdbackMoveLine;
  }

  protected InvoiceTerm computeInvoiceTerm(
      MoveLine moveLine, Move move, PaymentConditionLine paymentConditionLine, BigDecimal total) {
    BigDecimal amount =
        total
            .multiply(paymentConditionLine.getPaymentPercentage())
            .divide(
                BigDecimal.valueOf(100),
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                RoundingMode.HALF_UP);
    LocalDate dueDate =
        move.getPaymentCondition() == null || move.getOriginDate() == null
            ? move.getDate()
            : InvoiceToolService.getDueDate(
                move.getPaymentCondition().getPaymentConditionLineList().get(0),
                move.getOriginDate());

    InvoiceTerm invoiceTerm =
        invoiceTermService.createInvoiceTerm(
            null,
            moveLine,
            null, // RIB
            null,
            move.getPaymentMode(),
            dueDate,
            null,
            amount,
            paymentConditionLine.getPaymentPercentage(),
            paymentConditionLine.getIsHoldback());

    invoiceTerm.setPaymentConditionLine(paymentConditionLine);

    return invoiceTerm;
  }

  protected void recomputePercentages(InvoiceTerm invoiceTerm, BigDecimal total) {
    invoiceTerm.setPercentage(
        invoiceTermService.computeCustomizedPercentage(invoiceTerm.getAmount(), total));
  }

  protected void updateMoveLine(MoveLine moveLine, BigDecimal amount, boolean subtract) {
    if (subtract) {
      amount = amount.negate();
    }

    moveLine.setCredit(moveLine.getCredit().add(amount));
    moveLine.setDebit(moveLine.getDebit().add(amount));
    moveLine.setAmountRemaining(moveLine.getAmountRemaining().add(amount));
  }

  protected Account getHoldbackAccount(MoveLine moveLine, Move move) {
    Partner partner = moveLine.getPartner() != null ? moveLine.getPartner() : move.getPartner();
    // TODO
    return new Account();
  }
}
