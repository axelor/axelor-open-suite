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
import com.axelor.apps.account.db.InvoiceLineVat;
import com.axelor.apps.account.db.VatLine;
import com.axelor.apps.account.service.invoice.generator.TaxGenerator;

public class VatInvoiceLine extends TaxGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(VatInvoiceLine.class);
	
	private List<InvoiceLineTax> invoiceLineTaxes;

	public VatInvoiceLine(Invoice invoice, List<InvoiceLine> invoiceLines, List<InvoiceLineTax> invoiceLineTaxes) {
		
		super(invoice, invoiceLines);
		this.invoiceLineTaxes = invoiceLineTaxes;
		
	}

	/**
	 * Créer les lignes de TVA de la facure. La création des lignes de TVA se
	 * basent sur les lignes de factures ainsi que les lignes de taxes de
	 * celle-ci.
	 * 
	 * @param invoice
	 *            La facture.
	 * 
	 * @param invoiceLines
	 *            Les lignes de facture.
	 * 
	 * @param invoiceLineTaxes
	 *            Les lignes des taxes de la facture.
	 * 
	 * @return La liste des lignes de TVA de la facture.
	 */
	@Override
	public List<InvoiceLineVat> creates() {
		
		List<InvoiceLineVat> vatLines = new ArrayList<InvoiceLineVat>();
		Map<VatLine, InvoiceLineVat> map = new HashMap<VatLine, InvoiceLineVat>();
		
		if (invoiceLines != null && !invoiceLines.isEmpty()) {

			LOG.debug("Création des lignes de tva pour les lignes de factures.");
			
			for (InvoiceLine invoiceLine : invoiceLines) {

				VatLine vatLine = invoiceLine.getVatLine();
				LOG.debug("TVA {}", vatLine);
				
				if (map.containsKey(vatLine)) {
				
					InvoiceLineVat invoiceLineVat = map.get(vatLine);
					
					// Dans la devise de la facture
					invoiceLineVat.setExAllTaxBase(invoiceLineVat.getExAllTaxBase().add(invoiceLine.getExTaxTotal()));
					invoiceLineVat.setExTaxBase(invoiceLineVat.getExTaxBase().add(invoiceLine.getExTaxTotal()));
					
					// Dans la devise de la comptabilité du tiers
					invoiceLineVat.setAccountingExAllTaxBase(invoiceLineVat.getAccountingExAllTaxBase().add(invoiceLine.getAccountingExTaxTotal()));
					invoiceLineVat.setAccountingExTaxBase(invoiceLineVat.getAccountingExTaxBase().add(invoiceLine.getAccountingExTaxTotal()));
					
				}
				else {
					
					InvoiceLineVat invoiceLineVat = new InvoiceLineVat();
					invoiceLineVat.setInvoice(invoice);
					
					// Dans la devise de la facture
					invoiceLineVat.setExAllTaxBase(invoiceLine.getExTaxTotal());
					invoiceLineVat.setExTaxBase(invoiceLine.getExTaxTotal());
					
					// Dans la devise de la comptabilité du tiers
					invoiceLineVat.setAccountingExAllTaxBase(invoiceLine.getAccountingExTaxTotal());
					invoiceLineVat.setAccountingExTaxBase(invoiceLine.getAccountingExTaxTotal());
					
					invoiceLineVat.setVatLine(vatLine);
					map.put(vatLine, invoiceLineVat);
					
				}
			}
		}
			
		if (invoiceLineTaxes != null && !invoiceLineTaxes.isEmpty()){

			LOG.debug("Création des lignes de tva pour les lignes de taxes.");
			
			for (InvoiceLineTax invoiceLineTax : invoiceLineTaxes) {

				VatLine vatLine = invoiceLineTax.getVatLine();
				LOG.debug("TVA {}", vatLine);
				
				if (map.containsKey(vatLine)) {
					
					InvoiceLineVat invoiceLineVat = map.get(vatLine);
					
					// Dans la devise de la facture
					invoiceLineVat.setExTaxBase(invoiceLineVat.getExTaxBase().add(invoiceLineTax.getExTaxTotal()));
					
					// Dans la devise de la comptabilité du tiers
					invoiceLineVat.setAccountingExTaxBase(invoiceLineVat.getAccountingExTaxBase().add(invoiceLineTax.getAccountingExTaxTotal()));
					
				} 
				else {
					
					InvoiceLineVat invoiceLineVat = new InvoiceLineVat();
					invoiceLineVat.setInvoice(invoice);
					
					// Dans la devise de la facture
					invoiceLineVat.setExTaxBase(invoiceLineTax.getExTaxTotal());
					
					// Dans la devise de la comptabilité du tiers
					invoiceLineVat.setAccountingExTaxBase(invoiceLineTax.getAccountingExTaxTotal());
					
					invoiceLineVat.setVatLine(vatLine);
					map.put(vatLine, invoiceLineVat);
					
				}
			}
			
		}

		for (InvoiceLineVat vatLine : map.values()) {
			
			// Dans la devise de la facture
			BigDecimal vatExTaxBase = vatLine.getExTaxBase();
			BigDecimal vatTotal = computeAmount(vatExTaxBase, vatLine.getVatLine().getValue());
			vatLine.setVatTotal(vatTotal);
			vatLine.setInTaxTotal(vatExTaxBase.add(vatTotal));
			
			// Dans la devise de la comptabilité du tiers
			BigDecimal accountingVatExTaxBase = vatLine.getAccountingExTaxBase();
			BigDecimal accountingVatTotal = computeAmount(accountingVatExTaxBase, vatLine.getVatLine().getValue());
			vatLine.setAccountingVatTotal(accountingVatTotal);
			vatLine.setAccountingInTaxTotal(accountingVatExTaxBase.add(accountingVatTotal));
			
			vatLines.add(vatLine);

			LOG.debug("Ligne de TVA : Total TVA => {}, Total HT => {}", new Object[] {vatLine.getVatTotal(), vatLine.getInTaxTotal()});
			
		}

		return vatLines;
	}

}