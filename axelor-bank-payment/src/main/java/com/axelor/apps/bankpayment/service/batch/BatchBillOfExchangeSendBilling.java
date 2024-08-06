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
import com.axelor.apps.account.db.NoteBills;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.NoteBillsRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.notebills.NoteBillsCreateService;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Message;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class BatchBillOfExchangeSendBilling extends AbstractBatch {

  protected AppAccountService appAccountService;
  protected InvoiceRepository invoiceRepository;
  protected TemplateMessageService templateMessageService;
  protected NoteBillsCreateService noteBillsCreateService;
  protected NoteBillsRepository noteBillsRepository;
  protected CompanyRepository companyRepository;
  protected PartnerRepository partnerRepository;
  protected MessageService messageService;
  protected SequenceService sequenceService;
  private boolean end = false;

  @Inject
  public BatchBillOfExchangeSendBilling(
      AppAccountService appAccountService,
      InvoiceRepository invoiceRepository,
      TemplateMessageService templateMessageService,
      NoteBillsCreateService noteBillsCreateService,
      NoteBillsRepository noteBillsRepository,
      CompanyRepository companyRepository,
      PartnerRepository partnerRepository,
      MessageService messageService,
      SequenceService sequenceService) {
    this.appAccountService = appAccountService;
    this.invoiceRepository = invoiceRepository;
    this.templateMessageService = templateMessageService;
    this.noteBillsCreateService = noteBillsCreateService;
    this.noteBillsRepository = noteBillsRepository;
    this.companyRepository = companyRepository;
    this.partnerRepository = partnerRepository;
    this.messageService = messageService;
    this.sequenceService = sequenceService;
  }

  @Override
  protected void start() throws IllegalAccessException {
    super.start();
    if (!sequenceService.hasSequence(
        SequenceRepository.NOTE_BILLS, batch.getAccountingBatch().getCompany())) {
      TraceBackService.trace(
          new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.NOTE_BILLS_CONFIG_SEQUENCE),
              I18n.get(BaseExceptionMessage.EXCEPTION),
              batch.getAccountingBatch().getCompany().getName()),
          "Batch bill of exchange send billing",
          batch.getId());
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
    Map<Partner, List<Invoice>> mapPartnerInvoices = new HashMap<>();
    List<Invoice> invoicesList = null;

    List<Long> anomalyList = Lists.newArrayList(0L); // Can't pass an empty collection to the query
    Query<Invoice> query = buildOrderedQueryFetchLcrAccountedInvoices(accountingBatch, anomalyList);

    int offSet = 0;
    while (!(invoicesList = query.fetch(FETCH_LIMIT, offSet)).isEmpty()) {
      sortInvoicesPerPartner(invoicesList, mapPartnerInvoices);
      offSet += FETCH_LIMIT;
      JPA.clear();
    }

    try {
      mapPartnerInvoices
          .entrySet()
          .forEach(
              partner -> {
                generateNoteBillsAndSend(partner);
                JPA.clear();
              });
    } catch (Exception e) {
      incrementAnomaly();
      TraceBackService.trace(e, "Generation of note bills error", batch.getId());
    }
  }

  @Transactional
  protected void generateNoteBillsAndSend(Entry<Partner, List<Invoice>> entry) {
    Objects.requireNonNull(entry);
    Company company = null;
    Partner partner = partnerRepository.find(entry.getKey().getId());
    int counter = 0;

    for (Invoice invoice : entry.getValue()) {
      batch = batchRepo.find(this.batch.getId());
      if (company == null) {
        company = companyRepository.find(batch.getAccountingBatch().getCompany().getId());
      }
      addBatchSet(batch, invoice);
      counter++;
      if (counter % FETCH_LIMIT == 0) {
        JPA.clear();
      }
    }

    try {
      batch = batchRepo.find(this.batch.getId());
      company = companyRepository.find(batch.getAccountingBatch().getCompany().getId());
      partner = partnerRepository.find(entry.getKey().getId());
      if (partner.getEmailAddress() == null
          || StringUtils.isEmpty(partner.getEmailAddress().getAddress())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            BankPaymentExceptionMessage.BATCH_BOE_SEND_BILLING_PARTNER_ADRESS_MISSING,
            partner.getName());
      }
      NoteBills noteBills =
          noteBillsCreateService.createNoteBills(
              company, partner, batchRepo.find(this.batch.getId()));
      Message message =
          templateMessageService.generateAndSendMessage(
              noteBills, batch.getAccountingBatch().getBillOfExhangeMailTemplate());
      noteBills.setMessage(message);
      noteBills.setMetaFiles(messageService.getMetaAttachments(message));
      incrementDoneForInvoices(entry.getValue(), batch);
    } catch (Exception e) {
      incrementAnomaliesForInvoices(e, entry.getValue(), batch);
      TraceBackService.trace(
          e,
          String.format("Generation and send of message failed for %s", partner.getName()),
          batch.getId());
    }
  }

  protected void incrementAnomaliesForInvoices(
      Exception e, List<Invoice> invoicesList, Batch batch) {
    invoicesList.stream()
        .forEach(
            invoice -> {
              incrementAnomaly();
              TraceBackService.trace(e, "Generation and send of message failed", batch.getId());
            });
    ;
  }

  protected void incrementDoneForInvoices(List<Invoice> invoicesList, Batch batch) {
    invoicesList.stream()
        .forEach(
            invoice -> {
              incrementDone();
            });
    ;
  }

  @Transactional
  protected void addBatchSet(Batch batch, Invoice invoice) {
    Objects.requireNonNull(batch);
    Objects.requireNonNull(invoice);
    try {
      Invoice invoiceToSave = invoiceRepository.find(invoice.getId());
      invoiceToSave.addBatchSetItem(batch);
      invoiceRepository.save(invoiceToSave);
    } catch (Exception e) {
      incrementAnomaly();
      TraceBackService.trace(
          e, "batchBillOfExchange: adding batch in invoice batchSetItem", batch.getId());
    }
  }

  protected void sortInvoicesPerPartner(
      List<Invoice> invoicesList, Map<Partner, List<Invoice>> mapPartnerInvoices) {
    Objects.requireNonNull(invoicesList);
    Objects.requireNonNull(mapPartnerInvoices);

    invoicesList.forEach(
        invoice -> {
          if (!mapPartnerInvoices.containsKey(invoice.getPartner())) {
            mapPartnerInvoices.put(
                invoice.getPartner(), new ArrayList<Invoice>(Arrays.asList(invoice)));
          } else {
            mapPartnerInvoices.get(invoice.getPartner()).add(invoice);
          }
        });
  }

  protected void traceAnomalyForEachInvoices(List<Long> invoiceIdList, Exception exception) {

    invoiceIdList.forEach(
        invoiceId -> {
          incrementAnomaly();
          TraceBackService.trace(exception, "batchBillOfExchange: send billing", batch.getId());
        });
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
