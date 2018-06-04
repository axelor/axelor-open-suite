/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import java.math.BigDecimal;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;

public class JournalService {
	
	private AccountConfigService accountConfigService;
	
	public JournalService()  {
		
		this.accountConfigService = new AccountConfigService();
		
	}
	
	
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
		
		Company company = invoice.getCompany();
		
		if(company != null)  {
			
			AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
			
			switch(invoice.getOperationTypeSelect())  {
			case 1:
				// Si le montant est négatif, alors c'est un avoir
				if(invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == -1)  {
					return accountConfigService.getSupplierCreditNoteJournal(accountConfig);
				}
				else  {
					return accountConfigService.getSupplierPurchaseJournal(accountConfig);
				}
				
			case 2:
				// Si le montant est négatif, alors c'est une facture
				if(invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == -1)  {
					return accountConfigService.getSupplierPurchaseJournal(accountConfig);
				}
				else  {
					return accountConfigService.getSupplierCreditNoteJournal(accountConfig);
				}
			case 3:
				// Si le montant est négatif, alors c'est un avoir
				if(invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == -1)  {
					return accountConfigService.getCustomerCreditNoteJournal(accountConfig);
				}
				else  {
					return accountConfigService.getCustomerSalesJournal(accountConfig);
				}
			case 4:
				// Si le montant est négatif, alors c'est une facture
				if(invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == -1)  {
					return accountConfigService.getCustomerSalesJournal(accountConfig);
				}
				else  {
					return accountConfigService.getCustomerCreditNoteJournal(accountConfig);
				}
			
			default:
				throw new AxelorException(invoice, TraceBackRepository.CATEGORY_MISSING_FIELD, I18n.get(IExceptionMessage.JOURNAL_1), invoice.getInvoiceId());
			}	
		}
		
		return null;
	}
	
}
