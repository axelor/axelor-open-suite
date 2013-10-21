/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.service.invoice.generator.invoice;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.base.db.Status;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;

public class RefundInvoice extends InvoiceGenerator implements InvoiceStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(RefundInvoice.class);
	private Invoice invoice;
	
	public RefundInvoice(Invoice invoice) {
		
		super();

		this.invoice = invoice;
		
	}

	@Override
	public Invoice generate() throws AxelorException {
		
		LOG.debug("Créer un avoir pour la facture {}", new Object[] { invoice.getInvoiceId() });
		
		Invoice refund = JPA.copy(invoice, true);
		
		refund.setOperationTypeSelect(this.inverseOperationType(refund.getOperationTypeSelect()));
		
		List<InvoiceLine> refundLines = new ArrayList<InvoiceLine>();
		if(refund.getInvoiceLineList() != null)  {
			refundLines.addAll( refund.getInvoiceLineList() );
		}
		
		populate( refund, refundLines );
		refund.setMove(null);
		
		refund.setStatus(Status.all().filter("self.code = 'dra'").fetchOne());
		
		return refund;
		
	}
	
	@Override
	public void populate(Invoice invoice, List<InvoiceLine> invoiceLines) throws AxelorException {
		
		super.populate(invoice, invoiceLines);
	}
	
	
	/**
	 * Mets à jour les lignes de facture en appliquant la négation aux prix unitaires et
	 * au total hors taxe.
	 * 
	 * @param invoiceLines
	 */
	@Deprecated
	protected void refundInvoiceLines(List<InvoiceLine> invoiceLines){
		
		for (InvoiceLine invoiceLine : invoiceLines){
			
			invoiceLine.setQty(invoiceLine.getQty().negate());
			invoiceLine.setExTaxTotal(invoiceLine.getExTaxTotal().negate());
			
		}
		
	}

}
