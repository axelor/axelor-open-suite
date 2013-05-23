package com.axelor.apps.account.service.invoice.generator.batch;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.administration.AbstractBatch;

public abstract class BatchStrategy extends AbstractBatch {

	protected InvoiceService invoiceService;

	
	protected BatchStrategy( InvoiceService invoiceService ) {

		super();
		this.invoiceService = invoiceService;
		
		
	}
	
	
	protected void updateInvoice( Invoice invoice ){
		
		if (invoice != null) {
			
			invoice.addBatchSetItem( Batch.find( batch.getId() ) );
			incrementDone();
			
		}
		
	}
	
}
