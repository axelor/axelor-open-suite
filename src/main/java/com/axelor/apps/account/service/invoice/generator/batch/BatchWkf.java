/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
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
	 * @return Une liste de facture.
	 */
	protected static Collection<Invoice> invoices(InvoiceBatch invoiceBatch, boolean isTo) {

		if (invoiceBatch.getOnSelectOk()) { return invoiceBatch.getInvoiceSet(); } 
		else { return invoiceQuery(invoiceBatch, isTo); }

	}

	public static List<Invoice> invoiceQuery(InvoiceBatch invoiceBatch, boolean isTo) {
		
		if ( invoiceBatch != null ){
			
			List<Object> params = new ArrayList<Object>();
			
			String query = "self.company = ?1";
			params.add(invoiceBatch.getCompany());
	
			query += " AND self.status.code = ?2";
			if (isTo) { params.add(invoiceBatch.getToStatusSelect()); }
			else { params.add(invoiceBatch.getFromStatusSelect()); }
	
			LOG.debug("Query: {}", query);
			
			return Invoice.all().filter(query, params.toArray()).fetch();
			
		} else { return new ArrayList<Invoice>(); }

	}

}
