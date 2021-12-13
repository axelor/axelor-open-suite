package com.axelor.apps.bankpayment.service.batch;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineService;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BatchBillOfExchange extends AbstractBatch {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

 protected InvoiceRepository invoiceRepository;

 protected BankOrderLineService bankOrderLineService;

 protected BankOrderCreateService bankOrderCreateService;

 protected BankOrderRepository bankOrderRepository;
  
  @Inject
  public BatchBillOfExchange(InvoiceRepository invoiceRepository,
		  BankOrderLineService bankOrderLineService,
		  BankOrderCreateService bankOrderCreateService,
		  BankOrderRepository bankOrderRepository) {
	this.invoiceRepository = invoiceRepository;
	this.bankOrderLineService = bankOrderLineService;
	this.bankOrderCreateService = bankOrderCreateService;
	this.bankOrderRepository = bankOrderRepository;
}

  @Override
  @Transactional
  protected void process() {
    AccountingBatch accountingBatch = batch.getAccountingBatch();

    if (accountingBatch.getPaymentMode() == null
        || !accountingBatch.getPaymentMode().getGenerateBankOrder()) {
      return;
    }

    Query<Invoice> query = buildQueryFetchInvoices(accountingBatch);
    List<Invoice> invoicesList = null;
    BankOrder bankOrder = null;
    try {
      log.debug("Creating bank order");
      bankOrder =
          bankOrderCreateService.createBankOrder(
              accountingBatch.getPaymentMode(),
              BankOrderRepository.PARTNER_TYPE_CUSTOMER,
              accountingBatch.getDueDate(),
              accountingBatch.getCompany(),
              accountingBatch.getBankDetails() != null ? accountingBatch.getBankDetails() : accountingBatch.getCompany().getDefaultBankDetails(),
              accountingBatch.getCurrency(),
              null,
              null,
              BankOrderRepository.TECHNICAL_ORIGIN_AUTOMATIC);
      incrementDone();
    } catch (AxelorException e) {
      incrementAnomaly();
      TraceBackService.trace(e, null, batch.getId());
      return;
    }
    int offSet = 0;

    while (!(invoicesList = query.fetch(FETCH_LIMIT, offSet)).isEmpty()) {    	
      for (Invoice invoice : invoicesList) {
        try {
          log.debug("Creating bank order line from {}", invoice);
          bankOrder.addBankOrderLineListItem(
              createBankOrderLineFromInvoice(accountingBatch, invoice));
          incrementDone();
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(e, null, batch.getId());
        }
      }

      offSet += FETCH_LIMIT;
      JPA.clear();
      findBatch();
    }

    bankOrderRepository.save(bankOrder);
  }

  @Transactional
  protected BankOrderLine createBankOrderLineFromInvoice(
      AccountingBatch accountingBatch, Invoice invoice) throws AxelorException {

    return bankOrderLineService.createBankOrderLine(
        accountingBatch.getPaymentMode().getBankOrderFileFormat(),
        invoice.getPartner(),
        invoice.getAmountRemaining(),
        invoice.getCurrency(),
        invoice.getInvoiceDate(),
        null,
        null,
        invoice);
  }

  protected Query<Invoice> buildQueryFetchInvoices(AccountingBatch accountingBatch) {
    StringBuilder filter = new StringBuilder();
    filter.append(
        "self.operationTypeSelect = :operationTypeSelect "
            + "AND self.statusSelect = :statusSelect "
            + "AND self.amountRemaining > 0 "
            + "AND self.company = :company "
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
