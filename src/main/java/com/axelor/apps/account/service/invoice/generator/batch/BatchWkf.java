package com.axelor.apps.account.service.invoice.generator.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceBatch;
import com.axelor.apps.account.service.invoice.InvoiceService;

public abstract class BatchWkf extends BatchStrategy {

	static final Logger LOG = LoggerFactory.getLogger(BatchWkf.class);

	protected BatchWkf(InvoiceService invoiceService) {
		
		super(invoiceService);
		
	}

	/**
	 * Récupérer la liste des factures à traiter.
	 * 
	 * @param invoiceBatch
	 *            Le batch de facturation concerné.
	 * 
	 * @return Une liste de contrat.
	 */
	protected static Collection<Invoice> invoices(InvoiceBatch invoiceBatch, boolean isTo) {

		if (invoiceBatch.getOnSelectOk()) { return invoiceBatch.getInvoiceSet(); } 
		else { return invoiceQuery(invoiceBatch, isTo); }

	}

	public static List<Invoice> invoiceQuery(InvoiceBatch invoiceBatch, boolean isTo) {
		
		if ( invoiceBatch != null ){
			
			List<Object> params = new ArrayList<Object>();
			
			String query = "self.invoiceTypeSelect != 10 AND self.company = ?1";
			params.add(invoiceBatch.getCompany());
	
			query += " AND self.status.code = ?2";
			if (isTo) { params.add(invoiceBatch.getToStatusSelect()); }
			else { params.add(invoiceBatch.getFromStatusSelect()); }
	
			int i = 3;
			
			LOG.debug("Query: {}", query);
			
			return Invoice.all().filter(query, params.toArray()).fetch();
			
		} else { return new ArrayList<Invoice>(); }

	}

}
