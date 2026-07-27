/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.batch.BatchStrategy;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermReplaceService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionValidateService;
import com.axelor.apps.account.service.reconcile.ReconcileService;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.common.collect.Lists;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchBillOfExchangeInvoiceTerm extends BatchStrategy {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected InvoiceTermRepository invoiceTermRepository;
  protected AppAccountService appAccountService;
  protected MoveCreateService moveCreateService;
  protected AccountConfigService accountConfigService;
  protected PaymentSessionValidateService paymentSessionValidateService;
  protected MoveRepository moveRepository;
  protected JournalRepository journalRepository;
  protected MoveValidateService moveValidateService;
  protected AccountingBatchRepository accountingBatchRepository;
  protected InvoiceTermReplaceService invoiceTermReplaceService;
  protected InvoiceTermService invoiceTermService;
  protected ReconcileService reconcileService;
  protected BillOfExchangeInvoiceTermQueryService billOfExchangeInvoiceTermQueryService;

  @Inject
  public BatchBillOfExchangeInvoiceTerm(
      InvoiceTermRepository invoiceTermRepository,
      AppAccountService appAccountService,
      MoveCreateService moveCreateService,
      AccountConfigService accountConfigService,
      PaymentSessionValidateService paymentSessionValidateService,
      JournalRepository journalRepository,
      MoveRepository moveRepository,
      MoveValidateService moveValidateService,
      AccountingBatchRepository accountingBatchRepository,
      InvoiceTermReplaceService invoiceTermReplaceService,
      InvoiceTermService invoiceTermService,
      ReconcileService reconcileService,
      BillOfExchangeInvoiceTermQueryService billOfExchangeInvoiceTermQueryService) {
    super();
    this.invoiceTermRepository = invoiceTermRepository;
    this.appAccountService = appAccountService;
    this.moveCreateService = moveCreateService;
    this.accountConfigService = accountConfigService;
    this.paymentSessionValidateService = paymentSessionValidateService;
    this.journalRepository = journalRepository;
    this.moveRepository = moveRepository;
    this.moveValidateService = moveValidateService;
    this.accountingBatchRepository = accountingBatchRepository;
    this.invoiceTermReplaceService = invoiceTermReplaceService;
    this.invoiceTermService = invoiceTermService;
    this.reconcileService = reconcileService;
    this.billOfExchangeInvoiceTermQueryService = billOfExchangeInvoiceTermQueryService;
  }

  @Override
  protected void process() {
    AccountingBatch accountingBatch = batch.getAccountingBatch();

    if (accountingBatch.getPaymentMode() == null
        || !accountingBatch.getPaymentMode().getGenerateBankOrder()) {
      return;
    }

    List<Long> anomalyList = Lists.newArrayList(0L);
    Query<InvoiceTerm> query =
        billOfExchangeInvoiceTermQueryService.buildOrderedQueryFetchEligibleInvoiceTerms(
            accountingBatch, anomalyList);
    createLCRAccountingMovesForInvoiceTerms(query, anomalyList, accountingBatch);
  }

  protected void createLCRAccountingMovesForInvoiceTerms(
      Query<InvoiceTerm> query, List<Long> anomalyList, AccountingBatch accountingBatch) {
    List<InvoiceTerm> invoiceTermList = null;
    while (!(invoiceTermList = query.bind("anomalyList", anomalyList).fetch(getFetchLimit()))
        .isEmpty()) {
      accountingBatch = accountingBatchRepository.find(accountingBatch.getId());
      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        try {
          createMoveAndUpdateInvoiceTerm(accountingBatch, invoiceTerm);
          incrementDone();
        } catch (Exception e) {
          anomalyList.add(invoiceTerm.getId());
          incrementAnomaly();
          TraceBackService.trace(
              e, "billOfExchangeInvoiceTermBatch: create lcr accounting move", batch.getId());
          break;
        }
      }
      JPA.clear();
      findBatch();
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void createMoveAndUpdateInvoiceTerm(
      AccountingBatch accountingBatch, InvoiceTerm invoiceTerm) throws AxelorException {
    invoiceTerm = invoiceTermRepository.find(invoiceTerm.getId());

    BankDetails bankDetails =
        billOfExchangeInvoiceTermQueryService.getReceiverBankDetails(invoiceTerm);
    if (bankDetails == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(
              BankPaymentExceptionMessage
                  .BATCH_BILL_OF_EXCHANGE_BANK_DETAILS_IS_MISSING_ON_INVOICE_TERM),
          invoiceTerm.getName());
    }
    if (!bankDetails.getActive()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(
              BankPaymentExceptionMessage
                  .BATCH_BILL_OF_EXCHANGE_BANK_DETAILS_IS_INACTIVE_ON_INVOICE_TERM),
          bankDetails.getFullName(),
          invoiceTerm.getName(),
          invoiceTerm.getPartner().getPartnerSeq());
    }

    Company company = invoiceTerm.getMoveLine().getMove().getCompany();
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    BankDetails companyBankDetails = getCompanyBankDetails(invoiceTerm, accountingBatch, company);

    createLCRAccountMove(
        invoiceTerm, accountConfig, accountingBatch, company, bankDetails, companyBankDetails);
  }

  protected BankDetails getCompanyBankDetails(
      InvoiceTerm invoiceTerm, AccountingBatch accountingBatch, Company company) {
    BankDetails companyBankDetails;
    if (invoiceTerm.getInvoice() != null) {
      companyBankDetails = invoiceTerm.getInvoice().getCompanyBankDetails();
    } else {
      companyBankDetails = invoiceTerm.getMoveLine().getMove().getCompanyBankDetails();
    }

    if (companyBankDetails != null) {
      return companyBankDetails;
    }

    return accountingBatch.getBankDetails() != null
        ? accountingBatch.getBankDetails()
        : company.getDefaultBankDetails();
  }

  /**
   * The flag is written on the placement term(s) from {@code placementMoveLine}, not on {@code
   * invoiceTerm} itself: {@code replaceInvoiceTerms} below may already have detached/removed the
   * original term by this point, so writing to it here would touch a removed entity.
   */
  @Transactional(rollbackOn = {Exception.class})
  protected void createLCRAccountMove(
      InvoiceTerm invoiceTerm,
      AccountConfig accountConfig,
      AccountingBatch accountingBatch,
      Company company,
      BankDetails bankDetails,
      BankDetails companyBankDetails)
      throws AxelorException {
    log.debug("Creating lcr account move for invoice term {}", invoiceTerm);

    Move move =
        moveCreateService.createMove(
            journalRepository.find(accountingBatch.getBillOfExchangeJournal().getId()),
            company,
            invoiceTerm.getCurrency(),
            invoiceTerm.getPartner(),
            appAccountService.getTodayDate(company),
            null,
            accountingBatch.getPaymentMode(),
            invoiceTerm.getInvoice() != null ? invoiceTerm.getInvoice().getFiscalPosition() : null,
            bankDetails,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            invoiceTerm.getName(),
            null,
            companyBankDetails);

    MoveLine originMoveLine = invoiceTerm.getMoveLine();

    MoveLine clearingMoveLine =
        paymentSessionValidateService.generateMoveLine(
            move,
            invoiceTerm.getPartner(),
            originMoveLine.getAccount(),
            invoiceTerm.getAmountRemaining(),
            invoiceTerm.getName(),
            null,
            false);

    Account boeReceivAccount = accountConfigService.getBillOfExchReceivAccount(accountConfig);

    MoveLine placementMoveLine =
        paymentSessionValidateService.generateMoveLine(
            move,
            invoiceTerm.getPartner(),
            boeReceivAccount,
            invoiceTerm.getAmountRemaining(),
            invoiceTerm.getName(),
            null,
            true);

    move.addBatchSetItem(batchRepo.find(batch.getId()));
    move = moveRepository.save(move);
    moveValidateService.accounting(move);

    reconcileService.reconcile(originMoveLine, clearingMoveLine, null, false, false);

    invoiceTermService.payInvoiceTerms(List.of(invoiceTerm));
    invoiceTermService.payInvoiceTerms(clearingMoveLine.getInvoiceTermList());

    invoiceTerm.setPlacementMoveLine(placementMoveLine);
    invoiceTermReplaceService.replaceInvoiceTerms(
        invoiceTerm.getInvoice(),
        placementMoveLine.getInvoiceTermList(),
        List.of(invoiceTerm),
        null);

    Batch currentBatch = batchRepo.find(batch.getId());
    for (InvoiceTerm placementInvoiceTerm : placementMoveLine.getInvoiceTermList()) {
      // generateMoveLine defaults bankDetails/dueDate from the move, not the original term;
      // restore them here since later processing filters and displays on these fields.
      placementInvoiceTerm.setBankDetails(bankDetails);
      placementInvoiceTerm.setDueDate(invoiceTerm.getDueDate());
      placementInvoiceTerm.setLcrAccounted(true);
      placementInvoiceTerm.addBatchSetItem(currentBatch);
      invoiceTermRepository.save(placementInvoiceTerm);
    }

    log.debug("Created move {}", move);
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
