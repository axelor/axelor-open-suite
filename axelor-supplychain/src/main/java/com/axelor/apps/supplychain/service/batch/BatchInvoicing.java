/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.batch;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;

public class BatchInvoicing extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchInvoicing.class);
	
	@Inject
	private SaleOrderRepository saleOrderRepo;
	
	@Inject
	public BatchInvoicing(SaleOrderInvoiceService saleOrderInvoiceService) {
		
		super(saleOrderInvoiceService);
	}


	@Override
	protected void process() {
		
		int i = 0;
		List<SaleOrder> saleOrderList = (List<SaleOrder>) saleOrderRepo.all().filter("self.invoicingTypeSelect = ?1 AND self.statusSelect = ?2 AND self.company = ?3", 
				ISaleOrder.INVOICING_TYPE_SUBSCRIPTION, ISaleOrder.STATUS_VALIDATED, batch.getSaleBatch().getCompany()).fetch();

		for (SaleOrder saleOrder : saleOrderList) {

			try {
				
				saleOrderInvoiceService.checkSubscriptionSaleOrder(saleOrderRepo.find(saleOrder.getId()));
				
				Invoice invoice = saleOrderInvoiceService.runSubscriptionInvoicing(saleOrderRepo.find(saleOrder.getId()));
				
				if(invoice != null)  {  
					
					updateSaleOrder(saleOrder); 
					LOG.debug("Facture créée ({}) pour le devis {}", invoice.getInvoiceId(), saleOrder.getSaleOrderSeq());	
					i++; 
					
				}

			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format("Devis %s", saleOrderRepo.find(saleOrder.getId()).getSaleOrderSeq()), e, e.getcategory()), IException.INVOICE_ORIGIN, batch.getId());
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Devis %s", saleOrderRepo.find(saleOrder.getId()).getSaleOrderSeq()), e), IException.INVOICE_ORIGIN, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour le devis {}", saleOrderRepo.find(saleOrder.getId()).getSaleOrderSeq());
				
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
