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
package com.axelor.apps.account.service.invoice.generator.tax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.InvoiceLineTaxHistory;
import com.axelor.apps.account.service.invoice.generator.TaxGenerator;
import com.axelor.exception.AxelorException;

/**
 * InvoiceLineTaxService est une classe implémentant l'ensemble des services
 * pour les lignes de taxes des factures.
 */
public class TaxLine extends TaxGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(TaxLine.class);

	private List<InvoiceLineTaxHistory> invoiceLineTaxHistories;
	
	public TaxLine(Invoice invoice, List<InvoiceLine> invoiceLines) {
		
		super(invoice, invoiceLines);
		invoiceLineTaxHistories = new ArrayList<InvoiceLineTaxHistory>();
		
	}
	
	public List<InvoiceLineTaxHistory> getInvoiceLineTaxHistories(){
		return this.invoiceLineTaxHistories;
	}

	@Override
	public List<InvoiceLineTax> creates() throws AxelorException {
		
		LOG.debug("Création des ligne de taxes => lignes de facture: {}, ajustement: {}",
				new Object[] { invoiceLines.size() });
			
		List<InvoiceLineTax> invoiceLineTaxes = new ArrayList<InvoiceLineTax>();
		Map<List<Object>, InvoiceLineTax> map = new HashMap<List<Object>, InvoiceLineTax>();
		List<Object> keys = new ArrayList<Object>();
		
		this.invoiceLineTaxHistories.clear();
//		this.invoiceLineTaxHistories.addAll((new TaxHistoryLine(invoice, invoiceLines)).creates());
		
		for (InvoiceLineTaxHistory invoiceLineTaxTransition : this.invoiceLineTaxHistories) {
			
			keys.clear();
			keys.add(invoiceLineTaxTransition.getTax());
			keys.add(invoiceLineTaxTransition.getRateTax());
			keys.add(invoiceLineTaxTransition.getVatLine());
			keys.add(invoiceLineTaxTransition.getUnit());
			keys.add(invoiceLineTaxTransition.getTypeSelect());
			keys.add(invoiceLineTaxTransition.getBaseOnSelect());
			keys.add(invoiceLineTaxTransition.getPricingListVersion());
			
			if (map.containsKey(keys)) {
				InvoiceLineTax invoiceLineTax = map.get(keys);
				invoiceLineTax.setBase(invoiceLineTax.getBase().add(
						invoiceLineTaxTransition.getBase()));
			} 
			else {
				
				InvoiceLineTax invoiceLineTax = new InvoiceLineTax();
				invoiceLineTax.setTax(invoiceLineTaxTransition.getTax());
				invoiceLineTax.setBase(invoiceLineTaxTransition.getBase());
				invoiceLineTax.setRateTax(invoiceLineTaxTransition.getRateTax());
				invoiceLineTax.setUnit(invoiceLineTaxTransition.getUnit());
				invoiceLineTax.setExTaxTotal(computeAmount(invoiceLineTaxTransition.getBase(), invoiceLineTaxTransition.getRateTax()));
				invoiceLineTax.setVatLine(invoiceLineTaxTransition.getVatLine());
				invoiceLineTax.setBaseOnSelect(invoiceLineTaxTransition.getBaseOnSelect());
				invoiceLineTax.setPricingListVersion(invoiceLineTaxTransition.getPricingListVersion());
				invoiceLineTax.setInvoice(invoice);
								
				map.put(keys, invoiceLineTax);
			}
		}

		for (InvoiceLineTax invoiceLineTax : map.values()) {
			
			invoiceLineTax.setExTaxTotal( computeAmount(invoiceLineTax.getBase(), invoiceLineTax.getRateTax()) );
			invoiceLineTaxes.add(invoiceLineTax);
		}

		return invoiceLineTaxes;
	}
	
}