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
package com.axelor.apps.supplychain.service.batch;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.supplychain.db.ISalesOrder;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.service.SalesOrderInvoiceService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;

public class BatchInvoicing extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchInvoicing.class);

	
	@Inject
	public BatchInvoicing(SalesOrderInvoiceService salesOrderInvoiceService) {
		
		super(salesOrderInvoiceService);
	}


	@Override
	protected void process() {
		
		int i = 0;
		List<SalesOrder> salesOrderList = SalesOrder.all().filter("self.invoicingTypeSelect = ?1 AND self.statusSelect = ?2 AND self.company = ?3", 
				ISalesOrder.INVOICING_TYPE_SUBSCRIPTION, ISalesOrder.STATUS_VALIDATED, batch.getSupplychainBatch().getCompany()).fetch();

		for (SalesOrder salesOrder : salesOrderList) {

			try {
				
				salesOrderInvoiceService.checkSubscriptionSalesOrder(SalesOrder.find(salesOrder.getId()));
				
				Invoice invoice = salesOrderInvoiceService.runSubscriptionInvoicing(SalesOrder.find(salesOrder.getId()));
				
				if(invoice != null)  {  
					
					updateSalesOrder(salesOrder); 
					LOG.debug("Facture créée ({}) pour le devis {}", invoice.getInvoiceId(), salesOrder.getSalesOrderSeq());	
					i++; 
					
				}

			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format("Devis %s", SalesOrder.find(salesOrder.getId()).getSalesOrderSeq()), e, e.getcategory()), IException.INVOICE_ORIGIN, batch.getId());
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Devis %s", SalesOrder.find(salesOrder.getId()).getSalesOrderSeq()), e), IException.INVOICE_ORIGIN, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour le devis {}", SalesOrder.find(salesOrder.getId()).getSalesOrderSeq());
				
			} finally {
				
				if (i % 10 == 0) { JPA.clear(); }
	
			}

		}
		
		
	}
	
	
	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {

		String comment = "Compte rendu de génération de facture d'abonnement :\n";
		comment += String.format("\t* %s Devis(s) traité(s)\n", batch.getDone());
		comment += String.format("\t* %s anomalie(s)", batch.getAnomaly());
		
		super.stop();
		addComment(comment);
		
	}

}
