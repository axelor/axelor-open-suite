package com.axelor.apps.account.service.invoice.generator.batch;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;

public class BatchVentilation extends BatchWkf {

	static final Logger LOG = LoggerFactory.getLogger(BatchVentilation.class);

	@Inject
	public BatchVentilation(InvoiceService invoiceService) {

		super(invoiceService);
		
	}

	@Override
	protected void process() {
		
		for (Invoice invoice : invoices(batch.getInvoiceBatch(), true)) {

			try {
				
				invoiceService.ventilate( Invoice.find(invoice.getId()) );
				updateInvoice( Invoice.find(invoice.getId()) );

			} catch (AxelorException e) {

				TraceBackService.trace(new AxelorException(String.format("Facture %s", invoice.getInvoiceId()), e, e.getcategory()), IException.INVOICE_ORIGIN, batch.getId());
				incrementAnomaly();

			} catch (Exception e) {

				TraceBackService.trace(new Exception(String.format("Facture %s", invoice.getInvoiceId()), e), IException.INVOICE_ORIGIN, batch.getId());
				incrementAnomaly();

			} finally {
				
				JPA.clear();
				
			}

		}
		
	}

	@Override
	protected void stop() {

		String comment = "Compte rendu de la ventilation de facture :\n";
		comment += String.format("\t* %s facture(s) ventilée(s)\n", batch.getDone() );
		comment += String.format("\t* %s anomalie(s)", batch.getAnomaly() );
		
		super.stop();
		addComment(comment);
		
	}	

}
