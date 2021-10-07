package com.axelor.apps.cash.management.service;

import java.time.LocalDate;

import org.apache.commons.collections.CollectionUtils;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;

public class InvoiceEstimatedPaymentServiceImpl implements InvoiceEstimatedPaymentService {

  @Override
  public Invoice computeEstimatedPaymentDate(Invoice invoice) {
	  
	  LocalDate estimatedPaymentDate = invoice.getDueDate();
	  if (estimatedPaymentDate != null
			  && invoice.getPartner() != null
			  && invoice.getPartner().getPaymentDelay() != null) {
		  estimatedPaymentDate =
				  estimatedPaymentDate.plusDays(invoice.getPartner().getPaymentDelay().intValue());
	  }
	  if (!CollectionUtils.isEmpty(invoice.getInvoiceTermList())) {
		  for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
			  LocalDate invoiceTermEstimatedPaymentDate =
					  computeEstimatedPaymentDate(invoiceTerm,invoice);
			  invoiceTerm.setEstimatedPaymentDate(invoiceTermEstimatedPaymentDate);
			  if(invoiceTermEstimatedPaymentDate.isBefore(estimatedPaymentDate)) {
				  estimatedPaymentDate = invoiceTermEstimatedPaymentDate;
			  }
		  }
	  }
	  invoice.setEstimatedPaymentDate(estimatedPaymentDate);
	  return invoice;
  }
  
  @Override
  public LocalDate computeEstimatedPaymentDate(InvoiceTerm invoiceTerm,Invoice invoice) {
	    LocalDate estimatedPaymentDate = invoiceTerm.getDueDate();
	    if (estimatedPaymentDate != null
	        && invoice.getPartner() != null
	        && invoice.getPartner().getPaymentDelay() != null) {
	      estimatedPaymentDate =
	          estimatedPaymentDate.plusDays(invoice.getPartner().getPaymentDelay().intValue());
	    }
	    return estimatedPaymentDate;
	  }
}
