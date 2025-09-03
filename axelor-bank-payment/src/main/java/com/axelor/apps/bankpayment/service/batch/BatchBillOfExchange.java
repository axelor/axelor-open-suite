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
package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.batch.BatchStrategy;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermReplaceService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchBillOfExchange extends BatchStrategy {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected InvoiceRepository invoiceRepository;
  protected AppAccountService appAccountService;
  protected MoveCreateService moveCreateService;
  protected AccountConfigService accountConfigService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveRepository moveRepository;
  protected JournalRepository journalRepository;
  protected AccountRepository accountRepository;
  protected MoveValidateService moveValidateService;
  protected AccountingBatchRepository accountingBatchRepository;
  protected InvoiceTermReplaceService invoiceTermReplaceService;

  @Inject
  public BatchBillOfExchange(
      InvoiceRepository invoiceRepository,
      AppAccountService appAccountService,
      MoveCreateService moveCreateService,
      AccountConfigService accountConfigService,
      MoveLineCreateService moveLineCreateService,
      JournalRepository journalRepository,
      MoveRepository moveRepository,
      AccountRepository accountRepository,
      MoveValidateService moveValidateService,
      AccountingBatchRepository accountingBatchRepository,
      InvoiceTermReplaceService invoiceTermReplaceService) {
    super();
    this.invoiceRepository = invoiceRepository;
    this.appAccountService = appAccountService;
    this.moveCreateService = moveCreateService;
    this.accountConfigService = accountConfigService;
    this.moveLineCreateService = moveLineCreateService;
    this.journalRepository = journalRepository;
    this.moveRepository = moveRepository;
    this.accountRepository = accountRepository;
    this.moveValidateService = moveValidateService;
    this.accountingBatchRepository = accountingBatchRepository;
    this.invoiceTermReplaceService = invoiceTermReplaceService;
  }

  @Override
  protected void process() {
    AccountingBatch accountingBatch = batch.getAccountingBatch();

    if (accountingBatch.getPaymentMode() == null
        || !accountingBatch.getPaymentMode().getGenerateBankOrder()) {
      return;
    }

    List<Long> anomalyList = Lists.newArrayList(0L); // Can't pass an empty collection to the query
    Query<Invoice> query = buildOrderedQueryFetchInvoices(accountingBatch, anomalyList);
    // Creation des ecriture comptable lcr + modification des factures
    createLCRAccountingMovesForInvoices(query, anomalyList, accountingBatch);
  }

  protected void createLCRAccountingMovesForInvoices(
      Query<Invoice> query, List<Long> anomalyList, AccountingBatch accountingBatch) {
    List<Invoice> invoicesList = null;
    while (!(invoicesList = query.bind("anomalyList", anomalyList).fetch(getFetchLimit()))
        .isEmpty()) {
      accountingBatch = accountingBatchRepository.find(accountingBatch.getId());
      for (Invoice invoice : invoicesList) {
        try {
          createMoveAndUpdateInvoice(accountingBatch, invoice);
          incrementDone();
        } catch (Exception e) {
          anomalyList.add(invoice.getId());
          incrementAnomaly();
          TraceBackService.trace(
              e, "billOfExchangeBatch: create lcr accounting move", batch.getId());
          break;
        }
      }
      JPA.clear();
      findBatch();
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void createMoveAndUpdateInvoice(AccountingBatch accountingBatch, Invoice invoice)
      throws AxelorException {

    if (invoice.getBankDetails() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(
              BankPaymentExceptionMessage
                  .BATCH_BILL_OF_EXCHANGE_BANK_DETAILS_IS_MISSING_ON_INVOICE),
          invoice.getInvoiceId());
    }
    if (!invoice.getBankDetails().getActive()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(
              BankPaymentExceptionMessage
                  .BATCH_BILL_OF_EXCHANGE_BANK_DETAILS_IS_INACTIVE_ON_INVOICE),
          invoice.getBankDetails().getFullName(),
          invoice.getInvoiceId(),
          invoice.getPartner().getPartnerSeq());
    }
    AccountConfig accountConfig =
        accountConfigService.getAccountConfig(accountingBatch.getCompany());
    Move placementMove = createLCRAccountMove(invoice, accountConfig, accountingBatch);
    moveValidateService.accounting(placementMove);

    invoiceTermReplaceService.replaceInvoiceTerms(
        invoice, placementMove, invoice.getMove().getMoveLineList(), invoice.getPartnerAccount());
    updateInvoice(invoice, placementMove, accountConfig);
  }

  /**
   * Update invoice by setting lrc accounted to true and remplacing current move my move. Also add
   * current batch in invoice batchSet.
   *
   * @param invoice
   * @param move
   * @param accountConfig
   */
  protected void updateInvoice(Invoice invoice, Move move, AccountConfig accountConfig) {

    invoice.setOldMove(invoice.getMove());
    invoice.setMove(move);
    invoice.setPartnerAccount(
        accountRepository.find(accountConfig.getBillOfExchReceivAccount().getId()));
    invoice.setLcrAccounted(true);
    invoice.addBatchSetItem(batchRepo.find(batch.getId()));
  }

  @Transactional(rollbackOn = {Exception.class})
  protected Move createLCRAccountMove(
      Invoice invoice, AccountConfig accountConfig, AccountingBatch accountingBatch)
      throws AxelorException {
    log.debug("Creating lcr account move for invoice {}", invoice);
    if (accountConfig.getBillOfExchReceivAccount() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.BATCH_BILL_OF_EXCHANGE_ACCOUNT_MISSING),
          I18n.get("Bill of exchange receivable account"));
    }
    Move move =
        moveCreateService.createMove(
            journalRepository.find(accountingBatch.getBillOfExchangeJournal().getId()),
            invoice.getCompany(),
            invoice.getCurrency(),
            invoice.getPartner(),
            invoice.getPaymentMode(),
            invoice.getFiscalPosition(),
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            invoice.getInvoiceId(),
            null,
            invoice.getCompanyBankDetails());
    if (move != null) {
      move.setPaymentCondition(invoice.getPaymentCondition());

      LocalDate todayDate = this.appBaseService.getTodayDate(invoice.getCompany());
      MoveLine creditMoveLine =
          moveLineCreateService.createMoveLine(
              move,
              invoice.getPartner(),
              accountRepository.find(invoice.getPartnerAccount().getId()),
              invoice.getAmountRemaining(),
              false,
              todayDate,
              1,
              invoice.getInvoiceId(),
              null);
      log.debug("Created move line {}", creditMoveLine);
      MoveLine debitMoveLine =
          moveLineCreateService.createMoveLine(
              move,
              invoice.getPartner(),
              accountRepository.find(accountConfig.getBillOfExchReceivAccount().getId()),
              invoice.getAmountRemaining(),
              true,
              todayDate,
              2,
              invoice.getInvoiceId(),
              null);
      log.debug("Created move line {}", creditMoveLine);

      move.addMoveLineListItem(creditMoveLine);
      move.addMoveLineListItem(debitMoveLine);
      move.addBatchSetItem(batchRepo.find(batch.getId()));
      move = moveRepository.save(move);
    }

    log.debug("Created move {}", move);
    return move;
  }

  protected Query<Invoice> buildOrderedQueryFetchInvoices(
      AccountingBatch accountingBatch, List<Long> anomalyList) {
    StringBuilder filter = new StringBuilder();
    boolean manageMultiBanks = appAccountService.getAppBase().getManageMultiBanks();
    filter.append(
        "self.operationTypeSelect = :operationTypeSelect "
            + "AND self.statusSelect = :statusSelect "
            + "AND self.amountRemaining > 0 "
            + "AND self.company = :company "
            + "AND self.hasPendingPayments = FALSE "
            + "AND self.id NOT IN (:anomalyList) "
            + "AND self.paymentMode = :paymentMode "
            + "AND self.lcrAccounted = FALSE "
            + "AND (self.billOfExchangeBlockingOk = FALSE OR (self.billOfExchangeBlockingOk = TRUE AND self.billOfExchangeBlockingToDate < :dueDate))");

    Map<String, Object> bindings = new HashMap<>();
    bindings.put("operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE);
    bindings.put("statusSelect", InvoiceRepository.STATUS_VENTILATED);
    bindings.put("company", accountingBatch.getCompany());
    bindings.put("paymentMode", accountingBatch.getPaymentMode());
    bindings.put("anomalyList", anomalyList);
    bindings.put("dueDate", accountingBatch.getDueDate());

    if (accountingBatch.getDueDate() != null) {
      filter.append("AND self.dueDate <= :dueDate ");
    }
    if (accountingBatch.getCurrency() != null) {
      filter.append("AND self.currency = :currency ");
      bindings.put("currency", accountingBatch.getCurrency());
    }

    if (accountingBatch.getBankDetails() != null) {
      filter.append(" AND self.companyBankDetails IN (:bankDetailsSet) ");
      Set<BankDetails> bankDetailsSet = Sets.newHashSet(accountingBatch.getBankDetails());
      if (manageMultiBanks && accountingBatch.getIncludeOtherBankAccounts()) {
        bankDetailsSet.addAll(accountingBatch.getCompany().getBankDetailsList());
      }
      bindings.put("bankDetailsSet", bankDetailsSet);
    }

    Query<Invoice> query =
        invoiceRepository.all().filter(filter.toString()).bind(bindings).order("id");
    return query;
  }

  @Override
  protected void stop() {
    StringBuilder sb = new StringBuilder();
    sb.append(I18n.get(BaseExceptionMessage.ABSTRACT_BATCH_REPORT)).append(" ");
    sb.append(
        String.format(
            I18n.get(
                    BaseExceptionMessage.ABSTRACT_BATCH_DONE_SINGULAR,
                    BaseExceptionMessage.ABSTRACT_BATCH_DONE_PLURAL,
                    batch.getDone())
                + " ",
            batch.getDone()));
    sb.append(
        String.format(
            I18n.get(
                BaseExceptionMessage.ABSTRACT_BATCH_ANOMALY_SINGULAR,
                BaseExceptionMessage.ABSTRACT_BATCH_ANOMALY_PLURAL,
                batch.getAnomaly()),
            batch.getAnomaly()));
    addComment(sb.toString());
    super.stop();
  }
}
