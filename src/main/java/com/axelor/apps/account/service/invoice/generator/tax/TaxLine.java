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
 * 
 * @author Cédric Guerrier
 * 
 * @version 1.0
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