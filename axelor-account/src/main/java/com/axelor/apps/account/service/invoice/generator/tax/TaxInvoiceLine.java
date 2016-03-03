/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.invoice.generator.tax;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
				if(taxLine != null)  {
					LOG.debug("TVA {}", taxLine);

					if (map.containsKey(taxLine)) {

						InvoiceLineTax invoiceLineTax = map.get(taxLine);

						// Dans la devise de la facture
						invoiceLineTax.setExTaxBase(invoiceLineTax.getExTaxBase().add(invoiceLine.getExTaxTotal()));

						// Dans la devise de la société
						invoiceLineTax.setCompanyExTaxBase(invoiceLineTax.getCompanyExTaxBase().add(invoiceLine.getCompanyExTaxTotal()).setScale(2, RoundingMode.HALF_UP));

					}
					else {

						InvoiceLineTax invoiceLineTax = new InvoiceLineTax();
						invoiceLineTax.setInvoice(invoice);

						// Dans la devise de la facture
						invoiceLineTax.setExTaxBase(invoiceLine.getExTaxTotal());

						// Dans la devise de la comptabilité du tiers
						invoiceLineTax.setCompanyExTaxBase(invoiceLine.getCompanyExTaxTotal().setScale(2, RoundingMode.HALF_UP));

						invoiceLineTax.setTaxLine(taxLine);
						map.put(taxLine, invoiceLineTax);

					}
				}
			}
		}

		for (InvoiceLineTax invoiceLineTax : map.values()) {

			// Dans la devise de la facture
			BigDecimal exTaxBase = invoiceLineTax.getExTaxBase();
			BigDecimal taxTotal = computeAmount(exTaxBase, invoiceLineTax.getTaxLine().getValue());
			invoiceLineTax.setTaxTotal(taxTotal);
			invoiceLineTax.setInTaxTotal(exTaxBase.add(taxTotal));

			// Dans la devise de la société
			BigDecimal companyExTaxBase = invoiceLineTax.getCompanyExTaxBase();
			BigDecimal companyTaxTotal = computeAmount(companyExTaxBase, invoiceLineTax.getTaxLine().getValue());
			invoiceLineTax.setCompanyTaxTotal(companyTaxTotal);
			invoiceLineTax.setCompanyInTaxTotal(companyExTaxBase.add(companyTaxTotal));

			invoiceLineTaxList.add(invoiceLineTax);

			LOG.debug("Ligne de TVA : Total TVA => {}, Total HT => {}", new Object[] {invoiceLineTax.getTaxTotal(), invoiceLineTax.getInTaxTotal()});

		}

		return invoiceLineTaxList;
	}

}