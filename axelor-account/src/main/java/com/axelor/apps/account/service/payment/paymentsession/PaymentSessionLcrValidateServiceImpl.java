package com.axelor.apps.account.service.payment.paymentsession;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveComputeService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.PartnerService;
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

public class PaymentSessionLcrValidateServiceImpl implements PaymentSessionLcrValidateService {

  protected PaymentSessionValidateService paymentSessionValidateService;
  protected InvoiceTermRepository invoiceTermRepo;
  protected MoveValidateService moveValidateService;
  protected MoveComputeService moveComputeService;
  protected PaymentSessionRepository paymentSessionRepo;
  protected MoveRepository moveRepo;
  protected PartnerRepository partnerRepo;
  protected InvoicePaymentRepository invoicePaymentRepo;
  protected AccountConfigService accountConfigService;
  protected PartnerService partnerService;
  protected PaymentModeService paymentModeService;
  protected MoveInvoiceTermService moveInvoiceTermService;
  protected ReconcileService reconcileService;
  protected InvoiceTermService invoiceTermService;

  protected int counter = 0;

  @Inject
  public PaymentSessionLcrValidateServiceImpl(
      PaymentSessionValidateService paymentSessionValidateService,
      InvoiceTermRepository invoiceTermRepo,
      MoveValidateService moveValidateService,
      MoveComputeService moveComputeService,
      PaymentSessionRepository paymentSessionRepo,
      MoveRepository moveRepo,
      PartnerRepository partnerRepo,
      InvoicePaymentRepository invoicePaymentRepo,
      AccountConfigService accountConfigService,
      PartnerService partnerService,
      PaymentModeService paymentModeService,
      MoveInvoiceTermService moveInvoiceTermService,
      ReconcileService reconcileService,
      InvoiceTermService invoiceTermService) {
    this.paymentSessionValidateService = paymentSessionValidateService;
    this.invoiceTermRepo = invoiceTermRepo;
    this.moveValidateService = moveValidateService;
    this.moveComputeService = moveComputeService;
    this.paymentSessionRepo = paymentSessionRepo;
    this.moveRepo = moveRepo;
    this.partnerRepo = partnerRepo;
    this.invoicePaymentRepo = invoicePaymentRepo;
    this.accountConfigService = accountConfigService;
    this.partnerService = partnerService;
    this.paymentModeService = paymentModeService;
    this.moveInvoiceTermService = moveInvoiceTermService;
    this.reconcileService = reconcileService;
    this.invoiceTermService = invoiceTermService;
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
    this.processLcrAccounting(moveDateMap, paymentAmountMap);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void processInvoiceTerms(
      PaymentSession paymentSession,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefund,
      boolean out)
      throws AxelorException {
    counter = 0;
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

          if (invoiceTerm.getPaymentAmount().compareTo(BigDecimal.ZERO) > 0) {
            this.processInvoiceTermLcr(
                paymentSession,
                invoiceTerm,
                moveDateMap,
                paymentAmountMap,
                invoiceTermLinkWithRefund);
          }
        } else {
          paymentSessionValidateService.releaseInvoiceTerm(invoiceTerm);
        }
      }

      JPA.clear();
    }
  }

  protected void processInvoiceTermLcr(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefund)
      throws AxelorException {
    if (paymentSession.getStatusSelect() != PaymentSessionRepository.STATUS_AWAITING_PAYMENT) {
      if (paymentSessionValidateService.generatePaymentsFirst(paymentSession)) {
        paymentSessionValidateService.generatePendingPaymentFromInvoiceTerm(
            paymentSession, invoiceTerm);
      }
      processLcrPlacement(paymentSession, invoiceTerm, moveDateMap, invoiceTermLinkWithRefund);
    }

    if (paymentSession.getAccountingTriggerSelect()
            == PaymentSessionRepository.ACCOUNTING_TRIGGER_IMMEDIATE
        || paymentSession.getStatusSelect() == PaymentSessionRepository.STATUS_AWAITING_PAYMENT) {
      processLcrPayment(paymentSession, invoiceTerm, paymentAmountMap);
    }
  }

  protected void processLcrAccounting(
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

  protected void processLcrPlacement(
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
            invoiceTermLinkWithRefundList);

    fillPlacementMove(move, invoiceTerm, paymentSession, reconciledAmount, out, accountConfig);
  }

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

    if (invoiceTerm.getAmountPaid().signum() != 0) {
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
    }

    move.setDescription(
        this.getMoveDescription(paymentSession, moveLine.getCurrencyAmount(), false));

    moveComputeService.autoApplyCutOffDates(move);

    reconcileService.reconcile(invoiceTermMoveLine, moveLine, null, false, false);

    invoiceTermService.payInvoiceTerms(moveLine.getInvoiceTermList());
    invoiceTermService.payInvoiceTerms(List.of(invoiceTerm));
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

  protected void processLcrPayment(
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
              invoiceTerm.getMoveLine().getOrigin(),
              paymentSessionValidateService.getMoveLineDescription(paymentSession),
              !out);
    }

    if (paymentAmountMap.get(move) != null) {
      paymentAmountMap.replace(move, cashPartMoveLine.getCurrencyAmount());
    }

    move.setDescription(
        this.getMoveDescription(paymentSession, cashPartMoveLine.getCurrencyAmount(), true));

    moveComputeService.autoApplyCutOffDates(move);

    Reconcile reconcile =
        reconcileService.reconcile(moveLine, placementMoveLine, null, false, false);

    invoiceTermService.payInvoiceTerms(placementMoveLine.getInvoiceTermList());
    invoiceTermService.payInvoiceTerms(moveLine.getInvoiceTermList());

    reconcileService.updatePayment(
        reconcile,
        invoiceTerm.getMoveLine(),
        invoiceTerm.getInvoice(),
        invoiceTerm.getMoveLine().getMove(),
        move,
        invoiceTerm.getAmountPaid(),
        false);
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
