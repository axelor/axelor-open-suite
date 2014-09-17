/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
				
				invoiceService.ventilate( invoiceService.find(invoice.getId()) );
				updateInvoice( invoiceService.find(invoice.getId()) );

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
