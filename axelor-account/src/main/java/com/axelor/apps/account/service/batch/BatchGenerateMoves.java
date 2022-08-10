package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.*;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.TradingNameRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.shiro.util.CollectionUtils;

public class BatchGenerateMoves extends BatchStrategy {
  protected final MoveRepository moveRepository;
  protected final MoveCreateService moveCreateService;
  protected final PaymentModeService paymentModeService;
  protected final ReconcileService reconcileService;
  protected final SequenceService sequenceService;
  protected final MoveValidateService moveValidateService;
  protected final InvoiceService invoiceService;
  protected final TradingNameRepository tradingNameRepository;
  protected final AccountConfigService accountConfigService;
  protected final InvoicePaymentRepository invoicePaymentRepository;
  protected final InvoiceToolService invoiceToolService;
  protected final MoveLineCreateService moveLineCreateService;
  protected final TaxAccountService taxAccountService;

  @Inject
  public BatchGenerateMoves(
      MoveRepository moveRepository,
      PaymentModeService paymentModeService,
      ReconcileService reconcileService,
      SequenceService sequenceService,
      MoveValidateService moveValidateService,
      InvoiceService invoiceService,
      TradingNameRepository tradingNameRepository,
      AccountConfigService accountConfigService,
      InvoicePaymentRepository invoicePaymentRepository,
      MoveCreateService moveCreateService,
      InvoiceToolService invoiceToolService,
      MoveLineCreateService moveLineCreateService,
      TaxAccountService taxAccountService) {
    this.moveRepository = moveRepository;
    this.paymentModeService = paymentModeService;
    this.reconcileService = reconcileService;
    this.sequenceService = sequenceService;
    this.moveValidateService = moveValidateService;
    this.invoiceService = invoiceService;
    this.tradingNameRepository = tradingNameRepository;
    this.accountConfigService = accountConfigService;
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.moveCreateService = moveCreateService;
    this.invoiceToolService = invoiceToolService;
    this.moveLineCreateService = moveLineCreateService;
    this.taxAccountService = taxAccountService;
  }

  @Override
  protected void process() {
    List<Long> tradingNameIds =
        batch.getAccountingBatch().getTradingNameSet().stream()
            .map(TradingName::getId)
            .collect(Collectors.toList());

    Period period = batch.getAccountingBatch().getPeriod();
    Company company = batch.getAccountingBatch().getCompany();

    if (!CollectionUtils.isEmpty(tradingNameIds)) {
      for (Long tradingNameId : tradingNameIds) {
        checkPoint();
        TradingName tradingName = tradingNameRepository.find(tradingNameId);
        try {
          generateMoves(tradingName, period, company);
          incrementDone();
        } catch (Exception e) {
          TraceBackService.trace(e);
          incrementAnomaly();
        }
        JPA.clear();
      }
    } else {
      try {
        generateMoves(null, period, company);
        incrementDone();
      } catch (Exception e) {
        TraceBackService.trace(e);
        incrementAnomaly();
      }
      JPA.clear();
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void generateMoves(TradingName tradingName, Period period, Company company)
      throws AxelorException {
    List<Invoice> invoiceList =
            invoiceService.getValidatedInvoiceListForFiscalPeriod(
                    tradingName, period, company);

    if (CollectionUtils.isEmpty(invoiceList)) {
      return;
    }

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    Journal journal = accountConfig.getCustomerSalesJournal();
    Account account = accountConfig.getCustomerAccount();
    LocalDate date = LocalDate.now();

    this.logGenerateMove(tradingName, period, journal);

    Map<Account, BigDecimal> amountMap = new HashMap<>();
    Map<Tax, BigDecimal> taxMap = new HashMap<>();
    Map<PaymentMode, BigDecimal> paymentMap = new HashMap<>();

    for (Invoice invoice : invoiceList) {
      amountMap = invoiceToolService.getAmountMap(amountMap, invoice.getInvoiceLineList());
      taxMap = invoiceToolService.getTaxMap(taxMap, invoice.getInvoiceLineTaxList());
      paymentMap = invoiceToolService.getPaymentMap(paymentMap, invoice.getInvoicePaymentList(), invoice);
    }

    generateMove(tradingName, period, company, invoiceList, accountConfig, journal, account, date, amountMap, taxMap, paymentMap);
  }

  protected void generateMove(
          TradingName tradingName,
          Period period,
          Company company,
          List<Invoice> invoiceList,
          AccountConfig accountConfig,
          Journal journal,
          Account account,
          LocalDate date,
          Map<Account, BigDecimal> amountMap,
          Map<Tax, BigDecimal> taxMap,
          Map<PaymentMode, BigDecimal> paymentMap) throws AxelorException {
    Move move = moveCreateService.createMove(journal, company, period, date, tradingName, null);

    int ref = 1;
    BigDecimal total = BigDecimal.ZERO;

    for (Entry<Account, BigDecimal> entry : amountMap.entrySet()) {
      ref = generateAccountMoveLine(date, move, ref, entry);
      total = total.add(entry.getValue());
    }

    for (Entry<Tax, BigDecimal> entry : taxMap.entrySet()) {
      ref = generateTaxMoveLine(company, date, move, ref, entry);
      total = total.add(entry.getValue());
    }

    moveRepository.save(move);
    move.setReference(sequenceService.getDraftSequenceNumber(move));

    MoveLine debitMoveLine = generateDebitMoveLine(account, date, move, ref, total);

    Map<PaymentMode, Move> paymentMoveMap = new HashMap<>();

    for (Entry<PaymentMode, BigDecimal> entry : paymentMap.entrySet()) {
      Move paymentMove = generatePaymentMove(tradingName, period, company, account, date, move, debitMoveLine, entry);
      paymentMoveMap.put(entry.getKey(), paymentMove);
    }

    for (Invoice invoice : invoiceList) {
      ventilateInvoice(move, invoice, accountConfig);
      generateInvoicePayment(date, paymentMoveMap, invoice);
    }

    moveValidateService.updateValidateStatus(move, false);
  }

  @Transactional
  protected InvoicePayment generateInvoicePayment(
          LocalDate date,
          Map<PaymentMode, Move> paymentMoveMap,
          Invoice invoice) {
    InvoicePayment invoicePayment = new InvoicePayment();
    invoicePayment.setAmount(invoice.getInTaxTotal());
    invoicePayment.setCurrency(invoice.getCurrency());
    invoicePayment.setInvoice(invoice);
    invoicePayment.setTypeSelect(InvoicePaymentRepository.TYPE_PAYMENT);
    invoicePayment.setPaymentMode(invoice.getPaymentMode());
    invoicePayment.setMove(paymentMoveMap.get(invoice.getPaymentMode()));
    invoicePayment.setPaymentDate(date);
    invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
    return invoicePaymentRepository.save(invoicePayment);
  }

  private void ventilateInvoice(Move move, Invoice invoice, AccountConfig accountConfig)
          throws AxelorException {
    invoice.addBatchSetItem(getBatch());
    invoice.setMove(move);
    invoice.setStatusSelect(InvoiceRepository.STATUS_VENTILATED);
    invoice.setAmountPaid(invoice.getInTaxTotal());
    invoice.setAmountRemaining(BigDecimal.ZERO);
    invoice.setInvoiceId(
            sequenceService.getSequenceNumber(
                    accountConfigService.getCustInvSequence(accountConfig), invoice.getInvoiceDate()));
  }

  protected Move generatePaymentMove(TradingName tradingName, Period period, Company company, Account account, LocalDate date, Move move, MoveLine debitMoveLine, Entry<PaymentMode, BigDecimal> entry) throws AxelorException {
    Journal paymentModeJournal =
        paymentModeService.getPaymentModeJournal(
            entry.getKey(), company, company.getDefaultBankDetails());

    Move paymentMove =
        moveCreateService.createMove(
            paymentModeJournal, company, period, date, tradingName, entry.getKey());

    MoveLine moveLine1 =
        moveLineCreateService.createMoveLine(
                move, null, account, entry.getValue(), false, date, 1, null, null);
    paymentMove.addMoveLineListItem(moveLine1);

    Account paymentModeAccount =
        paymentModeService.getPaymentModeAccount(
            entry.getKey(), company, company.getDefaultBankDetails());
    MoveLine moveLine2 =
        moveLineCreateService.createMoveLine(
                move, null, paymentModeAccount, entry.getValue(), true, date, 2, null, null);
    paymentMove.addMoveLineListItem(moveLine2);

    reconcileService.reconcile(debitMoveLine, moveLine1, true, false);
    paymentMove.setReference(sequenceService.getDraftSequenceNumber(paymentMove));
    moveRepository.save(paymentMove);
    moveValidateService.validateWellBalancedMove(paymentMove);
    return paymentMove;
  }

  protected MoveLine generateDebitMoveLine(Account account, LocalDate date, Move move, int ref, BigDecimal total) throws AxelorException {
    MoveLine debitMoveLine =
        moveLineCreateService.createMoveLine(
                move, null, account, total, true, date, ref, null, null);
    move.addMoveLineListItem(debitMoveLine);
    return debitMoveLine;
  }

  protected int generateTaxMoveLine(Company company, LocalDate date, Move move, int ref, Entry<Tax, BigDecimal> entry) throws AxelorException {
    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
                move,
            null,
            taxAccountService.getAccount(entry.getKey(), company, false, true),
            entry.getValue(),
            false,
                date,
            ref++,
            null,
            null);
    move.addMoveLineListItem(moveLine);
    return ref;
  }

  protected int generateAccountMoveLine(LocalDate date, Move move, int ref, Entry<Account, BigDecimal> entry) throws AxelorException {
    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
                move, null, entry.getKey(), entry.getValue(), false, date, ref++, null, null);
    move.addMoveLineListItem(moveLine);
    return ref;
  }

  protected void logGenerateMove(TradingName tradingName, Period period, Journal journal) {
    if (tradingName != null) {
      LOG.debug(
          "Création d'une écriture comptable spécifique à la période {} (Magasin : {}, Journal : {})",
          period.getCode(),
          tradingName.getName(),
          journal.getCode());
    } else {
      LOG.debug(
          "Création d'une écriture comptable spécifique à la période {} (Journal : {})",
          period.getCode(),
          journal.getCode());
    }
  }
}
