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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
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
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PaymentSessionServiceImpl implements PaymentSessionService {

  protected AppBaseService appBaseService;
  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected ReconcileService reconcileService;
  protected PaymentSessionRepository paymentSessionRepo;
  protected InvoiceTermRepository invoiceTermRepo;
  protected MoveRepository moveRepo;
  protected PartnerRepository partnerRepo;
  protected int counter = 0;

  @Inject
  public PaymentSessionServiceImpl(
      AppBaseService appBaseService,
      MoveCreateService moveCreateService,
      MoveLineCreateService moveLineCreateService,
      ReconcileService reconcileService,
      PaymentSessionRepository paymentSessionRepo,
      InvoiceTermRepository invoiceTermRepo,
      MoveRepository moveRepo,
      PartnerRepository partnerRepo) {
    this.appBaseService = appBaseService;
    this.moveCreateService = moveCreateService;
    this.moveLineCreateService = moveLineCreateService;
    this.reconcileService = reconcileService;
    this.paymentSessionRepo = paymentSessionRepo;
    this.invoiceTermRepo = invoiceTermRepo;
    this.moveRepo = moveRepo;
    this.partnerRepo = partnerRepo;
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
  @Transactional(rollbackOn = {Exception.class})
  public Map<Partner, Move> processPaymentSession(PaymentSession paymentSession)
      throws AxelorException {
    int offset = 0;
    List<InvoiceTerm> invoiceTermList;
    Query<InvoiceTerm> invoiceTermQuery =
        invoiceTermRepo
            .all()
            .filter("self.paymentSession = :paymentSession")
            .bind("paymentSession", paymentSession)
            .order("id");

    Map<Partner, Move> moveMap = new HashMap<>();
    Map<Partner, BigDecimal> paymentAmountMap = new HashMap<>();
    counter = 0;
    boolean out = paymentSession.getPaymentMode().getInOutSelect() == PaymentModeRepository.OUT;
    boolean isGlobal =
        paymentSession.getAccountingMethodSelect()
            == PaymentSessionRepository.ACCOUNTING_METHOD_GLOBAL;

    while (!(invoiceTermList = invoiceTermQuery.fetch(AbstractBatch.FETCH_LIMIT, offset))
        .isEmpty()) {
      paymentSession = paymentSessionRepo.find(paymentSession.getId());

      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        offset++;

        if (invoiceTerm.getIsSelectedOnPaymentSession()) {
          this.processInvoiceTerm(
              paymentSession, invoiceTerm, moveMap, paymentAmountMap, out, isGlobal);
        } else {
          this.releaseInvoiceTerm(invoiceTerm);
        }
      }

      JPA.clear();
    }

    if (!moveMap.isEmpty()) {
      Account cashAccount = this.getCashAccount(paymentSession, isGlobal);

      for (Partner partner : moveMap.keySet()) {
        this.generateCashMoveLine(
            moveMap.get(partner), partner, cashAccount, paymentAmountMap.get(partner), !out);
      }

      if (isGlobal) {
        this.generateCashMove(paymentSession, cashAccount, paymentAmountMap.get(null), out);
      }
    }

    return moveMap;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected PaymentSession processInvoiceTerm(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      Map<Partner, Move> moveMap,
      Map<Partner, BigDecimal> paymentAmountMap,
      boolean out,
      boolean isGlobal)
      throws AxelorException {
    if (paymentSession.getAccountingTriggerSelect()
        == PaymentSessionRepository.ACCOUNTING_TRIGGER_IMMEDIATE) {
      paymentSession.setStatusSelect(PaymentSessionRepository.STATUS_CLOSED);
      paymentSession.setValidatedByUser(AuthUtils.getUser());
      paymentSession.setValidatedDate(
          appBaseService.getTodayDateTime(paymentSession.getCompany()).toLocalDateTime());

      this.generateMoveFromInvoiceTerm(
          paymentSession, invoiceTerm, moveMap, paymentAmountMap, out, isGlobal);
    } else {
      paymentSession.setStatusSelect(PaymentSessionRepository.STATUS_AWAITING_PAYMENT);
    }

    return paymentSession;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected Move generateMoveFromInvoiceTerm(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      Map<Partner, Move> moveMap,
      Map<Partner, BigDecimal> paymentAmountMap,
      boolean out,
      boolean isGlobal)
      throws AxelorException {
    if (invoiceTerm.getMoveLine() == null) {
      return null;
    }

    Partner partner = null;
    if (!isGlobal) {
      partner = invoiceTerm.getMoveLine().getPartner();
    }

    Move move;

    if (paymentSession.getAccountingMethodSelect()
            == PaymentSessionRepository.ACCOUNTING_METHOD_BY_INVOICE_TERM
        || !moveMap.containsKey(partner)) {
      move = this.createMove(paymentSession, invoiceTerm, partner);

      moveMap.put(partner, move);
      paymentAmountMap.put(partner, invoiceTerm.getPaymentAmount());
    } else {
      move = moveMap.get(partner);
      move = moveRepo.find(move.getId());
      paymentAmountMap.replace(
          partner, paymentAmountMap.get(partner).add(invoiceTerm.getPaymentAmount()));
    }

    this.generateMoveLineFromInvoiceTerm(paymentSession, invoiceTerm, move, out);

    return moveRepo.save(move);
  }

  protected Move createMove(PaymentSession paymentSession, InvoiceTerm invoiceTerm, Partner partner)
      throws AxelorException {
    String description =
        String.format(
            "%s - %s - %s - %s",
            paymentSession.getJournal() == null
                ? ""
                : paymentSession.getJournal().getDescriptionModel(),
            invoiceTerm == null ? "" : invoiceTerm.getMoveLine().getPartner().getFullName(),
            invoiceTerm == null ? "" : invoiceTerm.getPaymentAmount(),
            paymentSession.getCurrency() == null ? "" : paymentSession.getCurrency().getCode());

    return moveCreateService.createMove(
        paymentSession.getJournal(),
        paymentSession.getCompany(),
        paymentSession.getCurrency(),
        partner,
        paymentSession.getPaymentDate(),
        paymentSession.getPaymentDate(),
        paymentSession.getPaymentMode(),
        null,
        MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
        MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
        invoiceTerm == null ? "" : invoiceTerm.getMoveLine().getOrigin(),
        description);
  }

  protected Move generateMoveLineFromInvoiceTerm(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm, Move move, boolean out)
      throws AxelorException {
    MoveLine moveLine =
        this.generateMoveLine(
            move,
            invoiceTerm.getMoveLine().getPartner(),
            invoiceTerm.getMoveLine().getAccount(),
            invoiceTerm.getPaymentAmount(),
            out);

    this.reconcile(paymentSession, invoiceTerm, moveLine);

    return move;
  }

  protected MoveLine generateMoveLine(
      Move move, Partner partner, Account account, BigDecimal paymentAmount, boolean isDebit)
      throws AxelorException {
    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
            move,
            partner,
            account,
            paymentAmount,
            isDebit,
            move.getDate(),
            ++counter,
            null,
            move.getDescription());

    move.addMoveLineListItem(moveLine);

    return moveLine;
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

  protected Move generateCashMove(
      PaymentSession paymentSession, Account cashAccount, BigDecimal paymentAmount, boolean out)
      throws AxelorException {
    paymentSession = paymentSessionRepo.find(paymentSession.getId());
    Move move = this.createMove(paymentSession, null, null);

    this.generateCashMoveLine(
        move, null, this.getCashAccount(paymentSession, true), paymentAmount, out);
    this.generateCashMoveLine(
        move, null, this.getCashAccount(paymentSession, false), paymentAmount, !out);

    return moveRepo.save(move);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected Move generateCashMoveLine(
      Move move, Partner partner, Account cashAccount, BigDecimal paymentAmount, boolean out)
      throws AxelorException {
    move = moveRepo.find(move.getId());

    if (partner != null) {
      partner = partnerRepo.find(partner.getId());
    }

    this.generateMoveLine(move, partner, cashAccount, paymentAmount, !out);

    return moveRepo.save(move);
  }

  protected Account getCashAccount(PaymentSession paymentSession, boolean isGlobal) {
    paymentSession = paymentSessionRepo.find(paymentSession.getId());
    AccountManagement accountManagement =
        paymentSession.getPaymentMode().getAccountManagementList().get(0);

    return isGlobal
        ? accountManagement.getGlobalAccountingCashAccount()
        : accountManagement.getCashAccount();
  }

  @Transactional(rollbackOn = {Exception.class})
  protected InvoiceTerm releaseInvoiceTerm(InvoiceTerm invoiceTerm) {
    invoiceTerm.setPaymentSession(null);
    invoiceTerm.setPaymentAmount(BigDecimal.ZERO);

    return invoiceTermRepo.save(invoiceTerm);
  }
}
