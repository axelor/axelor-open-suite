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

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.NoteBills;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.notebills.NoteBillsCreateService;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
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
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BatchBillOfExchangeSendBillingInvoiceTerm extends AbstractBatch {

  protected InvoiceTermRepository invoiceTermRepository;
  protected TemplateMessageService templateMessageService;
  protected NoteBillsCreateService noteBillsCreateService;
  protected CompanyRepository companyRepository;
  protected PartnerRepository partnerRepository;
  protected MessageService messageService;
  protected SequenceService sequenceService;
  protected BillOfExchangeInvoiceTermQueryService billOfExchangeInvoiceTermQueryService;
  private boolean end = false;

  @Inject
  public BatchBillOfExchangeSendBillingInvoiceTerm(
      InvoiceTermRepository invoiceTermRepository,
      TemplateMessageService templateMessageService,
      NoteBillsCreateService noteBillsCreateService,
      CompanyRepository companyRepository,
      PartnerRepository partnerRepository,
      MessageService messageService,
      SequenceService sequenceService,
      BillOfExchangeInvoiceTermQueryService billOfExchangeInvoiceTermQueryService) {
    this.invoiceTermRepository = invoiceTermRepository;
    this.templateMessageService = templateMessageService;
    this.noteBillsCreateService = noteBillsCreateService;
    this.companyRepository = companyRepository;
    this.partnerRepository = partnerRepository;
    this.messageService = messageService;
    this.sequenceService = sequenceService;
    this.billOfExchangeInvoiceTermQueryService = billOfExchangeInvoiceTermQueryService;
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
          "Batch bill of exchange send billing invoice term",
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
    Map<Partner, List<InvoiceTerm>> mapPartnerInvoiceTerms = new HashMap<>();
    List<InvoiceTerm> invoiceTermsList = null;

    Query<InvoiceTerm> query =
        billOfExchangeInvoiceTermQueryService.buildOrderedQueryFetchLcrAccountedInvoiceTerms(
            accountingBatch);

    int offSet = 0;
    while (!(invoiceTermsList = query.fetch(getFetchLimit(), offSet)).isEmpty()) {
      sortInvoiceTermsPerPartner(invoiceTermsList, mapPartnerInvoiceTerms);
      offSet += getFetchLimit();
      JPA.clear();
      findBatch();
    }

    try {
      mapPartnerInvoiceTerms
          .entrySet()
          .forEach(
              entry -> {
                generateNoteBillsAndSend(entry);
                JPA.clear();
              });
    } catch (Exception e) {
      incrementAnomaly();
      TraceBackService.trace(e, "Generation of note bills error", batch.getId());
    }
  }

  @Transactional
  protected void generateNoteBillsAndSend(Entry<Partner, List<InvoiceTerm>> entry) {
    Company company = null;
    Partner partner = partnerRepository.find(entry.getKey().getId());

    for (InvoiceTerm invoiceTerm : entry.getValue()) {
      batch = batchRepo.find(this.batch.getId());
      if (company == null) {
        company = companyRepository.find(batch.getAccountingBatch().getCompany().getId());
      }
      addBatchSet(batch, invoiceTerm);
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
      incrementDoneForInvoiceTerms(entry.getValue());
    } catch (Exception e) {
      incrementAnomaliesForInvoiceTerms(e, entry.getValue());
      TraceBackService.trace(
          e,
          String.format("Generation and send of message failed for %s", partner.getName()),
          batch.getId());
    }
  }

  protected void incrementAnomaliesForInvoiceTerms(Exception e, List<InvoiceTerm> invoiceTermList) {
    invoiceTermList.forEach(
        invoiceTerm -> {
          incrementAnomaly();
          TraceBackService.trace(e, "Generation and send of message failed", batch.getId());
        });
  }

  protected void incrementDoneForInvoiceTerms(List<InvoiceTerm> invoiceTermList) {
    invoiceTermList.forEach(invoiceTerm -> incrementDone());
  }

  @Transactional
  protected void addBatchSet(Batch batch, InvoiceTerm invoiceTerm) {
    try {
      InvoiceTerm invoiceTermToSave = invoiceTermRepository.find(invoiceTerm.getId());
      invoiceTermToSave.addBatchSetItem(batch);
      invoiceTermRepository.save(invoiceTermToSave);
    } catch (Exception e) {
      incrementAnomaly();
      TraceBackService.trace(
          e,
          "billOfExchangeInvoiceTermBatch: adding batch in invoice term batchSetItem",
          batch.getId());
    }
  }

  protected void sortInvoiceTermsPerPartner(
      List<InvoiceTerm> invoiceTermsList, Map<Partner, List<InvoiceTerm>> mapPartnerInvoiceTerms) {
    invoiceTermsList.forEach(
        invoiceTerm ->
            mapPartnerInvoiceTerms
                .computeIfAbsent(invoiceTerm.getPartner(), key -> new ArrayList<>())
                .add(invoiceTerm));
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

  @Override
  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_BANK_PAYMENT_BATCH);
  }
}
