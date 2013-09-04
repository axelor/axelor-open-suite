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
