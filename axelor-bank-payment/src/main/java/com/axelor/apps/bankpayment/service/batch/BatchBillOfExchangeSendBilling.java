package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.NoteBills;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.NoteBillsRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.notebills.NoteBillsCreateService;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
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

  @Inject
  public BatchBillOfExchangeSendBilling(
      AppAccountService appAccountService,
      InvoiceRepository invoiceRepository,
      TemplateMessageService templateMessageService,
      NoteBillsCreateService noteBillsCreateService,
      NoteBillsRepository noteBillsRepository,
      CompanyRepository companyRepository,
      PartnerRepository partnerRepository,
      MessageService messageService) {
    this.appAccountService = appAccountService;
    this.invoiceRepository = invoiceRepository;
    this.templateMessageService = templateMessageService;
    this.noteBillsCreateService = noteBillsCreateService;
    this.noteBillsRepository = noteBillsRepository;
    this.companyRepository = companyRepository;
    this.partnerRepository = partnerRepository;
    this.messageService = messageService;
  }

  @Override
  protected void process() {
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
    Batch batch = batchRepo.find(this.batch.getId());
    Partner partner = partnerRepository.find(entry.getKey().getId());
    int counter = 0;

    for (Invoice invoice : entry.getValue()) {
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
      if (partner.getEmailAddress() == null
          || StringUtils.isEmpty(partner.getEmailAddress().getAddress())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            IExceptionMessage.BATCH_BOE_SEND_BILLING_PARTNER_ADRESS_MISSING,
            partner.getName());
      }
      NoteBills noteBills = noteBillsCreateService.createNoteBills(company, partner, batch);
      Message message =
          templateMessageService.generateAndSendMessage(
              noteBills, batch.getAccountingBatch().getBillOfExhangeMailTemplate());
      noteBills.setMessage(message);
      noteBills.setMetaFiles(messageService.getMetaAttachments(message));
      incrementDoneForInvoices(entry.getValue(), batch);
    } catch (Exception e) {
      incrementAnomaliesForInvoices(entry.getValue(), batch);
      TraceBackService.trace(
          e,
          String.format("Generation and send of message failed for %s", partner.getName()),
          batch.getId());
    }
  }

  protected void incrementAnomaliesForInvoices(List<Invoice> invoicesList, Batch batch) {
    invoicesList.stream()
        .forEach(
            invoice -> {
              incrementAnomaly();
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
    sb.append(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_REPORT))
        .append(" ");
    sb.append(
        String.format(
            I18n.get(
                    com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_DONE_SINGULAR,
                    com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_DONE_PLURAL,
                    batch.getDone())
                + " ",
            batch.getDone()));
    sb.append(
        String.format(
            I18n.get(
                com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_ANOMALY_SINGULAR,
                com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_ANOMALY_PLURAL,
                batch.getAnomaly()),
            batch.getAnomaly()));
    addComment(sb.toString());
    super.stop();
  }
}
