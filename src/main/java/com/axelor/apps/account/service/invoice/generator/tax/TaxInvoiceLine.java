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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.service.invoice.generator.TaxGenerator;

public class TaxInvoiceLine extends TaxGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(TaxInvoiceLine.class);
	
	public TaxInvoiceLine(Invoice invoice, List<InvoiceLine> invoiceLines) {
		
		super(invoice, invoiceLines);
		
	}

	/**
	 * Créer les lignes de TVA de la facure. La création des lignes de TVA se
	 * basent sur les lignes de factures
	 * 
	 * @param invoice
	 *            La facture.
	 * 
	 * @param invoiceLines
	 *            Les lignes de facture.
	 * 
	 * @return La liste des lignes de TVA de la facture.
	 */
	@Override
	public List<InvoiceLineTax> creates() {
		
		List<InvoiceLineTax> invoiceLineTaxList = new ArrayList<InvoiceLineTax>();
		Map<TaxLine, InvoiceLineTax> map = new HashMap<TaxLine, InvoiceLineTax>();
		
		if (invoiceLines != null && !invoiceLines.isEmpty()) {

			LOG.debug("Création des lignes de tva pour les lignes de factures.");
			
			for (InvoiceLine invoiceLine : invoiceLines) {

				TaxLine taxLine = invoiceLine.getTaxLine();
				LOG.debug("TVA {}", taxLine);
				
				if (map.containsKey(taxLine)) {
				
					InvoiceLineTax invoiceLineTax = map.get(taxLine);
					
					// Dans la devise de la facture
					invoiceLineTax.setExTaxBase(invoiceLineTax.getExTaxBase().add(invoiceLine.getExTaxTotal()));
					
					// Dans la devise de la comptabilité du tiers
					invoiceLineTax.setAccountingExTaxBase(invoiceLineTax.getAccountingExTaxBase().add(invoiceLine.getAccountingExTaxTotal()));
					
				}
				else {
					
					InvoiceLineTax invoiceLineTax = new InvoiceLineTax();
					invoiceLineTax.setInvoice(invoice);
					
					// Dans la devise de la facture
					invoiceLineTax.setExTaxBase(invoiceLine.getExTaxTotal());
					
					// Dans la devise de la comptabilité du tiers
					invoiceLineTax.setAccountingExTaxBase(invoiceLine.getAccountingExTaxTotal());
					
					invoiceLineTax.setTaxLine(taxLine);
					map.put(taxLine, invoiceLineTax);
					
				}
			}
		}
			
		for (InvoiceLineTax invoiceLineTax : map.values()) {
			
			// Dans la devise de la facture
			BigDecimal exTaxBase = invoiceLineTax.getExTaxBase();
			BigDecimal taxTotal = computeAmount(exTaxBase, invoiceLineTax.getTaxLine().getValue());
			invoiceLineTax.setTaxTotal(taxTotal);
			invoiceLineTax.setInTaxTotal(exTaxBase.add(taxTotal));
			
			// Dans la devise de la comptabilité du tiers
			BigDecimal accountingExTaxBase = invoiceLineTax.getAccountingExTaxBase();
			BigDecimal accountingTaxTotal = computeAmount(accountingExTaxBase, invoiceLineTax.getTaxLine().getValue());
			invoiceLineTax.setAccountingTaxTotal(accountingTaxTotal);
			invoiceLineTax.setAccountingInTaxTotal(accountingExTaxBase.add(accountingTaxTotal));
			
			invoiceLineTaxList.add(invoiceLineTax);

			LOG.debug("Ligne de TVA : Total TVA => {}, Total HT => {}", new Object[] {invoiceLineTax.getTaxTotal(), invoiceLineTax.getInTaxTotal()});
			
		}

		return invoiceLineTaxList;
	}

}