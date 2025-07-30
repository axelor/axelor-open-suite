/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.FinancialDiscountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermFilterService;
import com.axelor.apps.account.service.invoice.InvoiceTermFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveCutOffService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineFinancialDiscountService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateService;
import com.axelor.apps.account.service.reconcile.ReconcileService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppAccount;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.TypedQuery;
import javax.xml.datatype.DatatypeConfigurationException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

public class PaymentSessionValidateServiceImpl implements PaymentSessionValidateService {
  protected AppAccountService appService;
  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveCutOffService moveCutOffService;
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
  protected InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService;
  protected MoveLineFinancialDiscountService moveLineFinancialDiscountService;
  protected FinancialDiscountService financialDiscountService;
  protected InvoiceTermFilterService invoiceTermFilterService;
  protected CurrencyScaleService currencyScaleService;
  protected int counter = 0;

  @Inject
  public PaymentSessionValidateServiceImpl(
      AppAccountService appService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveCutOffService moveCutOffService,
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
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService,
      MoveLineFinancialDiscountService moveLineFinancialDiscountService,
      FinancialDiscountService financialDiscountService,
      InvoiceTermFilterService invoiceTermFilterService,
      CurrencyScaleService currencyScaleService) {
    this.appService = appService;
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveCutOffService = moveCutOffService;
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
    this.invoiceTermFinancialDiscountService = invoiceTermFinancialDiscountService;
    this.moveLineFinancialDiscountService = moveLineFinancialDiscountService;
    this.financialDiscountService = financialDiscountService;
    this.invoiceTermFilterService = invoiceTermFilterService;
    this.currencyScaleService = currencyScaleService;
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
            || !invoiceTermFilterService.isNotAwaitingPayment(invoiceTerm)) {
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
  public int processPaymentSession(
      PaymentSession paymentSession,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefundList)
      throws AxelorException {
    Map<LocalDate, Map<Partner, List<Move>>> moveDateMap = new HashMap<>();
    Map<Move, BigDecimal> paymentAmountMap = new HashMap<>();

    boolean out = paymentSession.getPaymentMode().getInOutSelect() == PaymentModeRepository.OUT;
    boolean isGlobal =
        paymentSession.getAccountingMethodSelect()
            == PaymentSessionRepository.ACCOUNTING_METHOD_GLOBAL;

    this.processInvoiceTerms(
        paymentSession,
        moveDateMap,
        paymentAmountMap,
        invoiceTermLinkWithRefundList,
        out,
        isGlobal);
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
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefund,
      boolean out,
      boolean isGlobal)
      throws AxelorException {
    counter = 0;
    int offset = 0;
    List<InvoiceTerm> invoiceTermList;
    Query<InvoiceTerm> invoiceTermQuery =
        invoiceTermRepo
            .all()
            .filter("self.paymentSession = :paymentSession AND self.paymentAmount != 0")
            .bind("paymentSession", paymentSession)
            .order("id");

    while (!(invoiceTermList = invoiceTermQuery.fetch(AbstractBatch.FETCH_LIMIT, offset))
        .isEmpty()) {
      paymentSession = paymentSessionRepo.find(paymentSession.getId());

      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        if (paymentSession.getStatusSelect() == PaymentSessionRepository.STATUS_AWAITING_PAYMENT
            || this.shouldBeProcessed(invoiceTerm)) {

          if (invoiceTerm.getPaymentAmount().compareTo(BigDecimal.ZERO) > 0) {
            offset++;

            this.processInvoiceTerm(
                paymentSession,
                invoiceTerm,
                moveDateMap,
                paymentAmountMap,
                invoiceTermLinkWithRefund,
                out,
                isGlobal);
          }
        } else {
          this.releaseInvoiceTerm(invoiceTerm);
        }
      }

      JPA.clear();
    }
  }

  @Override
  public boolean shouldBeProcessed(InvoiceTerm invoiceTerm) {
    return invoiceTerm.getIsSelectedOnPaymentSession()
        && !invoiceTerm.getIsPaid()
        && invoiceTerm.getAmountRemaining().compareTo(invoiceTerm.getPaymentAmount()) >= 0
        && invoiceTermFilterService.isNotAwaitingPayment(invoiceTerm);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected PaymentSession processInvoiceTerm(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefundList,
      boolean out,
      boolean isGlobal)
      throws AxelorException {
    if (this.generatePaymentsFirst(paymentSession)) {
      this.generatePendingPaymentFromInvoiceTerm(paymentSession, invoiceTerm);
    } else if (paymentSession.getAccountingTriggerSelect()
            == PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE
        || paymentSession.getStatusSelect() == PaymentSessionRepository.STATUS_AWAITING_PAYMENT) {
      this.generateMoveFromInvoiceTerm(
          paymentSession,
          invoiceTerm,
          moveDateMap,
          paymentAmountMap,
          invoiceTermLinkWithRefundList,
          out,
          isGlobal);
    }

    return paymentSession;
  }

  @Override
  public boolean generatePaymentsFirst(PaymentSession paymentSession) {
    return false;
  }

  @Override
  @Transactional
  public InvoicePayment generatePendingPaymentFromInvoiceTerm(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm) throws AxelorException {
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
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefundList,
      boolean out,
      boolean isGlobal)
      throws AxelorException {
    if (invoiceTerm.getMoveLine() == null) {
      return null;
    }
    AccountConfig accountConfig = null;

    if (invoiceTerm.getMoveLine().getMove() != null
        && invoiceTerm.getMoveLine().getMove().getCompany() != null
        && invoiceTerm.getMoveLine().getMove().getStatusSelect() == MoveRepository.STATUS_DAYBOOK) {
      accountConfig =
          accountConfigService.getAccountConfig(invoiceTerm.getMoveLine().getMove().getCompany());
      if (accountConfig.getAccountingDaybook() && accountConfig.getAccountAtPayment()) {
        moveValidateService.accounting(invoiceTerm.getMoveLine().getMove());
      }
    }

    Partner partner = invoiceTerm.getMoveLine().getPartner();

    Move move =
        this.getMove(paymentSession, partner, invoiceTerm, moveDateMap, paymentAmountMap, isGlobal);

    BigDecimal reconciledAmount =
        this.getReconciledAmount(
            paymentSession,
            move,
            invoiceTerm,
            out,
            paymentAmountMap,
            invoiceTermLinkWithRefundList,
            accountConfig);

    this.generateMoveLineFromInvoiceTerm(
        paymentSession,
        invoiceTerm,
        move,
        invoiceTerm.getMoveLine().getOrigin(),
        out,
        reconciledAmount);

    if (invoiceTerm.getApplyFinancialDiscountOnPaymentSession()
        && (paymentSession.getPartnerTypeSelect() == PaymentSessionRepository.PARTNER_TYPE_CUSTOMER
            || paymentSession.getPartnerTypeSelect()
                == PaymentSessionRepository.PARTNER_TYPE_SUPPLIER)) {
      this.createFinancialDiscountMoveLine(paymentSession, invoiceTerm, move, out);
    }

    moveCutOffService.autoApplyCutOffDates(move);

    return moveRepo.save(move);
  }

  @Override
  public BigDecimal getReconciledAmount(
      PaymentSession paymentSession,
      Move move,
      InvoiceTerm invoiceTerm,
      boolean out,
      Map<Move, BigDecimal> paymentAmountMap,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefundList,
      AccountConfig accountConfig)
      throws AxelorException {
    BigDecimal reconciledAmount = BigDecimal.ZERO;
    if (!CollectionUtils.isEmpty(invoiceTermLinkWithRefundList)) {
      List<Pair<InvoiceTerm, BigDecimal>> invoiceTermByAmountList =
          invoiceTermLinkWithRefundList.stream()
              .filter(pair -> pair.getLeft().equals(invoiceTerm))
              .map(pair -> pair.getRight())
              .collect(Collectors.toList());
      if (!CollectionUtils.isEmpty(invoiceTermByAmountList)) {
        for (Pair<InvoiceTerm, BigDecimal> pair : invoiceTermByAmountList) {
          reconciledAmount = reconciledAmount.add(pair.getRight());

          this.createAndReconcileMoveLineFromPair(
              paymentSession, move, invoiceTerm, pair, accountConfig, out, paymentAmountMap);
        }
      }
    }
    return currencyScaleService.getCompanyScaledValue(
        paymentSession.getCompany(), reconciledAmount);
  }

  @Override
  public Move getMove(
      PaymentSession paymentSession,
      Partner partner,
      InvoiceTerm invoiceTerm,
      Map<LocalDate, Map<Partner, List<Move>>> moveDateMap,
      Map<Move, BigDecimal> paymentAmountMap,
      boolean isGlobal)
      throws AxelorException {
    LocalDate accountingDate = this.getAccountingDate(paymentSession, invoiceTerm);
    Move move;

    if (!moveDateMap.containsKey(accountingDate)) {
      moveDateMap.put(accountingDate, new HashMap<>());
    }

    Map<Partner, List<Move>> moveMap = moveDateMap.get(accountingDate);

    if (paymentSession.getAccountingMethodSelect()
            == PaymentSessionRepository.ACCOUNTING_METHOD_BY_INVOICE_TERM
        || !moveMap.containsKey(partner)
        || (isGlobal && partner != null && !partner.getIsCompensation())) {
      BankDetails partnerBankDetails = invoiceTerm.getBankDetails();
      Partner thirdPartyPayerPartner = null;

      if (paymentSession.getAccountingMethodSelect()
          == PaymentSessionRepository.ACCOUNTING_METHOD_BY_INVOICE_TERM) {
        thirdPartyPayerPartner = invoiceTerm.getThirdPartyPayerPartner();
        partnerBankDetails = this.getBankDetails(invoiceTerm);
      }

      move =
          this.createMove(
              paymentSession, partner, thirdPartyPayerPartner, accountingDate, partnerBankDetails);

      if (!moveMap.containsKey(partner)) {
        moveMap.put(partner, new ArrayList<>());
      }

      moveMap.get(partner).add(move);
      paymentAmountMap.put(move, invoiceTerm.getAmountPaid());
    } else {
      move = moveMap.get(partner).get(0);
      move = moveRepo.find(move.getId());
      BigDecimal amount = paymentAmountMap.get(move);
      if (amount != null) {
        paymentAmountMap.replace(move, amount.add(invoiceTerm.getAmountPaid()));
      } else {
        paymentAmountMap.put(move, invoiceTerm.getAmountPaid());
      }
    }

    return move;
  }

  protected BankDetails getBankDetails(InvoiceTerm invoiceTerm) {
    return Optional.of(invoiceTerm)
        .map(InvoiceTerm::getThirdPartyPayerPartner)
        .map(partnerService::getDefaultBankDetails)
        .orElse(invoiceTerm.getBankDetails());
  }

  @Override
  public Move createMove(
      PaymentSession paymentSession,
      Partner partner,
      Partner thirdPartyPayerPartner,
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
            getMoveOrigin(paymentSession),
            "",
            paymentSession.getBankDetails());

    move.setPartnerBankDetails(partnerBankDetails);
    move.setPaymentSession(paymentSession);
    move.setPaymentCondition(null);
    move.setThirdPartyPayerPartner(thirdPartyPayerPartner);

    return move;
  }

  // Will be override in bank payment module
  @Override
  public String getMoveOrigin(PaymentSession paymentSession) {
    return paymentSession.getSequence();
  }

  protected String getMoveDescription(PaymentSession paymentSession, BigDecimal amount) {
    return String.format(
        "%s - %s%s",
        paymentSession.getName(),
        amount.setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP),
        paymentSession.getCurrency() == null ? "" : paymentSession.getCurrency().getCode());
  }

  @Override
  public String getMoveLineDescription(PaymentSession paymentSession) {
    return String.format("%s : %s", paymentSession.getSequence(), paymentSession.getName());
  }

  protected Move generateMoveLineFromInvoiceTerm(
      PaymentSession paymentSession,
      InvoiceTerm invoiceTerm,
      Move move,
      String origin,
      boolean out,
      BigDecimal reconciliedAmount)
      throws AxelorException {

    BigDecimal amount = invoiceTerm.getAmountPaid().add(reconciliedAmount);
    if (invoiceTerm.getApplyFinancialDiscountOnPaymentSession()) {
      amount = amount.add(invoiceTerm.getFinancialDiscountAmount());
    }

    MoveLine moveLine =
        this.generateMoveLine(
            move,
            invoiceTerm.getMoveLine().getPartner(),
            invoiceTerm.getMoveLine().getAccount(),
            amount,
            origin,
            this.getMoveLineDescription(paymentSession),
            out);

    moveLine.setAmountPaid(reconciliedAmount);

    this.reconcile(paymentSession, invoiceTerm, moveLine);

    recomputeAmountPaid(invoiceTerm.getMoveLine());

    return move;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine generateMoveLine(
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
    BigDecimal amountPaid =
        moveLine.getAmountRemaining().signum() == 0 ? moveLine.getAmountPaid() : BigDecimal.ZERO;

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

    if (paymentSession.getPaymentMode().getInOutSelect() == PaymentModeRepository.OUT) {
      debitMoveLine.setAmountPaid(debitMoveLine.getAmountPaid().subtract(amountPaid));
    } else {
      creditMoveLine.setAmountPaid(creditMoveLine.getAmountPaid().subtract(amountPaid));
    }

    return reconcileService.reconcile(
        debitMoveLine, creditMoveLine, invoicePayment, false, amountPaid.signum() == 0);
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

        if (isGlobal && moveDateMap.get(accountingDate) != null) {
          BigDecimal paymentAmount = BigDecimal.ZERO;
          for (Move move : paymentAmountMap.keySet()) {
            if (accountingDate == null || accountingDate.equals(move.getDate())) {
              paymentAmount = paymentAmount.add(paymentAmountMap.get(move));
            }
          }
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
    Move move = this.createMove(paymentSession, null, null, accountingDate, null);
    String description = this.getMoveLineDescription(paymentSession);

    this.generateCashMoveLine(
        move, null, this.getCashAccount(paymentSession, true), paymentAmount, description, !out);
    this.generateCashMoveLine(
        move, null, this.getCashAccount(paymentSession, false), paymentAmount, description, out);

    moveCutOffService.autoApplyCutOffDates(move);

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
        if (amount.signum() > 0) {
          this.generateCashMoveLine(
              move, partner, cashAccount, amount, this.getMoveLineDescription(paymentSession), out);
        }
      }
    }
  }

  protected void generateTaxMoveLines(Map<LocalDate, Map<Partner, List<Move>>> moveDateMap)
      throws AxelorException {
    for (Map<Partner, List<Move>> moveMap : moveDateMap.values()) {
      for (Partner partner : moveMap.keySet()) {
        for (Move move : moveMap.get(partner)) {
          move = moveRepo.find(move.getId());
          Map<MoveLine, Set<TaxLine>> taxLineMap =
              this.extractTaxLinesFromFinancialDiscountLines(move);

          moveLineTaxService.autoTaxLineGenerate(move, null, false);

          this.applyTaxes(taxLineMap);
        }
      }
    }
  }

  protected Map<MoveLine, Set<TaxLine>> extractTaxLinesFromFinancialDiscountLines(Move move)
      throws AxelorException {
    Map<MoveLine, Set<TaxLine>> taxLineMap = new HashMap<>();
    AppAccount account = appService.getAppAccount();
    if (account == null || !account.getManageFinancialDiscount()) {
      return taxLineMap;
    }
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLineFinancialDiscountService.isFinancialDiscountLine(moveLine, move.getCompany())) {
        taxLineMap.put(moveLine, moveLine.getTaxLineSet());
        moveLine.setTaxLineSet(Sets.newHashSet());
      }
    }

    return taxLineMap;
  }

  protected void applyTaxes(Map<MoveLine, Set<TaxLine>> taxLineMap) {
    for (MoveLine moveLine : taxLineMap.keySet()) {
      moveLine.setTaxLineSet(taxLineMap.get(moveLine));
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

    this.generateMoveLine(
        move, partner, cashAccount, paymentAmount, move.getOrigin(), description, !out);

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

  @Override
  @Transactional
  public InvoiceTerm releaseInvoiceTerm(InvoiceTerm invoiceTerm) {
    if (!invoiceTerm.getIsSelectedOnPaymentSession()) {
      invoiceTerm.setPaymentSession(null);
    }

    invoiceTerm.setIsSelectedOnPaymentSession(false);
    return invoiceTermRepo.save(invoiceTerm);
  }

  @Override
  @Transactional
  public void updateStatus(PaymentSession paymentSession) {
    paymentSession = paymentSessionRepo.find(paymentSession.getId());

    if (paymentSession.getAccountingTriggerSelect()
            == PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE
        || paymentSession.getStatusSelect() == PaymentSessionRepository.STATUS_AWAITING_PAYMENT) {
      paymentSession.setStatusSelect(PaymentSessionRepository.STATUS_CLOSED);
      paymentSession.setValidatedByUser(AuthUtils.getUser());
      paymentSession.setValidatedDate(
          appService.getTodayDateTime(paymentSession.getCompany()).toLocalDateTime());
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
      moveCutOffService.autoApplyCutOffDates(move);
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
        return appService.getTodayDate(paymentSession.getCompany());
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

  @Override
  public int getMoveCount(Map<LocalDate, Map<Partner, List<Move>>> moveDateMap, boolean isGlobal) {

    return moveDateMap.values().stream()
            .map(Map::values)
            .flatMap(Collection::stream)
            .map(List::size)
            .reduce(Integer::sum)
            .orElse(0)
        + (isGlobal ? moveDateMap.values().size() : 0);
  }

  protected void createFinancialDiscountMoveLine(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm, Move move, boolean out)
      throws AxelorException {
    Account financialDiscountAccount =
        financialDiscountService.getFinancialDiscountAccount(paymentSession.getCompany(), out);
    BigDecimal financialDiscountTaxAmount =
        invoiceTermFinancialDiscountService.getFinancialDiscountTaxAmount(invoiceTerm);
    BigDecimal financialDiscountAmount =
        invoiceTerm.getFinancialDiscountAmount().subtract(financialDiscountTaxAmount);
    Map<String, Pair<BigDecimal, BigDecimal>> financialDiscountTaxMap =
        moveLineFinancialDiscountService.getFinancialDiscountTaxMap(invoiceTerm.getMoveLine());
    Map<String, Integer> vatSystemTaxMap =
        moveLineFinancialDiscountService.getVatSystemTaxMap(invoiceTerm.getMoveLine().getMove());
    Map<String, Account> accountTaxMap =
        moveLineFinancialDiscountService.getAccountTaxMap(invoiceTerm.getMoveLine().getMove());

    counter =
        moveLineFinancialDiscountService.createFinancialDiscountMoveLine(
            move,
            null,
            financialDiscountTaxMap,
            vatSystemTaxMap,
            accountTaxMap,
            financialDiscountAccount,
            move.getOrigin(),
            move.getDescription(),
            financialDiscountAmount,
            financialDiscountTaxAmount,
            move.getDate(),
            counter,
            !out,
            financialDiscountTaxAmount.signum() > 0);
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
  public void reconciledInvoiceTermMoves(
      PaymentSession paymentSession,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefundList)
      throws AxelorException {

    TypedQuery<InvoiceTerm> invoiceTermQuery =
        JPA.em()
            .createQuery(
                "SELECT InvoiceTerm FROM InvoiceTerm InvoiceTerm "
                    + " WHERE InvoiceTerm.paymentSession = :paymentSession",
                InvoiceTerm.class);
    invoiceTermQuery.setParameter("paymentSession", paymentSession);

    List<InvoiceTerm> invoiceTermList = invoiceTermQuery.getResultList();

    if (!ObjectUtils.isEmpty(invoiceTermList)) {

      if (paymentSession.getAccountingMethodSelect()
              == PaymentSessionRepository.ACCOUNTING_METHOD_BY_INVOICE_TERM
          && containsCompensativeInvoiceTerm(invoiceTermList, paymentSession)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.COMPENSATION_ON_SESSION_BY_INVOICE_TERM));
      }
      invoiceTermList =
          invoiceTermService.reconcileMoveLineInvoiceTermsWithFullRollBack(
              invoiceTermList, invoiceTermLinkWithRefundList);
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
    return invoiceTermRepo
        .all()
        .filter("self.paymentSession = :paymentSession")
        .bind("paymentSession", paymentSession)
        .fetch()
        .stream()
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
    List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefund =
        new ArrayList<>();

    reconciledInvoiceTermMoves(paymentSession, invoiceTermLinkWithRefund);

    return generateFlashMessage(
        paymentSession, processPaymentSession(paymentSession, invoiceTermLinkWithRefund));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void createAndReconcileMoveLineFromPair(
      PaymentSession paymentSession,
      Move move,
      InvoiceTerm invoiceTerm,
      Pair<InvoiceTerm, BigDecimal> pair,
      AccountConfig accountConfig,
      boolean out,
      Map<Move, BigDecimal> paymentAmountMap)
      throws AxelorException {

    MoveLine moveLine = null;

    InvoiceTerm pairInvoiceTerm =
        Optional.ofNullable(pair)
            .map(Pair::getLeft)
            .map(InvoiceTerm::getId)
            .map(invoiceTermRepo::find)
            .orElse(null);

    if (pairInvoiceTerm == null) {
      return;
    }

    MoveLine pairMoveLine = pairInvoiceTerm.getMoveLine();

    if (pairMoveLine == null) {
      return;
    }

    Move pairMove = pairMoveLine.getMove();
    if (accountConfig != null
        && pairMove.getStatusSelect() == MoveRepository.STATUS_DAYBOOK
        && accountConfig.getAccountingDaybook()
        && accountConfig.getAccountAtPayment()) {
      moveValidateService.accounting(pairMove);
    }

    if (!CollectionUtils.isEmpty(move.getMoveLineList())) {
      moveLine =
          move.getMoveLineList().stream()
              .filter(
                  ml ->
                      ml.getOrigin() != null
                          && pairMoveLine.getOrigin() != null
                          && ml.getOrigin().equals(pairMoveLine.getOrigin()))
              .findFirst()
              .orElse(null);
    }

    if (moveLine != null) {
      this.updateMoveLine(moveLine, pair.getRight(), out);
    } else {
      moveLine =
          this.generateMoveLine(
              move,
              invoiceTerm.getMoveLine().getPartner(),
              pairMoveLine.getAccount(),
              pair.getRight(),
              move.getOrigin(),
              this.getMoveLineDescription(paymentSession),
              !out);
    }

    paymentAmountMap.replace(move, paymentAmountMap.get(move).subtract(pair.getRight()));

    MoveLine creditMoveLine = null;
    MoveLine debitMoveLine = null;
    if (pairMove.getFunctionalOriginSelect() == MoveRepository.FUNCTIONAL_ORIGIN_SALE) {
      creditMoveLine = moveLine;
      debitMoveLine = pairMoveLine;
    } else if (pairMove.getFunctionalOriginSelect() == MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE) {
      creditMoveLine = pairMoveLine;
      debitMoveLine = moveLine;
    }

    Reconcile invoiceTermsReconcile =
        reconcileService.createReconcile(debitMoveLine, creditMoveLine, pair.getRight(), true);

    reconcileService.confirmReconcile(invoiceTermsReconcile, false, false);

    invoicePaymentCreateService.updateInvoiceTermsAmounts(
        invoiceTerm, pair.getRight(), invoiceTermsReconcile, pairMove, paymentSession, false);
    invoicePaymentCreateService.updateInvoiceTermsAmounts(
        pairInvoiceTerm,
        pair.getRight(),
        invoiceTermsReconcile,
        invoiceTerm.getMoveLine().getMove(),
        paymentSession,
        true);
  }

  protected void updateMoveLine(MoveLine moveLine, BigDecimal amountToAdd, boolean out) {
    if (out) {
      moveLine.setCredit(moveLine.getCredit().add(amountToAdd));
    } else {
      moveLine.setDebit(moveLine.getDebit().add(amountToAdd));
    }

    moveLine.setCurrencyAmount(moveLine.getCurrencyAmount().add(amountToAdd));

    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      InvoiceTerm invoiceTerm = moveLine.getInvoiceTermList().get(0);

      BigDecimal newAmount = moveLine.getCurrencyAmount().abs();
      BigDecimal amountPaid = invoiceTerm.getAmount().subtract(invoiceTerm.getAmountRemaining());

      invoiceTerm.setAmountRemaining(newAmount.subtract(amountPaid));
      invoiceTerm.setAmount(newAmount);
      invoiceTerm.setCompanyAmountRemaining(invoiceTerm.getAmountRemaining());
      invoiceTerm.setCompanyAmount(invoiceTerm.getAmount());
    }
  }

  @Override
  public boolean containsCompensativeInvoiceTerm(
      List<InvoiceTerm> invoiceTermList, PaymentSession paymentSession) {
    if (!CollectionUtils.isEmpty(invoiceTermList)) {
      if (paymentSession.getPartnerTypeSelect() == PaymentSessionRepository.PARTNER_TYPE_CUSTOMER) {
        return invoiceTermList.stream()
            .anyMatch(
                invoiceTerm ->
                    invoiceTerm.getMoveLine().getMove().getFunctionalOriginSelect()
                        == MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE);
      } else if (paymentSession.getPartnerTypeSelect()
          == PaymentSessionRepository.PARTNER_TYPE_SUPPLIER) {
        return invoiceTermList.stream()
            .anyMatch(
                invoiceTerm ->
                    invoiceTerm.getMoveLine().getMove().getFunctionalOriginSelect()
                        == MoveRepository.FUNCTIONAL_ORIGIN_SALE);
      }
    }
    return false;
  }

  protected void recomputeAmountPaid(MoveLine moveLine) {
    if (!CollectionUtils.isEmpty(moveLine.getInvoiceTermList())) {
      moveLine.getInvoiceTermList().stream()
          .filter(InvoiceTerm::getIsPaid)
          .map(InvoiceTerm::getPaymentAmount)
          .reduce(BigDecimal::add)
          .ifPresent(moveLine::setAmountPaid);
    }
  }
}
