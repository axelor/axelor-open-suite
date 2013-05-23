package com.axelor.apps.account.service.invoice.generator.line;

import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;

/**
 * Classe de création de ligne de facture abstraite.
 * Chaine de responsabilité.
 * 
 * @author guerrier
 *
 */
public abstract class InvoiceLineChain extends InvoiceLineGenerator {

	protected InvoiceLineChain next;
	
	protected InvoiceLineChain() { }
	
	protected InvoiceLineChain(int type) {
		
		super(type);
		
	}


	
	public InvoiceLineChain setNext(InvoiceLineChain abstractInvoiceLine){
		
		next = abstractInvoiceLine;
		return abstractInvoiceLine;
		
	}
	
}