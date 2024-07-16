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
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermReplaceService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCutOffService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateService;
import com.axelor.apps.account.service.reconcile.ReconcileInvoiceTermComputationService;
import com.axelor.apps.account.service.reconcile.ReconcileService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class PaymentSessionBillOfExchangeValidateServiceImpl
    implements PaymentSessionBillOfExchangeValidateService {

  protected PaymentSessionValidateService paymentSessionValidateService;
  protected InvoiceTermRepository invoiceTermRepo;
  protected MoveValidateService moveValidateService;
  protected MoveCutOffService moveCutOffService;
  protected PaymentSessionRepository paymentSessionRepo;
  protected MoveRepository moveRepo;
  protected AccountConfigService accountConfigService;
  protected PaymentModeService paymentModeService;
  protected ReconcileService reconcileService;
  protected ReconcileInvoiceTermComputationService reconcileInvoiceTermComputationService;
  protected InvoiceTermService invoiceTermService;
  protected InvoiceTermReplaceService invoiceTermReplaceService;
  protected InvoicePaymentValidateService invoicePaymentValidateService;

  @Inject
  public PaymentSessionBillOfExchangeValidateServiceImpl(
      PaymentSessionValidateService paymentSessionValidateService,
      InvoiceTermRepository invoiceTermRepo,
      MoveValidateService moveValidateService,
      MoveCutOffService moveCutOffService,
      PaymentSessionRepository paymentSessionRepo,
      MoveRepository moveRepo,
      AccountConfigService accountConfigService,
      PaymentModeService paymentModeService,
      ReconcileService reconcileService,
      ReconcileInvoiceTermComputationService reconcileInvoiceTermComputationService,
      InvoiceTermService invoiceTermService,
      InvoiceTermReplaceService invoiceTermReplaceService,
      InvoicePaymentValidateService invoicePaymentValidateService) {
    this.paymentSessionValidateService = paymentSessionValidateService;
    this.invoiceTermRepo = invoiceTermRepo;
    this.moveValidateService = moveValidateService;
    this.moveCutOffService = moveCutOffService;
    this.paymentSessionRepo = paymentSessionRepo;
    this.moveRepo = moveRepo;
    this.accountConfigService = accountConfigService;
    this.paymentModeService = paymentModeService;
    this.reconcileService = reconcileService;
    this.reconcileInvoiceTermComputationService = reconcileInvoiceTermComputationService;
    this.invoiceTermService = invoiceTermService;
    this.invoiceTermReplaceService = invoiceTermReplaceService;
    this.invoicePaymentValidateService = invoicePaymentValidateService;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class})
  public StringBuilder processInvoiceTerms(PaymentSession paymentSession) throws AxelorException {
    List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefund =
        new ArrayList<>();

    paymentSessionValidateService.reconciledInvoiceTermMoves(
        paymentSession, invoiceTermLinkWithRefund);

    return paymentSessionValidateService.generateFlashMessage(
        paymentSession, processPaymentSession(paymentSession, invoiceTermLinkWithRefund));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public int processPaymentSession(
      PaymentSession paymentSession,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefundList)
      throws AxelorException {
    Map<LocalDate, Map<Partner, List<Move>>> moveDateMap = new HashMap<>();
    Map<Move, BigDecimal> paymentAmountMap = new HashMap<>();

    boolean out = paymentSession.getPaymentMode().getInOutSelect() == PaymentModeRepository.OUT;

    this.processInvoiceTerms(
        paymentSession, moveDateMap, paymentAmountMap, invoiceTermLinkWithRefundList, out);
    this.postProcessPaymentSession(paymentSession, moveDateMap, paymentAmountMap, out);

    return paymentSessionValidateService.getMoveCount(moveDateMap, false);
  }

  protected void postProcessPaymentSession(
      PaymentSession paymentSession,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap,
      boolean out)
      throws AxelorException {
    paymentSessionValidateService.updateStatus(paymentSession);
    this.processBillOfExchangeAccounting(moveDateMap, paymentAmountMap);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void processInvoiceTerms(
      PaymentSession paymentSession,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefund,
      boolean out)
      throws AxelorException {
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
        if (paymentSession.getStatusSelect() == PaymentSessionRepository.STATUS_AWAITING_PAYMENT
            || paymentSessionValidateService.shouldBeProcessed(invoiceTerm)) {

          this.processInvoiceTermBillOfExchange(
              paymentSession,
              invoiceTerm,
              moveDateMap,
              paymentAmountMap,
              invoiceTermLinkWithRefund);
        } else {
          paymentSessionValidateService.releaseInvoiceTerm(invoiceTerm);
        }
      }

      JPA.clear();
    }
  }

  protected void processInvoiceTermBillOfExchange(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefund)
      throws AxelorException {
    if (paymentSession.getStatusSelect() != PaymentSessionRepository.STATUS_AWAITING_PAYMENT) {
      processPlacement(paymentSession, invoiceTerm, moveDateMap, invoiceTermLinkWithRefund);
      if (paymentSessionValidateService.generatePaymentsFirst(paymentSession)) {
        generatePendingPaymentFromInvoiceTerm(paymentSession, invoiceTerm);
      }
    }

    if (paymentSession.getAccountingTriggerSelect()
            == PaymentSessionRepository.ACCOUNTING_TRIGGER_IMMEDIATE
        || paymentSession.getStatusSelect() == PaymentSessionRepository.STATUS_AWAITING_PAYMENT) {
      processPayment(paymentSession, invoiceTerm, paymentAmountMap);
    }
  }

  protected void generatePendingPaymentFromInvoiceTerm(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm) {
    MoveLine placementMoveLine = invoiceTerm.getPlacementMoveLine();
    if (placementMoveLine == null
        || ObjectUtils.isEmpty(placementMoveLine.getInvoiceTermList())
        || !paymentSessionValidateService.generatePaymentsFirst(paymentSession)) {
      return;
    }

    for (InvoiceTerm placementInvoiceTerm :
        invoiceTerm.getPlacementMoveLine().getInvoiceTermList()) {
      paymentSessionValidateService.generatePendingPaymentFromInvoiceTerm(
          paymentSession, placementInvoiceTerm);
    }
  }

  protected void processBillOfExchangeAccounting(
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap, Map<Move, BigDecimal> paymentAmountMap)
      throws AxelorException {
    if (paymentAmountMap.size() == 1) {
      Move paymentMove = paymentAmountMap.keySet().stream().findFirst().get();

      if (!moveDateMap.containsKey(paymentMove.getAccountingDate())) {
        moveDateMap.put(paymentMove.getAccountingDate(), new HashMap<>());
      }

      Map<Partner, List<Move>> moveMap = moveDateMap.get(paymentMove.getAccountingDate());

      if (!moveMap.containsKey(paymentMove.getPartner())) {
        moveMap.put(paymentMove.getPartner(), new ArrayList<>());
      }

      moveMap.get(paymentMove.getPartner()).add(paymentMove);
    }

    List<Move> moveList =
        moveDateMap.values().stream()
            .map(Map::values)
            .flatMap(Collection::stream)
            .flatMap(Collection::stream)
            .sorted(Comparator.comparing(Move::getId))
            .collect(Collectors.toList());

    if (!ObjectUtils.isEmpty(moveList)) {
      for (Move move : moveList) {
        move = moveRepo.find(move.getId());
        moveValidateService.accounting(move);
      }
    }
  }

  @Override
  public void processPlacement(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefundList)
      throws AxelorException {

    MoveLine invoiceTermMoveLine = invoiceTerm.getMoveLine();
    Company company =
        Optional.ofNullable(invoiceTermMoveLine)
            .map(MoveLine::getMove)
            .map(Move::getCompany)
            .orElse(null);
    if (company == null) {
      return;
    }

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    Move invoiceTermMove = invoiceTermMoveLine.getMove();

    if (invoiceTermMove.getStatusSelect() == MoveRepository.STATUS_DAYBOOK) {
      if (accountConfig.getAccountingDaybook() && accountConfig.getAccountAtPayment()) {
        moveValidateService.accounting(invoiceTermMove);
      }
    }

    Map<Move, BigDecimal> paymentAmountMap = new HashMap<>();
    Partner partner = invoiceTermMoveLine.getPartner();
    Move move =
        paymentSessionValidateService.getMove(
            paymentSession, partner, invoiceTerm, moveDateMap, paymentAmountMap, true);

    boolean out = paymentSession.getPaymentMode().getInOutSelect() == PaymentModeRepository.OUT;

    BigDecimal reconciledAmount =
        paymentSessionValidateService.getReconciledAmount(
            paymentSession,
            move,
            invoiceTerm,
            out,
            paymentAmountMap,
            invoiceTermLinkWithRefundList,
            accountConfig);

    fillPlacementMove(move, invoiceTerm, paymentSession, reconciledAmount, out, accountConfig);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void fillPlacementMove(
      Move move,
      InvoiceTerm invoiceTerm,
      PaymentSession paymentSession,
      BigDecimal reconciledAmount,
      boolean out,
      AccountConfig accountConfig)
      throws AxelorException {

    MoveLine invoiceTermMoveLine = invoiceTerm.getMoveLine();

    MoveLine moveLine =
        paymentSessionValidateService.generateMoveLine(
            move,
            invoiceTermMoveLine.getPartner(),
            invoiceTermMoveLine.getAccount(),
            invoiceTerm.getAmountPaid().add(reconciledAmount),
            invoiceTermMoveLine.getOrigin(),
            paymentSessionValidateService.getMoveLineDescription(paymentSession),
            out);

    Account counterPartAccount = accountConfigService.getBillOfExchReceivAccount(accountConfig);

    MoveLine counterPartMoveLine =
        paymentSessionValidateService.generateMoveLine(
            move,
            invoiceTermMoveLine.getPartner(),
            counterPartAccount,
            invoiceTerm.getAmountPaid(),
            invoiceTermMoveLine.getOrigin(),
            paymentSessionValidateService.getMoveLineDescription(paymentSession),
            !out);

    invoiceTerm.setPlacementMoveLine(counterPartMoveLine);

    move.setDescription(
        this.getMoveDescription(paymentSession, moveLine.getCurrencyAmount(), false));

    moveCutOffService.autoApplyCutOffDates(move);

    reconcileService.reconcile(invoiceTermMoveLine, moveLine, null, false, false);

    invoiceTermService.payInvoiceTerms(List.of(invoiceTerm));
    List<InvoiceTerm> invoiceTermListToPay = moveLine.getInvoiceTermList();
    invoiceTermService.payInvoiceTerms(invoiceTermListToPay);

    invoiceTermReplaceService.replaceInvoiceTerms(
        invoiceTerm.getInvoice(), counterPartMoveLine.getInvoiceTermList(), List.of(invoiceTerm));
  }

  protected String getMoveDescription(
      PaymentSession paymentSession, BigDecimal amount, boolean isPayment) {
    String description =
        String.format(
            "%s - %s%s",
            paymentSession.getName(),
            amount,
            paymentSession.getCurrency() == null ? "" : paymentSession.getCurrency().getCode());
    if (isPayment) {
      description = description.concat(String.format(" - %s", I18n.get("Payment")));
    } else {
      description = description.concat(String.format(" - %s", I18n.get("Placement")));
    }
    return description;
  }

  protected void processPayment(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      Map<Move, BigDecimal> paymentAmountMap)
      throws AxelorException {

    MoveLine placementMoveLine = invoiceTerm.getPlacementMoveLine();
    if (placementMoveLine == null) {
      return;
    }

    Move move =
        paymentAmountMap.size() == 1 ? paymentAmountMap.keySet().stream().findFirst().get() : null;

    if (move == null) {

      move =
          paymentSessionValidateService.createMove(
              paymentSession,
              null,
              null,
              paymentSessionValidateService.getAccountingDate(paymentSession, invoiceTerm),
              invoiceTerm.getBankDetails());

      paymentAmountMap.put(move, BigDecimal.ZERO);
    }

    boolean out = paymentSession.getPaymentMode().getInOutSelect() == PaymentModeRepository.OUT;

    Account account =
        paymentModeService.getPaymentModeAccount(
            paymentSession.getPaymentMode(),
            paymentSession.getCompany(),
            paymentSession.getBankDetails(),
            false);

    fillPaymentMove(move, invoiceTerm, paymentSession, out, account, paymentAmountMap);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void fillPaymentMove(
      Move move,
      InvoiceTerm invoiceTerm,
      PaymentSession paymentSession,
      boolean out,
      Account account,
      Map<Move, BigDecimal> paymentAmountMap)
      throws AxelorException {
    MoveLine placementMoveLine = invoiceTerm.getPlacementMoveLine();

    MoveLine moveLine =
        paymentSessionValidateService.generateMoveLine(
            move,
            placementMoveLine.getPartner(),
            placementMoveLine.getAccount(),
            invoiceTerm.getAmountPaid(),
            placementMoveLine.getOrigin(),
            paymentSessionValidateService.getMoveLineDescription(paymentSession),
            out);

    MoveLine cashPartMoveLine =
        move.getMoveLineList().stream()
            .filter(ml -> ml.getAccount().equals(account))
            .findFirst()
            .orElse(null);

    if (cashPartMoveLine != null) {

      cashPartMoveLine = updateMoveLineAmounts(cashPartMoveLine, invoiceTerm);

    } else {
      cashPartMoveLine =
          paymentSessionValidateService.generateMoveLine(
              move,
              null,
              account,
              invoiceTerm.getAmountPaid(),
              placementMoveLine.getOrigin(),
              paymentSessionValidateService.getMoveLineDescription(paymentSession),
              !out);
    }

    if (paymentAmountMap.get(move) != null) {
      paymentAmountMap.replace(move, cashPartMoveLine.getCurrencyAmount());
    }

    move.setDescription(
        this.getMoveDescription(paymentSession, cashPartMoveLine.getCurrencyAmount(), true));

    moveCutOffService.autoApplyCutOffDates(move);

    Reconcile reconcile =
        reconcileService.reconcile(moveLine, placementMoveLine, null, false, false);

    if (paymentSession.getStatusSelect() != PaymentSessionRepository.STATUS_AWAITING_PAYMENT) {
      if (!ObjectUtils.isEmpty(placementMoveLine.getInvoiceTermList())
          && placementMoveLine.getInvoiceTermList().get(0).getInvoice() != null) {
        Invoice invoice = placementMoveLine.getInvoiceTermList().get(0).getInvoice();
        reconcileInvoiceTermComputationService.updatePayment(
            reconcile,
            moveLine,
            placementMoveLine,
            invoice,
            placementMoveLine.getMove(),
            move,
            reconcile.getAmount(),
            false);
      }

    } else {
      Invoice invoice =
          Optional.of(invoiceTerm)
              .map(InvoiceTerm::getMoveLine)
              .map(MoveLine::getMove)
              .map(Move::getInvoice)
              .orElse(null);

      List<InvoicePayment> pendingInvoicePaymentList =
          findInvoicePaymentWithPlacementMoveLine(paymentSession, invoice, placementMoveLine);
      invoicePaymentValidateService.validateMultipleInvoicePayment(pendingInvoicePaymentList, true);
    }

    List<InvoiceTerm> invoiceTermListToPay = placementMoveLine.getInvoiceTermList();
    invoiceTermListToPay.addAll(moveLine.getInvoiceTermList());
    invoiceTermService.payInvoiceTerms(placementMoveLine.getInvoiceTermList());
    invoiceTermService.payInvoiceTerms(moveLine.getInvoiceTermList());

    moveRepo.save(move);
  }

  protected List<InvoicePayment> findInvoicePaymentWithPlacementMoveLine(
      PaymentSession paymentSession, Invoice invoice, MoveLine placementMoveLine) {
    List<InvoicePayment> resultList = new ArrayList<>();
    if (invoice == null
        || ObjectUtils.isEmpty(invoice.getInvoicePaymentList())
        || placementMoveLine == null
        || ObjectUtils.isEmpty(placementMoveLine.getInvoiceTermList())) {
      return resultList;
    }

    List<InvoicePayment> invoicePaymentList =
        invoice.getInvoicePaymentList().stream()
            .filter(
                it ->
                    it.getPaymentSession() != null && it.getPaymentSession().equals(paymentSession))
            .collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(invoicePaymentList)) {
      for (InvoiceTerm placementInvoiceTerm : placementMoveLine.getInvoiceTermList()) {
        InvoicePayment invoicePayment =
            invoicePaymentList.stream()
                .filter(
                    it ->
                        it.getInvoiceTermPaymentList().stream()
                            .anyMatch(itp -> placementInvoiceTerm.equals(itp.getInvoiceTerm())))
                .findFirst()
                .orElse(null);
        if (invoicePayment != null) {
          resultList.add(invoicePayment);
        }
      }
    }

    return resultList;
  }

  protected MoveLine updateMoveLineAmounts(MoveLine moveLine, InvoiceTerm invoiceTerm) {
    if (moveLine == null || invoiceTerm == null) {
      return null;
    }

    if (moveLine.getCredit().signum() != 0) {
      moveLine.setCredit(moveLine.getCredit().add(invoiceTerm.getAmountPaid()));

    } else {
      moveLine.setDebit(moveLine.getDebit().add(invoiceTerm.getAmountPaid()));
    }
    moveLine.setCurrencyAmount(moveLine.getCurrencyAmount().add(invoiceTerm.getAmountPaid()));
    return moveLine;
  }
}
