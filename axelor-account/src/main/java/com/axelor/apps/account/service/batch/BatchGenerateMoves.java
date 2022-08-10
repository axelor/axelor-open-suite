package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.*;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
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
import java.util.ArrayList;
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
  protected final InvoiceRepository invoiceRepository;
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
      InvoiceRepository invoiceRepository,
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
    this.invoiceRepository = invoiceRepository;
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
    List<Invoice> invoiceList = new ArrayList<>();
    if (tradingName != null) {
      invoiceList =
          invoiceRepository
              .all()
              .filter(
                  "self.statusSelect = ? AND self.operationTypeSelect = ? AND MONTH(self.invoiceDate) = ? AND YEAR(self.invoiceDate) = ? AND self.tradingName = ? and self.company = ?",
                  InvoiceRepository.STATUS_VALIDATED,
                  InvoiceRepository.OPERATION_TYPE_CLIENT_SALE,
                  period.getFromDate().getMonthValue(),
                  period.getFromDate().getYear(),
                  tradingName,
                  company)
              .fetch();
    } else {
      invoiceList =
          invoiceRepository
              .all()
              .filter(
                  "self.statusSelect = ? AND self.operationTypeSelect = ? AND MONTH(self.invoiceDate) = ? AND YEAR(self.invoiceDate) = ? AND self.company = ?",
                  InvoiceRepository.STATUS_VALIDATED,
                  InvoiceRepository.OPERATION_TYPE_CLIENT_SALE,
                  period.getFromDate().getMonthValue(),
                  period.getFromDate().getYear(),
                  company)
              .fetch();
    }

    if (CollectionUtils.isEmpty(invoiceList)) {
      return;
    }

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    Journal journal = accountConfig.getCustomerSalesJournal();
    Account account = accountConfig.getCustomerAccount();
    LocalDate date = LocalDate.now();

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

    Move move = moveCreateService.createMove(journal, company, period, date, tradingName, null);

    Map<Account, BigDecimal> amountMap = new HashMap<>();
    Map<Tax, BigDecimal> taxMap = new HashMap<>();
    Map<PaymentMode, BigDecimal> paymentMap = new HashMap<>();

    for (Invoice invoice : invoiceList) {
      amountMap = invoiceToolService.getAmountMap(amountMap, invoice.getInvoiceLineList());
      taxMap = invoiceToolService.getTaxMap(taxMap, invoice.getInvoiceLineTaxList());
      paymentMap = invoiceToolService.getPaymentMap(paymentMap, invoice.getInvoicePaymentList(), invoice);
    }

    int ref = 1;
    BigDecimal total = BigDecimal.ZERO;
    for (Entry<Account, BigDecimal> entry : amountMap.entrySet()) {
      MoveLine moveLine =
          moveLineCreateService.createMoveLine(
              move, null, entry.getKey(), entry.getValue(), false, date, ref++, null, null);
      move.addMoveLineListItem(moveLine);

      total = total.add(entry.getValue());
    }

    for (Entry<Tax, BigDecimal> entry : taxMap.entrySet()) {
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

      total = total.add(entry.getValue());
    }

    moveRepository.save(move);
    move.setReference(sequenceService.getDraftSequenceNumber(move));

    MoveLine debitMoveLine =
        moveLineCreateService.createMoveLine(
            move, null, account, total, true, date, ref, null, null);
    move.addMoveLineListItem(debitMoveLine);

    Map<PaymentMode, Move> paymentMoveMap = new HashMap<>();

    for (Entry<PaymentMode, BigDecimal> entry : paymentMap.entrySet()) {
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
      paymentMoveMap.put(entry.getKey(), paymentMove);
    }

    for (Invoice invoice : invoiceList) {
      invoice.addBatchSetItem(getBatch());
      invoice.setMove(move);
      invoice.setStatusSelect(InvoiceRepository.STATUS_VENTILATED);
      invoice.setAmountPaid(invoice.getInTaxTotal());
      invoice.setAmountRemaining(BigDecimal.ZERO);

      InvoicePayment invoicePayment = new InvoicePayment();
      invoicePayment.setAmount(invoice.getInTaxTotal());
      invoicePayment.setCurrency(invoice.getCurrency());
      invoicePayment.setInvoice(invoice);
      invoicePayment.setTypeSelect(InvoicePaymentRepository.TYPE_PAYMENT);
      invoicePayment.setPaymentMode(invoice.getPaymentMode());
      invoicePayment.setMove(paymentMoveMap.get(invoice.getPaymentMode()));
      invoicePayment.setPaymentDate(date);
      invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
      invoice.setInvoiceId(
          sequenceService.getSequenceNumber(
              accountConfigService.getCustInvSequence(accountConfig), invoice.getInvoiceDate()));
      invoicePaymentRepository.save(invoicePayment);
    }
    moveValidateService.updateValidateStatus(move, false);
  }
}
