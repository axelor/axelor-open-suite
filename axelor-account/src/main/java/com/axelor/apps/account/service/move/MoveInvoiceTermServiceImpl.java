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

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PayVoucherDueElement;
import com.axelor.apps.account.db.PayVoucherElementToPay;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PayVoucherDueElementRepository;
import com.axelor.apps.account.db.repo.PayVoucherElementToPayRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MoveInvoiceTermServiceImpl implements MoveInvoiceTermService {
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;
  protected InvoiceTermService invoiceTermService;
  protected MoveRepository moveRepo;
  protected InvoiceTermRepository invoiceTermRepo;
  protected PayVoucherElementToPayRepository payVoucherElementToPayRepository;
  protected PayVoucherDueElementRepository payVoucherDueElementRepository;

  @Inject
  public MoveInvoiceTermServiceImpl(
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      InvoiceTermService invoiceTermService,
      MoveRepository moveRepo,
      InvoiceTermRepository invoiceTermRepo,
      PayVoucherElementToPayRepository payVoucherElementToPayRepository,
      PayVoucherDueElementRepository payVoucherDueElementRepository) {
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
    this.invoiceTermService = invoiceTermService;
    this.moveRepo = moveRepo;
    this.invoiceTermRepo = invoiceTermRepo;
    this.payVoucherElementToPayRepository = payVoucherElementToPayRepository;
    this.payVoucherDueElementRepository = payVoucherDueElementRepository;
  }

  @Override
  public void generateInvoiceTerms(Move move) throws AxelorException {
    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      for (MoveLine moveLine : move.getMoveLineList()) {
        if (moveLine.getAccount() != null
            && moveLine.getAccount().getUseForPartnerBalance()
            && CollectionUtils.isEmpty(moveLine.getInvoiceTermList())) {
          moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, false);
        }
      }
    }
  }

  @Override
  public void roundInvoiceTermPercentages(Move move) {
    move.getMoveLineList().stream()
        .filter(it -> CollectionUtils.isNotEmpty(it.getInvoiceTermList()))
        .forEach(
            it ->
                invoiceTermService.roundPercentages(
                    it.getInvoiceTermList(), it.getDebit().max(it.getCredit())));
  }

  @Override
  public boolean updateInvoiceTerms(Move move) {
    List<InvoiceTerm> invoiceTermToUpdateList =
        move.getMoveLineList().stream()
            .filter(
                it ->
                    it.getAmountRemaining().compareTo(it.getDebit().max(it.getCredit())) == 0
                        && it.getAccount().getUseForPartnerBalance()
                        && CollectionUtils.isNotEmpty(it.getInvoiceTermList()))
            .map(MoveLine::getInvoiceTermList)
            .flatMap(Collection::stream)
            .filter(invoiceTermService::isNotReadonlyExceptPfp)
            .collect(Collectors.toList());

    invoiceTermToUpdateList.forEach(it -> invoiceTermService.updateFromMoveHeader(move, it));

    return invoiceTermToUpdateList.size()
        == move.getMoveLineList().stream()
            .map(MoveLine::getInvoiceTermList)
            .mapToLong(Collection::size)
            .sum();
  }

  @Override
  public void recreateInvoiceTerms(Move move) throws AxelorException {
    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      for (MoveLine moveLine : move.getMoveLineList()) {
        if (moveLine.getAccount().getUseForPartnerBalance()) {
          moveLineInvoiceTermService.recreateInvoiceTerms(move, moveLine);
        }
      }
    }
  }

  public void updateMoveLineDueDates(Move move) {
    for (MoveLine moveLine : move.getMoveLineList()) {
      moveLine.setDueDate(
          invoiceTermService.getDueDate(moveLine.getInvoiceTermList(), moveLine.getOriginDate()));
    }
  }

  @Override
  public boolean displayDueDate(Move move) {
    return move.getJournal() != null
        && move.getJournal().getJournalType().getTechnicalTypeSelect()
            != JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY
        && this.isSingleTerm(move);
  }

  protected boolean isSingleTerm(Move move) {
    if (move.getPaymentCondition() == null
        || move.getPaymentCondition().getPaymentConditionLineList().size() > 1) {
      return false;
    }

    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      List<MoveLine> moveLinesWithInvoiceTerms =
          move.getMoveLineList().stream()
              .filter(it -> it.getAccount() != null && it.getAccount().getUseForPartnerBalance())
              .collect(Collectors.toList());

      return ObjectUtils.isEmpty(moveLinesWithInvoiceTerms)
          || (moveLinesWithInvoiceTerms.size() == 1
              && (ObjectUtils.isEmpty(moveLinesWithInvoiceTerms.get(0).getInvoiceTermList())
                  || moveLinesWithInvoiceTerms.get(0).getInvoiceTermList().size() == 1));
    }

    return true;
  }

  @Override
  public LocalDate computeDueDate(Move move, boolean isSingleTerm, boolean isDateChange) {
    if (move.getPaymentCondition() == null
        || CollectionUtils.isEmpty(move.getPaymentCondition().getPaymentConditionLineList())
        || move.getPaymentCondition().getPaymentConditionLineList().size() > 1) {
      return null;
    }

    if (isSingleTerm && !isDateChange && CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      InvoiceTerm singleInvoiceTerm = this.getSingleInvoiceTerm(move);

      if (singleInvoiceTerm != null) {
        return singleInvoiceTerm.getDueDate();
      }
    }

    return invoiceTermService.computeDueDate(
        move, move.getPaymentCondition().getPaymentConditionLineList().get(0));
  }

  @Override
  public void updateSingleInvoiceTermDueDate(Move move, LocalDate dueDate) {
    if (CollectionUtils.isEmpty(move.getMoveLineList()) || dueDate == null) {
      return;
    }

    InvoiceTerm singleInvoiceTerm = this.getSingleInvoiceTerm(move);

    if (singleInvoiceTerm != null
        && invoiceTermService.isNotReadonly(singleInvoiceTerm)
        && !Objects.equals(dueDate, singleInvoiceTerm.getDueDate())) {
      singleInvoiceTerm.setDueDate(dueDate);
      singleInvoiceTerm.getMoveLine().setDueDate(dueDate);
    }
  }

  protected InvoiceTerm getSingleInvoiceTerm(Move move) {
    List<InvoiceTerm> invoiceTermList =
        move.getMoveLineList().stream()
            .filter(it -> it.getAccount().getUseForPartnerBalance())
            .map(MoveLine::getInvoiceTermList)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

    if (invoiceTermList.size() == 1) {
      return invoiceTermList.get(0);
    } else {
      return null;
    }
  }

  @Override
  public String checkIfInvoiceTermInPayment(Move move) {
    String errorMessage;

    if (move != null
        && (move.getStatusSelect().equals(MoveRepository.STATUS_DAYBOOK)
            || move.getStatusSelect().equals(MoveRepository.STATUS_ACCOUNTED))
        && CollectionUtils.isNotEmpty(move.getMoveLineList())) {

      // No check if not persisted
      if (move.getMoveLineList().stream()
          .map(MoveLine::getInvoiceTermList)
          .flatMap(Collection::stream)
          .anyMatch(it -> it.getId() == null)) {
        return "";
      }

      List<InvoiceTerm> invoiceTermList =
          move.getMoveLineList().stream()
              .map(MoveLine::getInvoiceTermList)
              .flatMap(Collection::stream)
              .collect(Collectors.toList());

      if (CollectionUtils.isNotEmpty(invoiceTermList)) {
        errorMessage = this.checkInvoiceTermInPaymentVoucher(invoiceTermList);

        if (StringUtils.notEmpty(errorMessage)) {
          return errorMessage;
        }

        errorMessage = this.checkInvoiceTermInPaymentSession(invoiceTermList);

        if (StringUtils.notEmpty(errorMessage)) {
          return errorMessage;
        }

        for (InvoiceTerm invoiceTerm : invoiceTermList) {
          if (!invoiceTermService.isNotReadonlyExceptPfp(invoiceTerm)) {
            return I18n.get(AccountExceptionMessage.MOVE_INVOICE_TERM_IN_PAYMENT_AWAITING_CHANGE);
          }
        }
      }
    }

    return "";
  }

  public String checkInvoiceTermInPaymentVoucher(List<InvoiceTerm> invoiceTermList) {
    if (!CollectionUtils.isEmpty(invoiceTermList)) {
      List<String> paymentVoucherRefList =
          payVoucherElementToPayRepository.all().filter("self.invoiceTerm in (:invoiceTermList)")
              .bind("invoiceTermList", invoiceTermList).fetch().stream()
              .map(PayVoucherElementToPay::getPaymentVoucher)
              .map(PaymentVoucher::getRef)
              .collect(Collectors.toList());
      paymentVoucherRefList.addAll(
          payVoucherDueElementRepository.all().filter("self.invoiceTerm in (:invoiceTermList)")
              .bind("invoiceTermList", invoiceTermList).fetch().stream()
              .map(PayVoucherDueElement::getPaymentVoucher)
              .map(PaymentVoucher::getRef)
              .collect(Collectors.toList()));
      if (!CollectionUtils.isEmpty(paymentVoucherRefList)) {
        return String.format(
            I18n.get(AccountExceptionMessage.MOVE_INVOICE_TERM_IN_PAYMENT_VOUCHER_CHANGE),
            paymentVoucherRefList.toString());
      }
    }
    return "";
  }

  public String checkInvoiceTermInPaymentSession(List<InvoiceTerm> invoiceTermList) {
    if (!CollectionUtils.isEmpty(invoiceTermList)) {
      List<String> paymentSessionSeqList =
          invoiceTermList.stream()
              .filter(it -> it.getPaymentSession() != null)
              .map(InvoiceTerm::getPaymentSession)
              .map(PaymentSession::getSequence)
              .collect(Collectors.toList());
      if (!CollectionUtils.isEmpty(paymentSessionSeqList)) {
        return String.format(
            I18n.get(AccountExceptionMessage.MOVE_INVOICE_TERM_IN_PAYMENT_SESSION_CHANGE),
            paymentSessionSeqList.toString());
      }
    }
    return "";
  }
}
