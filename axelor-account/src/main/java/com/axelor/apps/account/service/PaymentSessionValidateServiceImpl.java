package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentSessionValidateServiceImpl implements PaymentSessionValidateService {
  protected AppBaseService appBaseService;
  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveLineCreateService moveLineCreateService;
  protected ReconcileService reconcileService;
  protected InvoiceTermService invoiceTermService;
  protected MoveLineTaxService moveLineTaxService;
  protected PaymentSessionRepository paymentSessionRepo;
  protected InvoiceTermRepository invoiceTermRepo;
  protected MoveRepository moveRepo;
  protected PartnerRepository partnerRepo;
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
      PaymentSessionRepository paymentSessionRepo,
      InvoiceTermRepository invoiceTermRepo,
      MoveRepository moveRepo,
      PartnerRepository partnerRepo) {
    this.appBaseService = appBaseService;
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveLineCreateService = moveLineCreateService;
    this.reconcileService = reconcileService;
    this.invoiceTermService = invoiceTermService;
    this.moveLineTaxService = moveLineTaxService;
    this.paymentSessionRepo = paymentSessionRepo;
    this.invoiceTermRepo = invoiceTermRepo;
    this.moveRepo = moveRepo;
    this.partnerRepo = partnerRepo;
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
  public int processPaymentSession(PaymentSession paymentSession) throws AxelorException {
    Map<Partner, List<Move>> moveMap = new HashMap<>();
    Map<Move, BigDecimal> paymentAmountMap = new HashMap<>();

    boolean out = paymentSession.getPaymentMode().getInOutSelect() == PaymentModeRepository.OUT;
    boolean isGlobal =
        paymentSession.getAccountingMethodSelect()
            == PaymentSessionRepository.ACCOUNTING_METHOD_GLOBAL;

    this.processInvoiceTerms(paymentSession, moveMap, paymentAmountMap, out, isGlobal);
    this.postProcessPaymentSession(paymentSession, moveMap, paymentAmountMap, out, isGlobal);

    return this.getMoveCount(moveMap, isGlobal);
  }

  protected void postProcessPaymentSession(
      PaymentSession paymentSession,
      Map<Partner, List<Move>> moveMap,
      Map<Move, BigDecimal> paymentAmountMap,
      boolean out,
      boolean isGlobal)
      throws AxelorException {
    this.generateCashMoveAndLines(paymentSession, moveMap, paymentAmountMap, out, isGlobal);
    this.generateTaxMoveLines(moveMap);
    this.updateStatuses(paymentSession, moveMap, paymentAmountMap);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void processInvoiceTerms(
      PaymentSession paymentSession,
      Map<Partner, List<Move>> moveMap,
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
            .filter("self.paymentSession = :paymentSession")
            .bind("paymentSession", paymentSession)
            .order("id");

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
  }

  @Transactional(rollbackOn = {Exception.class})
  protected PaymentSession processInvoiceTerm(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      Map<Partner, List<Move>> moveMap,
      Map<Move, BigDecimal> paymentAmountMap,
      boolean out,
      boolean isGlobal)
      throws AxelorException {
    if (paymentSession.getAccountingTriggerSelect()
        == PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE) {
      paymentSession.setStatusSelect(PaymentSessionRepository.STATUS_CLOSED);
      paymentSession.setValidatedByUser(AuthUtils.getUser());
      paymentSession.setValidatedDate(
          appBaseService.getTodayDateTime(paymentSession.getCompany()).toLocalDateTime());

      this.generateMoveFromInvoiceTerm(
          paymentSession, invoiceTerm, moveMap, paymentAmountMap, out, isGlobal);
    } else {
      paymentSession.setStatusSelect(PaymentSessionRepository.STATUS_AWAITING_PAYMENT);
      paymentSessionRepo.save(paymentSession);
    }

    return paymentSession;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected Move generateMoveFromInvoiceTerm(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      Map<Partner, List<Move>> moveMap,
      Map<Move, BigDecimal> paymentAmountMap,
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

    Move move = this.getMove(paymentSession, partner, invoiceTerm, moveMap, paymentAmountMap);

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
      Map<Partner, List<Move>> moveMap,
      Map<Move, BigDecimal> paymentAmountMap)
      throws AxelorException {
    Move move;

    if (paymentSession.getAccountingMethodSelect()
            == PaymentSessionRepository.ACCOUNTING_METHOD_BY_INVOICE_TERM
        || !moveMap.containsKey(partner)) {
      move = this.createMove(paymentSession, partner);

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

  protected Move createMove(PaymentSession paymentSession, Partner partner) throws AxelorException {

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
        paymentSession.getSequence(),
        "");
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

  protected void generateCashMoveAndLines(
      PaymentSession paymentSession,
      Map<Partner, List<Move>> moveMap,
      Map<Move, BigDecimal> paymentAmountMap,
      boolean out,
      boolean isGlobal)
      throws AxelorException {
    if (!moveMap.isEmpty()) {
      this.generateCashMoveLines(paymentSession, moveMap, paymentAmountMap, out, isGlobal);

      if (isGlobal) {
        this.generateCashMove(paymentSession, paymentAmountMap.values().iterator().next(), out);
      }
    }
  }

  protected Move generateCashMove(
      PaymentSession paymentSession, BigDecimal paymentAmount, boolean out) throws AxelorException {
    paymentSession = paymentSessionRepo.find(paymentSession.getId());
    Move move = this.createMove(paymentSession, null);
    String description = this.getMoveLineDescription(paymentSession);

    this.generateCashMoveLine(
        move, null, this.getCashAccount(paymentSession, true), paymentAmount, description, out);
    this.generateCashMoveLine(
        move, null, this.getCashAccount(paymentSession, false), paymentAmount, description, !out);

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

  protected void generateTaxMoveLines(Map<Partner, List<Move>> moveMap) throws AxelorException {
    for (Partner partner : moveMap.keySet()) {
      for (Move move : moveMap.get(partner)) {
        move = moveRepo.find(move.getId());
        moveLineTaxService.autoTaxLineGenerate(move);
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
    AccountManagement accountManagement =
        paymentSession.getPaymentMode().getAccountManagementList().get(0);

    Account cashAccount =
        isGlobal
            ? accountManagement.getGlobalAccountingCashAccount()
            : accountManagement.getCashAccount();

    if (cashAccount == null && isGlobal) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PAYMENT_SESSION_NO_GLOBAL_ACCOUNTING_CASH_ACCOUNT),
          paymentSession.getPaymentMode().getName());
    }

    return cashAccount;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected InvoiceTerm releaseInvoiceTerm(InvoiceTerm invoiceTerm) {
    invoiceTerm.setPaymentSession(null);
    invoiceTerm.setPaymentAmount(BigDecimal.ZERO);

    return invoiceTermRepo.save(invoiceTerm);
  }

  protected void updateStatuses(
      PaymentSession paymentSession,
      Map<Partner, List<Move>> moveMap,
      Map<Move, BigDecimal> paymentAmountMap)
      throws AxelorException {
    paymentSession = paymentSessionRepo.find(paymentSession.getId());

    for (List<Move> moveList : moveMap.values()) {
      for (Move move : moveList) {
        move = moveRepo.find(move.getId());
        move.setDescription(this.getMoveDescription(paymentSession, paymentAmountMap.get(move)));

        this.updateStatus(move, paymentSession.getJournal().getAllowAccountingDaybook());
      }
    }
  }

  protected void updateStatus(Move move, boolean daybook) throws AxelorException {
    moveValidateService.updateValidateStatus(move, daybook);

    if (daybook) {
      move.setStatusSelect(MoveRepository.STATUS_DAYBOOK);
    } else {
      moveValidateService.accounting(move);
    }
  }

  protected int getMoveCount(Map<Partner, List<Move>> moveMap, boolean isGlobal) {
    return moveMap.values().stream().map(List::size).reduce(Integer::sum).orElse(0)
        + (isGlobal ? 1 : 0);
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

  protected Account getFinancialDiscountAccount(Company company, boolean out) {
    return out
        ? company.getAccountConfig().getPurchFinancialDiscountAccount()
        : company.getAccountConfig().getSaleFinancialDiscountAccount();
  }

  protected Tax getFinancialDiscountTax(Company company, boolean out) {
    return out
        ? company.getAccountConfig().getPurchFinancialDiscountTax()
        : company.getAccountConfig().getSaleFinancialDiscountTax();
  }

  @Override
  public StringBuilder generateFlashMessage(PaymentSession paymentSession, int moveCount) {
    StringBuilder flashMessage = new StringBuilder();

    if (moveCount > 0) {
      flashMessage.append(
          String.format(I18n.get(IExceptionMessage.PAYMENT_SESSION_GENERATED_MOVES), moveCount));
    }

    return flashMessage;
  }
}
