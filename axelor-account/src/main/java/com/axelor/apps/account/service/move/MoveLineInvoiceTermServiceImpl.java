/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineInvoiceTermServiceImpl implements MoveLineInvoiceTermService {
  protected AppAccountService appAccountService;
  protected InvoiceTermService invoiceTermService;
  protected MoveLineService moveLineService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineToolService moveLineToolService;
  protected AccountingSituationService accountingSituationService;

  @Inject
  public MoveLineInvoiceTermServiceImpl(
      AppAccountService appAccountService,
      InvoiceTermService invoiceTermService,
      MoveLineService moveLineService,
      MoveLineCreateService moveLineCreateService,
      MoveLineToolService moveLineToolService,
      AccountingSituationService accountingSituationService) {
    this.appAccountService = appAccountService;
    this.invoiceTermService = invoiceTermService;
    this.moveLineService = moveLineService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveLineToolService = moveLineToolService;
    this.accountingSituationService = accountingSituationService;
  }

  @Override
  public void generateDefaultInvoiceTerm(
      Move move, MoveLine moveLine, boolean canCreateHolbackMoveLine) throws AxelorException {
    this.generateDefaultInvoiceTerm(move, moveLine, null, canCreateHolbackMoveLine);
  }

  @Override
  public void generateDefaultInvoiceTerm(
      Move move, MoveLine moveLine, LocalDate singleTermDueDate, boolean canCreateHolbackMoveLine)
      throws AxelorException {
    if (move == null
        || moveLine == null
        || moveLine.getAccount() == null
        || !moveLine.getAccount().getUseForPartnerBalance()) {
      return;
    }

    PaymentCondition paymentCondition = move.getPaymentCondition();

    boolean containsHoldback =
        paymentCondition != null
            && CollectionUtils.isNotEmpty(paymentCondition.getPaymentConditionLineList())
            && paymentCondition.getPaymentConditionLineList().stream()
                .anyMatch(PaymentConditionLine::getIsHoldback);
    boolean isHoldbackAllowed =
        Lists.newArrayList(
                MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE,
                MoveRepository.FUNCTIONAL_ORIGIN_FIXED_ASSET,
                MoveRepository.FUNCTIONAL_ORIGIN_SALE)
            .contains(move.getFunctionalOriginSelect());

    moveLine.clearInvoiceTermList();

    if (paymentCondition == null
        || CollectionUtils.isEmpty(paymentCondition.getPaymentConditionLineList())
        || (containsHoldback && !isHoldbackAllowed)) {
      this.computeInvoiceTerm(
          moveLine,
          move,
          move.getDate(),
          BigDecimal.valueOf(100),
          moveLine.getCurrencyAmount(),
          1,
          false);

      return;
    } else if (CollectionUtils.isNotEmpty(paymentCondition.getPaymentConditionLineList())
        && paymentCondition.getPaymentConditionLineList().size() > 1
        && !appAccountService.getAppAccount().getAllowMultiInvoiceTerms()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.INVOICE_INVOICE_TERM_MULTIPLE_LINES_NO_MULTI));
    }

    Account holdbackAccount = containsHoldback ? this.getHoldbackAccount(moveLine, move) : null;
    boolean isHoldback = moveLine.getAccount().equals(holdbackAccount);
    BigDecimal total =
        invoiceTermService.getTotalInvoiceTermsAmount(moveLine, holdbackAccount, containsHoldback);
    MoveLine holdbackMoveLine = null;

    for (PaymentConditionLine paymentConditionLine :
        paymentCondition.getPaymentConditionLineList().stream()
            .sorted(Comparator.comparing(PaymentConditionLine::getSequence))
            .collect(Collectors.toList())) {
      if ((paymentConditionLine.getIsHoldback()
              && !this.isHoldbackAlreadyGenerated(move, holdbackAccount))
          || isHoldback) {
        holdbackMoveLine =
            this.computeInvoiceTermWithHoldback(
                move,
                moveLine,
                paymentConditionLine,
                holdbackAccount,
                singleTermDueDate,
                total,
                canCreateHolbackMoveLine);
      } else if (!paymentConditionLine.getIsHoldback()) {
        this.computeInvoiceTerm(moveLine, move, paymentConditionLine, singleTermDueDate, total);
      }
    }

    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      adjustLastInvoiceTerm(
          moveLine.getInvoiceTermList(),
          moveLine.getCurrencyAmount().abs(),
          moveLine.getDebit().max(moveLine.getCredit()));
      moveLine.getInvoiceTermList().forEach(it -> this.recomputePercentages(it, total));
      this.handleFinancialDiscount(moveLine);
    }

    if (holdbackMoveLine != null
        && CollectionUtils.isNotEmpty(holdbackMoveLine.getInvoiceTermList())) {
      holdbackMoveLine.getInvoiceTermList().forEach(it -> this.recomputePercentages(it, total));
      this.setDueDateFromInvoiceTerms(holdbackMoveLine);
      this.handleFinancialDiscount(holdbackMoveLine);
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

  protected void handleFinancialDiscount(MoveLine moveLine) {
    if (moveLine.getPartner() != null
        && appAccountService.getAppAccount().getManageFinancialDiscount()) {
      moveLine.setFinancialDiscount(moveLine.getPartner().getFinancialDiscount());
      moveLineService.computeFinancialDiscount(moveLine);
    }
  }

  public void updateInvoiceTermsParentFields(MoveLine moveLine) {
    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      moveLine
          .getInvoiceTermList()
          .forEach(
              it ->
                  invoiceTermService.setParentFields(
                      it, moveLine.getMove(), moveLine, it.getInvoice()));
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
    MoveLine holdbackMoveLine = null;

    if (!moveLine.getAccount().equals(holdbackAccount)
        || !paymentConditionLine.getIsHoldback()
        || !this.isHoldbackAlreadyGenerated(move, holdbackAccount)) {
      holdbackMoveLine = this.getHoldbackMoveLine(moveLine, move, holdbackAccount);
    }

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
    if (ObjectUtils.isEmpty(move.getMoveLineList())) {
      return null;
    }

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
            ? total.setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP)
            : total
                .multiply(percentage)
                .divide(
                    BigDecimal.valueOf(100),
                    AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                    RoundingMode.HALF_UP);

    if (isHoldback) {
      amount =
          amount.divide(
              moveLine.getCurrencyRate(),
              AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
              RoundingMode.HALF_UP);
    }

    return invoiceTermService.createInvoiceTerm(
        null,
        move,
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
        .contains(move.getFunctionalOriginSelect())) {
      return accountingSituationService.getHoldBackSupplierAccount(partner, move.getCompany());
    } else if (move.getFunctionalOriginSelect() == MoveRepository.FUNCTIONAL_ORIGIN_SALE) {
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
  public void recreateInvoiceTerms(Move move, MoveLine moveLine) throws AxelorException {
    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      if (!moveLine.getInvoiceTermList().stream().allMatch(invoiceTermService::isNotReadonly)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.MOVE_LINE_INVOICE_TERM_ACCOUNT_CHANGE));
      }

      moveLine.clearInvoiceTermList();
    }

    if (move.getPaymentCondition() != null) {
      this.generateDefaultInvoiceTerm(move, moveLine, false);
    }
  }

  @Override
  public void setDueDateFromInvoiceTerms(MoveLine moveLine) {
    moveLine.setDueDate(
        invoiceTermService.getDueDate(moveLine.getInvoiceTermList(), moveLine.getDueDate()));
  }

  protected void adjustLastInvoiceTerm(
      List<InvoiceTerm> invoiceTermList, BigDecimal totalAmount, BigDecimal companyTotalAmount) {
    BigDecimal sumOfInvoiceTerm =
        invoiceTermList.stream()
            .map(InvoiceTerm::getAmount)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    if (totalAmount.compareTo(sumOfInvoiceTerm) != 0) {
      InvoiceTerm lastElement = invoiceTermList.get(invoiceTermList.size() - 1);

      BigDecimal difference = totalAmount.subtract(sumOfInvoiceTerm);
      BigDecimal amount = lastElement.getAmount().add(difference);
      BigDecimal amountRemaining = lastElement.getAmountRemaining().add(difference);

      lastElement.setAmount(amount);
      lastElement.setAmountRemaining(amountRemaining);
    }

    BigDecimal companySumOfInvoiceTerm =
        invoiceTermList.stream()
            .map(InvoiceTerm::getCompanyAmount)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    if (companyTotalAmount.compareTo(companySumOfInvoiceTerm) != 0) {
      InvoiceTerm lastElement = invoiceTermList.get(invoiceTermList.size() - 1);

      BigDecimal difference = companyTotalAmount.subtract(companySumOfInvoiceTerm);
      BigDecimal companyAmount = lastElement.getCompanyAmount().add(difference);
      BigDecimal companyAmountRemaining = lastElement.getCompanyAmountRemaining().add(difference);

      lastElement.setCompanyAmount(companyAmount);
      lastElement.setCompanyAmountRemaining(companyAmountRemaining);
    }
  }
}
