package com.axelor.apps.account.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class JournalService {
	
	private static final Logger LOG = LoggerFactory.getLogger(JournalService.class);
	
	
	/**
	 * 
	 * @param invoice
	 * 
	 * OperationTypeSelect
	 *  1 : Achat fournisseur
	 *	2 : Avoir fournisseur
	 *	3 : Vente client
	 *	4 : Avoir client
	 * @return
	 * @throws AxelorException
	 */
	public Journal getJournal(Invoice invoice) throws AxelorException  {
		
//		if(invoice.getJournal() != null)  {  return invoice.getJournal();  }
		
		Company company = invoice.getCompany();
		
		if(company != null)  {
			switch(invoice.getOperationTypeSelect())  {
			case 1:
				// Si le montant est négatif, alors c'est un avoir
				if(invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == -1)  {
					return company.getSupplierCreditNoteJournal();
				}
				else  {
					return company.getSupplierPurchaseJournal();
				}
				
			case 2:
				// Si le montant est négatif, alors c'est une facture
				if(invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == -1)  {
					return company.getSupplierPurchaseJournal();
				}
				else  {
					return company.getSupplierCreditNoteJournal();
				}
			case 3:
				// Si le montant est négatif, alors c'est un avoir
				if(invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == -1)  {
					return company.getCustomerCreditNoteJournal();
				}
				else  {
					return company.getCustomerSalesJournal();
				}
			case 4:
				// Si le montant est négatif, alors c'est une facture
				if(invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == -1)  {
					return company.getCustomerSalesJournal();
				}
				else  {
					return company.getCustomerCreditNoteJournal();
				}
			
			default:
				throw new AxelorException(String.format("Type de facture absent de la facture %s", invoice.getInvoiceId()), IException.MISSING_FIELD);
			}	
		}
		
		return null;
	}
	
}
