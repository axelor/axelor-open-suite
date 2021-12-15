package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchBillOfExchange extends AbstractBatch {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected InvoiceRepository invoiceRepository;
  protected InvoicePaymentCreateService invoicePaymentCreateService;
  protected BankOrderMergeService bankOrderMergeService;
  protected AppAccountService appAccountService;
  protected BankDetailsRepository bankDetailsRepository;
  protected InvoicePaymentRepository invoicePaymentRepository;

  @Inject
  public BatchBillOfExchange(
      InvoiceRepository invoiceRepository,
      InvoicePaymentCreateService invoicePaymentCreateService,
      BankOrderMergeService bankOrderMergeService,
      AppAccountService appAccountService,
      BankDetailsRepository bankDetailsRepository,
      InvoicePaymentRepository invoicePaymentRepository) {
    super();
    this.invoiceRepository = invoiceRepository;
    this.invoicePaymentCreateService = invoicePaymentCreateService;
    this.bankOrderMergeService = bankOrderMergeService;
    this.appAccountService = appAccountService;
    this.bankDetailsRepository = bankDetailsRepository;
    this.invoicePaymentRepository = invoicePaymentRepository;
  }

  @Override
  protected void process() {
    AccountingBatch accountingBatch = batch.getAccountingBatch();

    if (accountingBatch.getPaymentMode() == null
        || !accountingBatch.getPaymentMode().getGenerateBankOrder()) {
      return;
    }

    Query<Invoice> query = buildOrderedQueryFetchInvoices(accountingBatch);
    List<Long> invoicePaymentIdList = createInvoicePayments(query);
    if (invoicePaymentIdList != null && !invoicePaymentIdList.isEmpty()) {

      try {
        bankOrderMergeService.mergeFromInvoicePayments(
            invoicePaymentIdList.stream()
                .map(id -> invoicePaymentRepository.find(id))
                .collect(Collectors.toList()));
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(e, "billOfExchangeBatch: Merge", batch.getId());
      }
    }
  }

  protected BankDetails getAccountingBankDetails(AccountingBatch accountingBatch) {
    return accountingBatch.getBankDetails() != null
        ? accountingBatch.getBankDetails()
        : accountingBatch.getCompany().getDefaultBankDetails();
  }

  protected List<Long> createInvoicePayments(Query<Invoice> query) {
    int offSet = 0;
    List<Invoice> invoicesList = null;
    List<Long> invoicePaymentIdList = new ArrayList<>();
    AccountingBatch accountingBatch = batch.getAccountingBatch();
    BankDetails companyBankDetails = getAccountingBankDetails(accountingBatch);
    while (!(invoicesList = query.fetch(FETCH_LIMIT, offSet)).isEmpty()) {
      if (!JPA.em().contains(companyBankDetails)) {
        companyBankDetails = bankDetailsRepository.find(companyBankDetails.getId());
      }
      for (Invoice invoice : invoicesList) {
        try {
          createInvoicePayment(invoicePaymentIdList, companyBankDetails, invoice);
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(e, "billOfExchangeBatch: create invoice payment", batch.getId());
        }
      }

      offSet += FETCH_LIMIT;
      JPA.clear();
    }
    return invoicePaymentIdList;
  }

  @Transactional
  protected void createInvoicePayment(
      List<Long> invoicePaymentIdList, BankDetails companyBankDetails, Invoice invoice) {
    log.debug("Creating Invoice payments from {}", invoice);
    invoiceRepository.find(invoice.getId());
    invoicePaymentIdList.add(
        invoicePaymentCreateService.createInvoicePayment(invoice, companyBankDetails).getId());
    incrementDone();
  }

  protected Query<Invoice> buildOrderedQueryFetchInvoices(AccountingBatch accountingBatch) {
    StringBuilder filter = new StringBuilder();
    boolean manageMultiBanks = appAccountService.getAppBase().getManageMultiBanks();
    filter.append(
        "self.operationTypeSelect = :operationTypeSelect "
            + "AND self.statusSelect = :statusSelect "
            + "AND self.amountRemaining > 0 "
            + "AND self.company = :company "
            + "AND self.hasPendingPayments = FALSE "
            + "AND self.paymentMode = :paymentMode ");

    Map<String, Object> bindings = new HashMap<>();
    bindings.put("operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE);
    bindings.put("statusSelect", InvoiceRepository.STATUS_VENTILATED);
    bindings.put("company", accountingBatch.getCompany());
    bindings.put("paymentMode", accountingBatch.getPaymentMode());

    if (accountingBatch.getDueDate() != null) {
      filter.append("AND self.dueDate <= :dueDate ");
      bindings.put("dueDate", accountingBatch.getDueDate());
    }
    if (accountingBatch.getCurrency() != null) {
      filter.append("AND self.currency = :currency ");
      bindings.put("currency", accountingBatch.getCurrency());
    }

    if (manageMultiBanks) {
      filter.append(" AND self.companyBankDetails IN (:bankDetailsSet) ");
      Set<BankDetails> bankDetailsSet = Sets.newHashSet(accountingBatch.getBankDetails());
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
