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
import com.axelor.apps.base.db.*;
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
  @Transactional(rollbackOn = {Exception.class})
  protected void process() {
    List<Long> tradingNameIds =
        batch.getAccountingBatch().getTradingNameSet().stream()
            .map(TradingName::getId)
            .collect(Collectors.toList());

    boolean isMoveManagedByPartner = batch.getAccountingBatch().getIsMoveManagedByPartners();

    if (!CollectionUtils.isEmpty(tradingNameIds)) {
      for (Long tradingNameId : tradingNameIds) {
        try {
          findBatch();

          TradingName tradingName = tradingNameRepository.find(tradingNameId);
          Period period = batch.getAccountingBatch().getPeriod();
          Company company = batch.getAccountingBatch().getCompany();

          generateMoves(tradingName, period, company, isMoveManagedByPartner);
          incrementDone();
        } catch (Exception e) {
          TraceBackService.trace(e);
          incrementAnomaly();
        }
        JPA.clear();
      }
    } else {
      try {
        findBatch();

        Period period = batch.getAccountingBatch().getPeriod();
        Company company = batch.getAccountingBatch().getCompany();

        generateMoves(null, period, company, isMoveManagedByPartner);
        incrementDone();
      } catch (Exception e) {
        TraceBackService.trace(e);
        incrementAnomaly();
      }
      JPA.clear();
    }
  }

  public void generateMoves(
      TradingName tradingName, Period period, Company company, boolean isMoveManagedByPartners)
      throws AxelorException {
    List<Invoice> invoiceList =
        invoiceService.getValidatedInvoiceListForFiscalPeriod(tradingName, period, company);

    if (CollectionUtils.isEmpty(invoiceList)) {
      return;
    }

    if (isMoveManagedByPartners) {
      Map<Partner, List<Invoice>> partnerMap = invoiceToolService.getPartnerMap(invoiceList);

      for (Entry<Partner, List<Invoice>> entry : partnerMap.entrySet()) {
        generateMoves(entry.getValue(), tradingName, period, company, entry.getKey());
      }
    } else {
      generateMoves(invoiceList, tradingName, period, company, null);
    }
  }

  public void generateMoves(
      List<Invoice> invoiceList,
      TradingName tradingName,
      Period period,
      Company company,
      Partner partner)
      throws AxelorException {

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    Journal journal = accountConfig.getCustomerSalesJournal();
    Account account = accountConfig.getCustomerAccount();
    LocalDate date = LocalDate.now();

    this.logGenerateMove(tradingName, period, journal);

    Map<Currency, List<Invoice>> currencyMap = invoiceToolService.getCurrencyMap(invoiceList);

    for (Entry<Currency, List<Invoice>> entry : currencyMap.entrySet()) {
      List<Invoice> invoiceListByCurrency = entry.getValue();
      Currency currency = entry.getKey();
      generateMovesByCurrency(
          tradingName,
          period,
          company,
          partner,
          accountConfig,
          journal,
          account,
          date,
          invoiceListByCurrency,
          currency);
    }
  }

  private void generateMovesByCurrency(
      TradingName tradingName,
      Period period,
      Company company,
      Partner partner,
      AccountConfig accountConfig,
      Journal journal,
      Account account,
      LocalDate date,
      List<Invoice> invoiceListByCurrency,
      Currency currency)
      throws AxelorException {
    Map<Account, BigDecimal> amountMap = new HashMap<>();
    Map<Tax, BigDecimal> taxMap = new HashMap<>();
    Map<PaymentMode, BigDecimal> paymentMap = new HashMap<>();

    for (Invoice invoice : invoiceListByCurrency) {
      amountMap = invoiceToolService.getAmountMap(amountMap, invoice.getInvoiceLineList());
      taxMap = invoiceToolService.getTaxMap(taxMap, invoice.getInvoiceLineTaxList());
      paymentMap =
          invoiceToolService.getPaymentMap(paymentMap, invoice.getInvoicePaymentList(), invoice);
    }

    generateMove(
        journal,
        account,
        company,
        partner,
        currency,
        date,
        period,
        tradingName,
        invoiceListByCurrency,
        accountConfig,
        amountMap,
        taxMap,
        paymentMap);
  }

  protected void generateMove(
      Journal journal,
      Account account,
      Company company,
      Partner partner,
      Currency currency,
      LocalDate date,
      Period period,
      TradingName tradingName,
      List<Invoice> invoiceList,
      AccountConfig accountConfig,
      Map<Account, BigDecimal> amountMap,
      Map<Tax, BigDecimal> taxMap,
      Map<PaymentMode, BigDecimal> paymentMap)
      throws AxelorException {

    Move move =
        moveCreateService.createMove(
            journal,
            company,
            partner,
            currency,
            period,
            date,
            tradingName,
            null,
            MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE,
            null,
            journal.getDescriptionModel());

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

    MoveLine debitMoveLine = generateDebitMoveLine(account, date, move, ref, total);

    moveRepository.save(move);

    Map<PaymentMode, Move> paymentMoveMap = new HashMap<>();

    for (Entry<PaymentMode, BigDecimal> entry : paymentMap.entrySet()) {
      Move paymentMove =
          generatePaymentMove(
              tradingName,
              period,
              company,
              partner,
              currency,
              account,
              date,
              move,
              debitMoveLine,
              entry);
      paymentMoveMap.put(entry.getKey(), paymentMove);
    }

    for (Invoice invoice : invoiceList) {
      ventilateInvoice(move, invoice, accountConfig);
      generateInvoicePayment(date, paymentMoveMap, invoice);
    }

    String origin =
        invoiceList.stream().map(Invoice::getInvoiceId).collect(Collectors.toList()).toString();

    move.setOrigin(origin);

    Boolean dayBookMode =
        accountConfigService.getAccountConfig(move.getCompany()).getAccountingDaybook()
            && move.getJournal().getAllowAccountingDaybook();

    moveValidateService.updateValidateStatus(move, dayBookMode);
  }

  @Transactional
  protected InvoicePayment generateInvoicePayment(
      LocalDate date, Map<PaymentMode, Move> paymentMoveMap, Invoice invoice) {
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

  protected Move generatePaymentMove(
      TradingName tradingName,
      Period period,
      Company company,
      Partner partner,
      Currency currency,
      Account account,
      LocalDate date,
      Move move,
      MoveLine debitMoveLine,
      Entry<PaymentMode, BigDecimal> entry)
      throws AxelorException {
    Journal paymentModeJournal =
        paymentModeService.getPaymentModeJournal(
            entry.getKey(), company, company.getDefaultBankDetails());

    Move paymentMove =
        moveCreateService.createMove(
            paymentModeJournal,
            company,
            partner,
            currency,
            period,
            date,
            tradingName,
            entry.getKey(),
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            move.getOrigin(),
            paymentModeJournal.getDescriptionModel());

    MoveLine moveLine1 =
        moveLineCreateService.createMoveLine(
            move, partner, account, entry.getValue(), false, date, 1, null, null);
    paymentMove.addMoveLineListItem(moveLine1);

    Account paymentModeAccount =
        paymentModeService.getPaymentModeAccount(
            entry.getKey(), company, company.getDefaultBankDetails());
    MoveLine moveLine2 =
        moveLineCreateService.createMoveLine(
            move, partner, paymentModeAccount, entry.getValue(), true, date, 2, null, null);
    paymentMove.addMoveLineListItem(moveLine2);

    reconcileService.reconcile(debitMoveLine, moveLine1, true, false);

    moveRepository.save(paymentMove);

    moveValidateService.validateWellBalancedMove(paymentMove);
    return paymentMove;
  }

  protected MoveLine generateDebitMoveLine(
      Account account, LocalDate date, Move move, int ref, BigDecimal total)
      throws AxelorException {
    MoveLine debitMoveLine =
        moveLineCreateService.createMoveLine(
            move, null, account, total, true, date, ref, null, null);
    move.addMoveLineListItem(debitMoveLine);
    return debitMoveLine;
  }

  protected int generateTaxMoveLine(
      Company company, LocalDate date, Move move, int ref, Entry<Tax, BigDecimal> entry)
      throws AxelorException {
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

  protected int generateAccountMoveLine(
      LocalDate date, Move move, int ref, Entry<Account, BigDecimal> entry) throws AxelorException {
    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
            move,
            null,
            entry.getKey(),
            entry.getValue(),
            false,
            date,
            ref++,
            null,
            null);
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
