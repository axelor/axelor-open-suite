/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PaymentSessionServiceImpl implements PaymentSessionService {

  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected ReconcileService reconcileService;
  protected PaymentSessionRepository paymentSessionRepo;
  protected InvoiceTermRepository invoiceTermRepo;
  protected int counter = 0;

  @Inject
  public PaymentSessionServiceImpl(
      MoveCreateService moveCreateService,
      MoveLineCreateService moveLineCreateService,
      ReconcileService reconcileService,
      PaymentSessionRepository paymentSessionRepo,
      InvoiceTermRepository invoiceTermRepo) {
    this.moveCreateService = moveCreateService;
    this.moveLineCreateService = moveLineCreateService;
    this.reconcileService = reconcileService;
    this.paymentSessionRepo = paymentSessionRepo;
    this.invoiceTermRepo = invoiceTermRepo;
  }

  @Override
  public String computeName(PaymentSession paymentSession) {
    StringBuilder name = new StringBuilder("Session");
    User createdBy = paymentSession.getCreatedBy();
    Boolean isFr =
        ObjectUtils.notEmpty(createdBy)
            && ObjectUtils.notEmpty(createdBy.getLanguage())
            && createdBy.getLanguage().equals(Locale.FRENCH.getLanguage());
    if (ObjectUtils.notEmpty(paymentSession.getPaymentMode())) {
      name.append(" " + paymentSession.getPaymentMode().getName());
    }
    if (ObjectUtils.notEmpty(paymentSession.getCreatedOn())) {
      name.append(
          (isFr ? " du " : " on the ")
              + paymentSession
                  .getCreatedOn()
                  .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
    if (ObjectUtils.notEmpty(createdBy)) {
      name.append((isFr ? " par " : " by ") + createdBy.getName());
    }
    return name.toString();
  }

  @Override
  public boolean validateInvoiceTerms(PaymentSession paymentSession) {
    if (paymentSession.getNextSessionDate() == null) {
      return true;
    }

    LocalDate nextSessionDate;
    int offset = 0;
    List<InvoiceTerm> invoiceTermList;
    Query<InvoiceTerm> invoiceTermQuery =
        invoiceTermRepo
            .all()
            .filter(
                "self.paymentSession = :paymentSession "
                    + "AND self.isSelectedOnPaymentSession IS TRUE "
                    + "AND self.financialDiscount IS NOT NULL")
            .bind("paymentSession", paymentSession)
            .order("id");

    while (!(invoiceTermList = invoiceTermQuery.fetch(AbstractBatch.FETCH_LIMIT, offset))
        .isEmpty()) {
      nextSessionDate = this.fetchNextSessionDate(paymentSession);

      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        offset++;

        if ((invoiceTerm.getInvoice() != null
                && !invoiceTerm
                    .getInvoice()
                    .getFinancialDiscountDeadlineDate()
                    .isAfter(nextSessionDate))
            || (invoiceTerm.getMoveLine() != null
                && invoiceTerm.getMoveLine().getPartner() != null
                && invoiceTerm.getMoveLine().getPartner().getFinancialDiscount() != null
                && !invoiceTerm
                    .getDueDate()
                    .minusDays(
                        invoiceTerm
                            .getMoveLine()
                            .getPartner()
                            .getFinancialDiscount()
                            .getDiscountDelay())
                    .isAfter(nextSessionDate))) {
          return false;
        }
      }

      JPA.clear();
    }

    return true;
  }

  protected LocalDate fetchNextSessionDate(PaymentSession paymentSession) {
    paymentSession = paymentSessionRepo.find(paymentSession.getId());
    return paymentSession.getNextSessionDate();
  }

  @Override
  public void processPaymentSession(PaymentSession paymentSession) throws AxelorException {
    int offset = 0;
    List<InvoiceTerm> invoiceTermList;
    Query<InvoiceTerm> invoiceTermQuery =
        invoiceTermRepo
            .all()
            .filter("self.paymentSession = :paymentSession")
            .bind("paymentSession", paymentSession)
            .order("id");

    while (!(invoiceTermList = invoiceTermQuery.fetch(AbstractBatch.FETCH_LIMIT, offset))
        .isEmpty()) {
      paymentSession = paymentSessionRepo.find(paymentSession.getId());

      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        offset++;

        if (invoiceTerm.getIsSelectedOnPaymentSession()) {
          this.processInvoiceTerm(paymentSession, invoiceTerm);
        } else {
          this.releaseInvoiceTerm(invoiceTerm);
        }
      }

      JPA.clear();
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected PaymentSession processInvoiceTerm(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm) throws AxelorException {
    if (paymentSession.getAccountingTriggerSelect()
        == PaymentSessionRepository.ACCOUNTING_TRIGGER_IMMEDIATE) {
      paymentSession.setStatusSelect(PaymentSessionRepository.STATUS_CLOSED);
      this.generateMovesFromInvoiceTerm(paymentSession, invoiceTerm);
    } else {
      paymentSession.setStatusSelect(PaymentSessionRepository.STATUS_AWAITING_PAYMENT);
    }

    return paymentSession;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected Move generateMovesFromInvoiceTerm(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm) throws AxelorException {
    if (invoiceTerm.getMoveLine() == null) {
      return null;
    }

    String description =
        String.format(
            "%s - %s - %s - %s",
            paymentSession.getJournal() == null
                ? ""
                : paymentSession.getJournal().getDescriptionModel(),
            invoiceTerm.getMoveLine().getPartner().getFullName(),
            invoiceTerm.getPaymentAmount(),
            paymentSession.getCurrency() == null ? "" : paymentSession.getCurrency().getCode());

    Move move =
        moveCreateService.createMove(
            paymentSession.getJournal(),
            paymentSession.getCompany(),
            paymentSession.getCurrency(),
            invoiceTerm.getMoveLine().getPartner(),
            paymentSession.getPaymentDate(),
            paymentSession.getPaymentDate(),
            paymentSession.getPaymentMode(),
            null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            invoiceTerm.getMoveLine().getOrigin(),
            description);

    counter = 0;

    this.generateMoveLinesFromInvoiceTerm(paymentSession, invoiceTerm, move);

    return move;
  }

  protected Move generateMoveLinesFromInvoiceTerm(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm, Move move) throws AxelorException {
    MoveLine moveLine =
        this.generateMoveLineFromInvoiceTerm(paymentSession, invoiceTerm, move, true);
    move.addMoveLineListItem(moveLine);
    move.addMoveLineListItem(
        this.generateMoveLineFromInvoiceTerm(paymentSession, invoiceTerm, move, false));

    this.reconcile(paymentSession, invoiceTerm, moveLine);

    return move;
  }

  protected MoveLine generateMoveLineFromInvoiceTerm(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm, Move move, boolean out)
      throws AxelorException {
    return moveLineCreateService.createMoveLine(
        move,
        invoiceTerm.getMoveLine().getPartner(),
        invoiceTerm.getMoveLine().getAccount(),
        invoiceTerm.getPaymentAmount(),
        out == (paymentSession.getPaymentMode().getInOutSelect() == PaymentModeRepository.OUT),
        move.getDate(),
        ++counter,
        null,
        move.getDescription());
  }

  protected Reconcile reconcile(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm, MoveLine moveLine)
      throws AxelorException {
    MoveLine debitMoveLine, creditMoveLine;

    if (paymentSession.getPaymentMode().getInOutSelect() == PaymentModeRepository.OUT) {
      debitMoveLine = moveLine;
      creditMoveLine = invoiceTerm.getMoveLine();
    } else {
      debitMoveLine = invoiceTerm.getMoveLine();
      creditMoveLine = moveLine;
    }

    return reconcileService.reconcile(debitMoveLine, creditMoveLine, false, true);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected InvoiceTerm releaseInvoiceTerm(InvoiceTerm invoiceTerm) {
    invoiceTerm.setPaymentSession(null);
    invoiceTerm.setPaymentAmount(BigDecimal.ZERO);

    return invoiceTerm;
  }
}
