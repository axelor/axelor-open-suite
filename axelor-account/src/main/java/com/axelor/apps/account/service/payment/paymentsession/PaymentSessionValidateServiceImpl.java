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
package com.axelor.apps.account.service.payment.paymentsession;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.TypedQuery;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import org.apache.commons.collections.CollectionUtils;

public class PaymentSessionValidateServiceImpl implements PaymentSessionValidateService {
  protected AppBaseService appBaseService;
  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveLineCreateService moveLineCreateService;
  protected ReconcileService reconcileService;
  protected InvoiceTermService invoiceTermService;
  protected MoveLineTaxService moveLineTaxService;
  protected InvoicePaymentCreateService invoicePaymentCreateService;
  protected InvoicePaymentValidateService invoicePaymentValidateService;
  protected PaymentSessionRepository paymentSessionRepo;
  protected InvoiceTermRepository invoiceTermRepo;
  protected MoveRepository moveRepo;
  protected PartnerRepository partnerRepo;
  protected InvoicePaymentRepository invoicePaymentRepo;
  protected AccountConfigService accountConfigService;
  protected PartnerService partnerService;
  protected PaymentModeService paymentModeService;
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;
  protected int counter = 0;

  @Inject
  public PaymentSessionValidateServiceImpl(
      AppBaseService appBaseService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveLineCreateService moveLineCreateService,
      ReconcileService reconcileService,
      InvoiceTermService invoiceTermService,
      MoveLineTaxService moveLineTaxService,
      InvoicePaymentCreateService invoicePaymentCreateService,
      InvoicePaymentValidateService invoicePaymentValidateService,
      PaymentSessionRepository paymentSessionRepo,
      InvoiceTermRepository invoiceTermRepo,
      MoveRepository moveRepo,
      PartnerRepository partnerRepo,
      InvoicePaymentRepository invoicePaymentRepo,
      AccountConfigService accountConfigService,
      PartnerService partnerService,
      PaymentModeService paymentModeService,
      MoveLineInvoiceTermService moveLineInvoiceTermService) {
    this.appBaseService = appBaseService;
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveLineCreateService = moveLineCreateService;
    this.reconcileService = reconcileService;
    this.invoiceTermService = invoiceTermService;
    this.moveLineTaxService = moveLineTaxService;
    this.invoicePaymentCreateService = invoicePaymentCreateService;
    this.invoicePaymentValidateService = invoicePaymentValidateService;
    this.paymentSessionRepo = paymentSessionRepo;
    this.invoiceTermRepo = invoiceTermRepo;
    this.moveRepo = moveRepo;
    this.partnerRepo = partnerRepo;
    this.invoicePaymentRepo = invoicePaymentRepo;
    this.accountConfigService = accountConfigService;
    this.partnerService = partnerService;
    this.paymentModeService = paymentModeService;
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
  }

  @Override
  public int checkValidTerms(PaymentSession paymentSession) {
    LocalDate nextSessionDate;
    int offset = 0;
    List<InvoiceTerm> invoiceTermList;
    Query<InvoiceTerm> invoiceTermQuery =
        invoiceTermRepo
            .all()
            .filter(
                "self.paymentSession = :paymentSession "
                    + "AND self.isSelectedOnPaymentSession IS TRUE")
            .bind("paymentSession", paymentSession)
            .order("id");

    while (!(invoiceTermList = invoiceTermQuery.fetch(AbstractBatch.FETCH_LIMIT, offset))
        .isEmpty()) {
      nextSessionDate = this.fetchNextSessionDate(paymentSession);

      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        offset++;

        if (nextSessionDate != null
            && invoiceTerm.getFinancialDiscount() != null
            && this.checkNextSessionDate(invoiceTerm, nextSessionDate)) {
          return 1;
        } else if (invoiceTerm.getIsPaid()
            || invoiceTerm.getPaymentAmount().compareTo(invoiceTerm.getAmountRemaining()) > 0
            || !invoiceTermService.isNotAwaitingPayment(invoiceTerm)) {
          return 2;
        }
      }

      JPA.clear();
    }

    return 0;
  }

  protected boolean checkNextSessionDate(InvoiceTerm invoiceTerm, LocalDate nextSessionDate) {
    return (invoiceTerm.getInvoice() != null
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
                .isAfter(nextSessionDate));
  }

  protected LocalDate fetchNextSessionDate(PaymentSession paymentSession) {
    paymentSession = paymentSessionRepo.find(paymentSession.getId());
    return paymentSession.getNextSessionDate();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public int processPaymentSession(PaymentSession paymentSession) throws AxelorException {
    Map<LocalDate, Map<Partner, List<Move>>> moveDateMap = new HashMap<>();
    Map<Move, BigDecimal> paymentAmountMap = new HashMap<>();

    boolean out = paymentSession.getPaymentMode().getInOutSelect() == PaymentModeRepository.OUT;
    boolean isGlobal =
        paymentSession.getAccountingMethodSelect()
            == PaymentSessionRepository.ACCOUNTING_METHOD_GLOBAL;

    this.processInvoiceTerms(paymentSession, moveDateMap, paymentAmountMap, out, isGlobal);
    this.postProcessPaymentSession(paymentSession, moveDateMap, paymentAmountMap, out, isGlobal);

    return this.getMoveCount(moveDateMap, isGlobal);
  }

  protected void postProcessPaymentSession(
      PaymentSession paymentSession,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap,
      boolean out,
      boolean isGlobal)
      throws AxelorException {
    this.updateStatus(paymentSession);
    this.generateCashMoveAndLines(paymentSession, moveDateMap, paymentAmountMap, out, isGlobal);
    this.generateTaxMoveLines(moveDateMap);
    this.updateStatuses(paymentSession, moveDateMap, paymentAmountMap);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void processInvoiceTerms(
      PaymentSession paymentSession,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap,
      boolean out,
      boolean isGlobal)
      throws AxelorException {
    counter = 0;
    int offset = 0;
    List<InvoiceTerm> invoiceTermList;
    Query<InvoiceTerm> invoiceTermQuery =
        invoiceTermRepo
            .all()
            .filter("self.paymentSession = :paymentSession AND self.paymentAmount > 0")
            .bind("paymentSession", paymentSession)
            .order("id");

    while (!(invoiceTermList = invoiceTermQuery.fetch(AbstractBatch.FETCH_LIMIT, offset))
        .isEmpty()) {
      paymentSession = paymentSessionRepo.find(paymentSession.getId());

      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        offset++;
        if (paymentSession.getStatusSelect() == PaymentSessionRepository.STATUS_AWAITING_PAYMENT
            || this.shouldBeProcessed(invoiceTerm)) {

          if (invoiceTerm.getPaymentAmount().compareTo(BigDecimal.ZERO) > 0) {
            this.processInvoiceTerm(
                paymentSession, invoiceTerm, moveDateMap, paymentAmountMap, out, isGlobal);
          }
        } else {
          this.releaseInvoiceTerm(invoiceTerm);
        }
      }

      JPA.clear();
    }
  }

  protected boolean shouldBeProcessed(InvoiceTerm invoiceTerm) {
    return invoiceTerm.getIsSelectedOnPaymentSession()
        && !invoiceTerm.getIsPaid()
        && invoiceTerm.getAmountRemaining().compareTo(invoiceTerm.getPaymentAmount()) >= 0
        && invoiceTermService.isNotAwaitingPayment(invoiceTerm);
  }

  protected PaymentSession processInvoiceTerm(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap,
      boolean out,
      boolean isGlobal)
      throws AxelorException {
    if (this.generatePaymentsFirst(paymentSession)) {
      this.generatePendingPaymentFromInvoiceTerm(paymentSession, invoiceTerm);
    } else if (paymentSession.getAccountingTriggerSelect()
            == PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE
        || paymentSession.getStatusSelect() == PaymentSessionRepository.STATUS_AWAITING_PAYMENT) {
      this.generateMoveFromInvoiceTerm(
          paymentSession, invoiceTerm, moveDateMap, paymentAmountMap, out, isGlobal);
    }

    return paymentSession;
  }

  protected boolean generatePaymentsFirst(PaymentSession paymentSession) {
    return false;
  }

  @Transactional
  protected InvoicePayment generatePendingPaymentFromInvoiceTerm(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getInvoice() == null) {
      return null;
    }

    InvoicePayment invoicePayment =
        invoicePaymentCreateService.createInvoicePayment(
            invoiceTerm.getInvoice(),
            invoiceTerm,
            paymentSession.getPaymentMode(),
            paymentSession.getBankDetails(),
            paymentSession.getPaymentDate(),
            paymentSession);

    invoiceTerm.getInvoice().addInvoicePaymentListItem(invoicePayment);
    return invoicePaymentRepo.save(invoicePayment);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected Move generateMoveFromInvoiceTerm(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap,
      boolean out,
      boolean isGlobal)
      throws AxelorException {
    if (invoiceTerm.getMoveLine() == null) {
      return null;
    }

    if (invoiceTerm.getMoveLine().getMove() != null
        && invoiceTerm.getMoveLine().getMove().getCompany() != null
        && invoiceTerm.getMoveLine().getMove().getStatusSelect() == MoveRepository.STATUS_DAYBOOK) {
      AccountConfig accountConfig =
          accountConfigService.getAccountConfig(invoiceTerm.getMoveLine().getMove().getCompany());
      if (accountConfig.getAccountingDaybook() && accountConfig.getAccountAtPayment()) {
        moveValidateService.accounting(invoiceTerm.getMoveLine().getMove());
      }
    }

    Partner partner = null;
    if (!isGlobal) {
      partner = invoiceTerm.getMoveLine().getPartner();
    }

    Move move = this.getMove(paymentSession, partner, invoiceTerm, moveDateMap, paymentAmountMap);

    this.generateMoveLineFromInvoiceTerm(
        paymentSession, invoiceTerm, move, invoiceTerm.getMoveLine().getOrigin(), out);

    if (invoiceTerm.getApplyFinancialDiscountOnPaymentSession()
        && (paymentSession.getPartnerTypeSelect() == PaymentSessionRepository.PARTNER_TYPE_CUSTOMER
            || paymentSession.getPartnerTypeSelect()
                == PaymentSessionRepository.PARTNER_TYPE_SUPPLIER)) {
      this.createFinancialDiscountMoveLine(paymentSession, invoiceTerm, move, out);
    }

    return moveRepo.save(move);
  }

  protected Move getMove(
      PaymentSession paymentSession,
      Partner partner,
      InvoiceTerm invoiceTerm,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap)
      throws AxelorException {
    LocalDate accountingDate = this.getAccountingDate(paymentSession, invoiceTerm);
    Move move;

    if (!moveDateMap.containsKey(accountingDate)) {
      moveDateMap.put(accountingDate, new HashMap<>());
    }

    Map<Partner, List<Move>> moveMap = moveDateMap.get(accountingDate);

    if (paymentSession.getAccountingMethodSelect()
            == PaymentSessionRepository.ACCOUNTING_METHOD_BY_INVOICE_TERM
        || !moveMap.containsKey(partner)) {
      BankDetails partnerBankDetails = null;
      if (paymentSession.getAccountingMethodSelect()
          == PaymentSessionRepository.ACCOUNTING_METHOD_BY_INVOICE_TERM) {
        partnerBankDetails = invoiceTerm.getBankDetails();
      }
      move = this.createMove(paymentSession, partner, accountingDate, partnerBankDetails);

      if (!moveMap.containsKey(partner)) {
        moveMap.put(partner, new ArrayList<>());
      }

      moveMap.get(partner).add(move);
      paymentAmountMap.put(move, invoiceTerm.getAmountPaid());
    } else {
      move = moveMap.get(partner).get(0);
      move = moveRepo.find(move.getId());
      paymentAmountMap.replace(move, paymentAmountMap.get(move).add(invoiceTerm.getAmountPaid()));
    }

    return move;
  }

  protected Move createMove(
      PaymentSession paymentSession,
      Partner partner,
      LocalDate accountingDate,
      BankDetails partnerBankDetails)
      throws AxelorException {
    Move move =
        moveCreateService.createMove(
            paymentSession.getJournal(),
            paymentSession.getCompany(),
            paymentSession.getCurrency(),
            partner,
            accountingDate,
            paymentSession.getPaymentDate(),
            paymentSession.getPaymentMode(),
            null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            paymentSession.getSequence(),
            "",
            paymentSession.getBankDetails());

    move.setPaymentSession(paymentSession);
    move.setPartnerBankDetails(partnerBankDetails);
    move.setPaymentCondition(null);

    return move;
  }

  protected String getMoveDescription(PaymentSession paymentSession, BigDecimal amount) {
    return String.format(
        "%s - %s%s",
        paymentSession.getName(),
        amount,
        paymentSession.getCurrency() == null ? "" : paymentSession.getCurrency().getCode());
  }

  protected String getMoveLineDescription(PaymentSession paymentSession) {
    return String.format("%s : %s", paymentSession.getSequence(), paymentSession.getName());
  }

  protected Move generateMoveLineFromInvoiceTerm(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm, Move move, String origin, boolean out)
      throws AxelorException {
    MoveLine moveLine =
        this.generateMoveLine(
            move,
            invoiceTerm.getMoveLine().getPartner(),
            invoiceTerm.getMoveLine().getAccount(),
            invoiceTerm.getPaymentAmount(),
            origin,
            this.getMoveLineDescription(paymentSession),
            out);

    this.reconcile(paymentSession, invoiceTerm, moveLine);

    return move;
  }

  protected MoveLine generateMoveLine(
      Move move,
      Partner partner,
      Account account,
      BigDecimal paymentAmount,
      String origin,
      String description,
      boolean isDebit)
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
            origin,
            description);

    move.addMoveLineListItem(moveLine);

    moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, move.getDate(), false);

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

    InvoicePayment invoicePayment = this.findInvoicePayment(paymentSession, invoiceTerm);
    if (invoicePayment != null) {
      invoicePayment.setMove(moveLine.getMove());

      if (invoicePayment.getStatusSelect() == InvoicePaymentRepository.STATUS_PENDING) {
        try {
          invoicePaymentValidateService.validate(invoicePayment, true);
        } catch (JAXBException | IOException | DatatypeConfigurationException e) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY, e.getLocalizedMessage());
        }
      }
    }

    return reconcileService.reconcile(debitMoveLine, creditMoveLine, invoicePayment, false, true);
  }

  protected InvoicePayment findInvoicePayment(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getInvoice() == null
        || CollectionUtils.isEmpty(invoiceTerm.getInvoice().getInvoicePaymentList())) {
      return null;
    }

    return invoiceTerm.getInvoice().getInvoicePaymentList().stream()
        .filter(
            it ->
                it.getPaymentSession() != null
                    && it.getPaymentSession().equals(paymentSession)
                    && it.getInvoiceTermPaymentList().stream()
                        .anyMatch(itp -> invoiceTerm.equals(itp.getInvoiceTerm())))
        .findFirst()
        .orElse(null);
  }

  protected void generateCashMoveAndLines(
      PaymentSession paymentSession,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap,
      boolean out,
      boolean isGlobal)
      throws AxelorException {

    for (LocalDate accountingDate : moveDateMap.keySet()) {

      Map<Partner, List<Move>> moveMapIt = moveDateMap.get(accountingDate);

      if (!moveMapIt.isEmpty()) {
        this.generateCashMoveLines(paymentSession, moveMapIt, paymentAmountMap, out, isGlobal);

        if (isGlobal
            && moveDateMap != null
            && moveDateMap.get(accountingDate) != null
            && moveDateMap.get(accountingDate).get(null) != null) {
          BigDecimal paymentAmount =
              paymentAmountMap.get(moveDateMap.get(accountingDate).get(null).get(0));
          this.generateCashMove(paymentSession, accountingDate, paymentAmount, out);
        }
      }
    }
  }

  protected Move generateCashMove(
      PaymentSession paymentSession,
      LocalDate accountingDate,
      BigDecimal paymentAmount,
      boolean out)
      throws AxelorException {
    paymentSession = paymentSessionRepo.find(paymentSession.getId());
    Move move = this.createMove(paymentSession, null, accountingDate, null);
    String description = this.getMoveLineDescription(paymentSession);

    this.generateCashMoveLine(
        move, null, this.getCashAccount(paymentSession, true), paymentAmount, description, !out);
    this.generateCashMoveLine(
        move, null, this.getCashAccount(paymentSession, false), paymentAmount, description, out);

    moveRepo.save(move);

    this.updateStatus(move, paymentSession.getJournal().getAllowAccountingDaybook());

    return move;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void generateCashMoveLines(
      PaymentSession paymentSession,
      Map<Partner, List<Move>> moveMap,
      Map<Move, BigDecimal> paymentAmountMap,
      boolean out,
      boolean isGlobal)
      throws AxelorException {
    Account cashAccount = this.getCashAccount(paymentSession, isGlobal);
    BigDecimal amount;

    for (Partner partner : moveMap.keySet()) {
      for (Move move : moveMap.get(partner)) {
        amount = paymentAmountMap.get(move);

        this.generateCashMoveLine(
            move, partner, cashAccount, amount, this.getMoveLineDescription(paymentSession), out);
      }
    }
  }

  protected void generateTaxMoveLines(Map<LocalDate, Map<Partner, List<Move>>> moveDateMap)
      throws AxelorException {
    for (Map<Partner, List<Move>> moveMap : moveDateMap.values()) {
      for (Partner partner : moveMap.keySet()) {
        for (Move move : moveMap.get(partner)) {
          move = moveRepo.find(move.getId());
          moveLineTaxService.autoTaxLineGenerate(move, null, false);
        }
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected Move generateCashMoveLine(
      Move move,
      Partner partner,
      Account cashAccount,
      BigDecimal paymentAmount,
      String description,
      boolean out)
      throws AxelorException {
    move = moveRepo.find(move.getId());

    if (partner != null) {
      partner = partnerRepo.find(partner.getId());
    }

    this.generateMoveLine(move, partner, cashAccount, paymentAmount, null, description, !out);

    return moveRepo.save(move);
  }

  protected Account getCashAccount(PaymentSession paymentSession, boolean isGlobal)
      throws AxelorException {
    paymentSession = paymentSessionRepo.find(paymentSession.getId());

    return paymentModeService.getPaymentModeAccount(
        paymentSession.getPaymentMode(),
        paymentSession.getCompany(),
        paymentSession.getBankDetails(),
        isGlobal);
  }

  @Transactional
  protected InvoiceTerm releaseInvoiceTerm(InvoiceTerm invoiceTerm) {
    if (!invoiceTerm.getIsSelectedOnPaymentSession()) {
      invoiceTerm.setPaymentSession(null);
    }
    invoiceTerm.setPaymentAmount(BigDecimal.ZERO);

    return invoiceTermRepo.save(invoiceTerm);
  }

  @Transactional
  protected void updateStatus(PaymentSession paymentSession) {
    paymentSession = paymentSessionRepo.find(paymentSession.getId());

    if (paymentSession.getAccountingTriggerSelect()
            == PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE
        || paymentSession.getStatusSelect() == PaymentSessionRepository.STATUS_AWAITING_PAYMENT) {
      paymentSession.setStatusSelect(PaymentSessionRepository.STATUS_CLOSED);
      paymentSession.setValidatedByUser(AuthUtils.getUser());
      paymentSession.setValidatedDate(
          appBaseService.getTodayDateTime(paymentSession.getCompany()).toLocalDateTime());
    } else {
      paymentSession.setStatusSelect(PaymentSessionRepository.STATUS_AWAITING_PAYMENT);
    }

    paymentSessionRepo.save(paymentSession);
  }

  protected void updateStatuses(
      PaymentSession paymentSession,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap)
      throws AxelorException {
    paymentSession = paymentSessionRepo.find(paymentSession.getId());

    for (LocalDate accountingDate : moveDateMap.keySet()) {
      for (List<Move> moveList : moveDateMap.get(accountingDate).values()) {
        for (Move move : moveList) {
          move = moveRepo.find(move.getId());
          move.setDescription(this.getMoveDescription(paymentSession, paymentAmountMap.get(move)));

          this.updateStatus(move, paymentSession.getJournal().getAllowAccountingDaybook());
          this.updatePaymentDescription(move);
        }
      }
    }
  }

  protected void updateStatus(Move move, boolean daybook) throws AxelorException {
    moveValidateService.updateValidateStatus(move, daybook);

    if (daybook) {
      move.setStatusSelect(MoveRepository.STATUS_DAYBOOK);
      moveValidateService.completeMoveLines(move);
      moveValidateService.freezeFieldsOnMoveLines(move);
    } else {
      moveValidateService.accounting(move);
    }
  }

  @Override
  public LocalDate getAccountingDate(PaymentSession paymentSession, InvoiceTerm invoiceTerm) {
    switch (paymentSession.getMoveAccountingDateSelect()) {
      case PaymentSessionRepository.MOVE_ACCOUNTING_DATE_PAYMENT:
        return paymentSession.getPaymentDate();
      case PaymentSessionRepository.MOVE_ACCOUNTING_DATE_ORIGIN_DOCUMENT:
        return invoiceTerm.getDueDate().isBefore(paymentSession.getPaymentDate())
            ? paymentSession.getPaymentDate()
            : invoiceTerm.getDueDate();
      case PaymentSessionRepository.MOVE_ACCOUNTING_DATE_ACCOUNTING_TRIGGER:
        return appBaseService.getTodayDate(paymentSession.getCompany());
    }

    return null;
  }

  @Transactional
  protected void updatePaymentDescription(Move move) {
    for (InvoicePayment invoicePayment :
        invoicePaymentRepo.all().filter("self.move = ?", move).fetch()) {
      invoicePayment.setDescription(move.getDescription());
      invoicePaymentRepo.save(invoicePayment);
    }
  }

  protected int getMoveCount(
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap, boolean isGlobal) {

    return moveDateMap.values().stream()
            .map(Map::values)
            .flatMap(Collection::stream)
            .map(List::size)
            .reduce(Integer::sum)
            .orElse(0)
        + (isGlobal ? moveDateMap.values().size() : 0);
  }

  protected MoveLine createFinancialDiscountMoveLine(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm, Move move, boolean out)
      throws AxelorException {
    Account financialDiscountAccount =
        this.getFinancialDiscountAccount(paymentSession.getCompany(), out);
    BigDecimal financialDiscountTaxAmount =
        invoiceTermService.getFinancialDiscountTaxAmount(invoiceTerm);

    MoveLine moveLine =
        this.generateMoveLine(
            move,
            null,
            financialDiscountAccount,
            invoiceTerm.getFinancialDiscountAmount().subtract(financialDiscountTaxAmount),
            move.getOrigin(),
            move.getDescription(),
            !out);

    if (financialDiscountTaxAmount.signum() > 0) {
      Tax financialDiscountTax = this.getFinancialDiscountTax(paymentSession.getCompany(), out);

      if (financialDiscountTax != null && financialDiscountTax.getActiveTaxLine() != null) {
        moveLine.setTaxLine(financialDiscountTax.getActiveTaxLine());
        moveLine.setTaxRate(financialDiscountTax.getActiveTaxLine().getValue());
        moveLine.setTaxCode(financialDiscountTax.getCode());
      }
    }

    return moveLine;
  }

  protected Account getFinancialDiscountAccount(Company company, boolean out)
      throws AxelorException {
    return out
        ? accountConfigService.getPurchFinancialDiscountAccount(
            accountConfigService.getAccountConfig(company))
        : accountConfigService.getSaleFinancialDiscountAccount(
            accountConfigService.getAccountConfig(company));
  }

  protected Tax getFinancialDiscountTax(Company company, boolean out) throws AxelorException {
    return out
        ? accountConfigService.getPurchFinancialDiscountTax(
            accountConfigService.getAccountConfig(company))
        : accountConfigService.getSaleFinancialDiscountTax(
            accountConfigService.getAccountConfig(company));
  }

  @Override
  public StringBuilder generateFlashMessage(PaymentSession paymentSession, int moveCount) {
    StringBuilder flashMessage = new StringBuilder();

    if (moveCount > 0) {
      flashMessage.append(
          String.format(
                  I18n.get(AccountExceptionMessage.PAYMENT_SESSION_GENERATED_MOVES), moveCount)
              + " ");
    }

    return flashMessage;
  }

  @Override
  public List<Partner> getPartnersWithNegativeAmount(PaymentSession paymentSession)
      throws AxelorException {
    TypedQuery<Partner> partnerQuery =
        JPA.em()
            .createQuery(
                "SELECT DISTINCT Partner FROM Partner Partner "
                    + " FULL JOIN MoveLine MoveLine on Partner.id = MoveLine.partner "
                    + " FULL JOIN InvoiceTerm InvoiceTerm on  MoveLine.id = InvoiceTerm.moveLine "
                    + " WHERE InvoiceTerm.paymentSession = :paymentSession "
                    + " AND InvoiceTerm.isSelectedOnPaymentSession = true"
                    + " GROUP BY Partner.id "
                    + " HAVING SUM(InvoiceTerm.paymentAmount) < 0 ",
                Partner.class);

    partnerQuery.setParameter("paymentSession", paymentSession);

    return partnerQuery.getResultList();
  }

  @Override
  public void reconciledInvoiceTermMoves(PaymentSession paymentSession) throws AxelorException {

    TypedQuery<InvoiceTerm> invoiceTermQuery =
        JPA.em()
            .createQuery(
                "SELECT InvoiceTerm FROM InvoiceTerm InvoiceTerm "
                    + " WHERE InvoiceTerm.paymentSession = :paymentSession "
                    + " AND InvoiceTerm.isSelectedOnPaymentSession = true",
                InvoiceTerm.class);
    invoiceTermQuery.setParameter("paymentSession", paymentSession);

    List<InvoiceTerm> invoiceTermList = invoiceTermQuery.getResultList();

    if (!ObjectUtils.isEmpty(invoiceTermList)) {
      invoiceTermList =
          invoiceTermService.reconcileMoveLineInvoiceTermsWithFullRollBack(invoiceTermList);
    }
  }

  @Override
  public boolean checkIsHoldBackWithRefund(PaymentSession paymentSession) throws AxelorException {
    boolean isHoldBackWithRefund = false;
    TypedQuery<InvoiceTerm> holdbackInvoiceTermQuery =
        JPA.em()
            .createQuery(
                "SELECT InvoiceTerm FROM InvoiceTerm InvoiceTerm "
                    + " WHERE InvoiceTerm.paymentSession = :paymentSession "
                    + " AND InvoiceTerm.isSelectedOnPaymentSession = true "
                    + " AND InvoiceTerm.isHoldBack = true ",
                InvoiceTerm.class);
    holdbackInvoiceTermQuery.setParameter("paymentSession", paymentSession);

    List<InvoiceTerm> holdbackInvoiceTermList = holdbackInvoiceTermQuery.getResultList();

    TypedQuery<InvoiceTerm> refundInvoiceTermQuery =
        JPA.em()
            .createQuery(
                "SELECT InvoiceTerm FROM InvoiceTerm InvoiceTerm "
                    + " WHERE InvoiceTerm.paymentSession = :paymentSession "
                    + " AND InvoiceTerm.isSelectedOnPaymentSession = true "
                    + " AND InvoiceTerm.amountPaid < 0",
                InvoiceTerm.class);
    refundInvoiceTermQuery.setParameter("paymentSession", paymentSession);

    List<InvoiceTerm> refundInvoiceTermList = refundInvoiceTermQuery.getResultList();

    if (!ObjectUtils.isEmpty(holdbackInvoiceTermList)
        && !ObjectUtils.isEmpty(refundInvoiceTermList)) {
      isHoldBackWithRefund = true;
    }

    return isHoldBackWithRefund;
  }

  @Override
  public boolean isEmpty(PaymentSession paymentSession) {
    return invoiceTermRepo.all().filter("self.paymentSession = :paymentSession")
        .bind("paymentSession", paymentSession).fetch().stream()
        .noneMatch(this::shouldBeProcessed);
  }

  @Override
  public List<InvoiceTerm> getInvoiceTermsWithInActiveBankDetails(PaymentSession paymentSession) {
    return invoiceTermRepo
        .all()
        .filter(
            "self.paymentSession = :paymentSession AND self.isSelectedOnPaymentSession = true AND self.bankDetails.active = false")
        .bind("paymentSession", paymentSession)
        .fetch();
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class})
  public StringBuilder processInvoiceTerms(PaymentSession paymentSession) throws AxelorException {
    reconciledInvoiceTermMoves(paymentSession);
    return generateFlashMessage(paymentSession, processPaymentSession(paymentSession));
  }
}
