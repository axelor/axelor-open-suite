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
package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.bankpayment.db.BankPaymentConfig;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.bankpayment.service.config.BankPaymentConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatch;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchBankOrderGenerationBillOfExchange extends AbstractBatch {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected InvoiceRepository invoiceRepository;
  protected InvoicePaymentCreateService invoicePaymentCreateService;
  protected BankOrderMergeService bankOrderMergeService;
  protected AppAccountService appAccountService;
  protected BankDetailsRepository bankDetailsRepository;
  protected InvoicePaymentRepository invoicePaymentRepository;
  protected MoveValidateService moveValidateService;
  protected BankPaymentConfigService bankPaymentConfigService;
  protected MoveToolService moveToolService;
  private boolean end = false;

  @Inject
  public BatchBankOrderGenerationBillOfExchange(
      InvoiceRepository invoiceRepository,
      InvoicePaymentCreateService invoicePaymentCreateService,
      BankOrderMergeService bankOrderMergeService,
      AppAccountService appAccountService,
      BankDetailsRepository bankDetailsRepository,
      InvoicePaymentRepository invoicePaymentRepository,
      MoveValidateService moveValidateService,
      BankPaymentConfigService bankPaymentConfigService,
      MoveToolService moveToolService) {
    super();
    this.invoiceRepository = invoiceRepository;
    this.invoicePaymentCreateService = invoicePaymentCreateService;
    this.bankOrderMergeService = bankOrderMergeService;
    this.appAccountService = appAccountService;
    this.bankDetailsRepository = bankDetailsRepository;
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.moveValidateService = moveValidateService;
    this.bankPaymentConfigService = bankPaymentConfigService;
    this.moveToolService = moveToolService;
  }

  @Override
  protected void start() throws IllegalAccessException {
    super.start();
    try {
      BankPaymentConfig bankPaymentConfig =
          bankPaymentConfigService.getBankPaymentConfig(batch.getAccountingBatch().getCompany());
      if (bankPaymentConfig.getBillOfExchangeSequence() == null) {
        throw new AxelorException(
            bankPaymentConfig,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BankPaymentExceptionMessage.ACCOUNT_CONFIG_SEQUENCE_12),
            I18n.get(BaseExceptionMessage.EXCEPTION),
            bankPaymentConfig.getCompany().getName());
      }
    } catch (Exception e) {
      TraceBackService.trace(e, "Batch bill of exchange bank order generation", batch.getId());
      incrementAnomaly();
      end = true;
    }
  }

  @Override
  protected void process() {
    if (end) {
      return;
    }
    AccountingBatch accountingBatch = batch.getAccountingBatch();

    if (accountingBatch.getPaymentMode() == null
        || !accountingBatch.getPaymentMode().getGenerateBankOrder()) {
      return;
    }

    List<Long> anomalyList = Lists.newArrayList(0L); // Can't pass an empty collection to the query
    Query<Invoice> query = buildOrderedQueryFetchLcrAccountedInvoices(accountingBatch, anomalyList);
    try {
      List<Long> invoicePaymentIdList = createInvoicePayments(query, anomalyList);
      if (invoicePaymentIdList != null && !invoicePaymentIdList.isEmpty()) {

        bankOrderMergeService.mergeFromInvoicePayments(
            invoicePaymentIdList.stream()
                .map(id -> invoicePaymentRepository.find(id))
                .collect(Collectors.toList()),
            accountingBatch.getDueDate());
      }
    } catch (Exception e) {
      incrementAnomaly();
      TraceBackService.trace(e, "billOfExchangeBatch: Merge", batch.getId());
    }
  }

  protected BankDetails getAccountingBankDetails(AccountingBatch accountingBatch) {
    return accountingBatch.getBankDetails() != null
        ? accountingBatch.getBankDetails()
        : accountingBatch.getCompany().getDefaultBankDetails();
  }

  protected List<Long> createInvoicePayments(Query<Invoice> query, List<Long> anomalyList) {
    List<Invoice> invoicesList = null;
    List<Long> invoicePaymentIdList = new ArrayList<>();
    AccountingBatch accountingBatch = batch.getAccountingBatch();
    BankDetails companyBankDetails = getAccountingBankDetails(accountingBatch);
    while (!(invoicesList = query.fetch(FETCH_LIMIT)).isEmpty()) {
      if (!JPA.em().contains(companyBankDetails)) {
        companyBankDetails = bankDetailsRepository.find(companyBankDetails.getId());
      }
      for (Invoice invoice : invoicesList) {
        try {
          createInvoicePayment(invoicePaymentIdList, companyBankDetails, invoice, null);
        } catch (Exception e) {
          incrementAnomaly();
          anomalyList.add(invoice.getId());
          query.bind("anomalyList", anomalyList);
          TraceBackService.trace(e, "billOfExchangeBatch: create invoice payment", batch.getId());
          break;
        }
      }
      JPA.clear();
    }
    return invoicePaymentIdList;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createInvoicePayment(
      List<Long> invoicePaymentIdList,
      BankDetails companyBankDetails,
      Invoice invoice,
      LocalDate paymentDate)
      throws AxelorException {
    log.debug("Creating Invoice payments from {}", invoice);
    invoiceRepository.find(invoice.getId());
    invoice.setCompanyInTaxTotalRemaining(moveToolService.getInTaxTotalRemaining(invoice));
    invoicePaymentIdList.add(
        invoicePaymentCreateService
            .createInvoicePayment(invoice, companyBankDetails, paymentDate)
            .getId());
    invoice.addBatchSetItem(batchRepo.find(batch.getId()));

    incrementDone();
  }

  protected Query<Invoice> buildOrderedQueryFetchLcrAccountedInvoices(
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
            + "AND self.lcrAccounted = TRUE "
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

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_BANK_PAYMENT_BATCH);
  }
}
