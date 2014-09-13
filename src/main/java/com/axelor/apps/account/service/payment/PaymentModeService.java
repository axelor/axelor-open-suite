/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.administration.GeneralServiceAccount;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class PaymentModeService extends PaymentModeRepository{
	
	private static final Logger LOG = LoggerFactory.getLogger(PaymentModeService.class);
	
//	public Account getCompanyAccount(PaymentMode paymentMode,Company company, boolean isPurchase) throws AxelorException{
//		
//		LOG.debug("Récupération du compte comptable du mode de paiement associé à la société :" +
//			" Société : {}, Mode de paiement : {}", new Object[]{ company.getName(),paymentMode.getName() });
//		
//		Account account = null;
//		
//		if(paymentMode.getAccountManagementList() != null && !paymentMode.getAccountManagementList().isEmpty()){
//			
//			for (AccountManagement am : paymentMode.getAccountManagementList()) {
//				if(am.getCompany().equals(company)){
//					if(isPurchase)  {
//						account = am.getPurchaseAccount();
//					}
//					else  {
//						account = am.getSaleAccount();
//					}
//				}
//			}
//			
//		}
//		
//		if (account == null) {
//			throw new AxelorException(String.format("Société : %s, Mode de paiement : %S: Compte comptable associé non configuré",
//					company.getName(),paymentMode.getName()), IException.CONFIGURATION_ERROR);
//		}
//		
//		return account;
//	}
	
	
	public Account getCompanyAccount(PaymentMode paymentMode, Company company) throws AxelorException{
		
		LOG.debug("Récupération du compte comptable du mode de paiement associé à la société :" +
			" Société : {}, Mode de paiement : {}", new Object[]{ company.getName(),paymentMode.getName() });
		
		AccountManagement accountManagement = this.getAccountManagement(paymentMode, company);
		
		if(accountManagement != null)  {
			return accountManagement.getCashAccount();
		}
		
		throw new AxelorException(String.format("Société : %s, Mode de paiement : %S: Compte comptable associé non configuré",
				company.getName(),paymentMode.getName()), IException.CONFIGURATION_ERROR);
		
	}

	
	public AccountManagement getAccountManagement(PaymentMode paymentMode, Company company)  {
		
		if(paymentMode.getAccountManagementList() == null)  {  return null;  }
		
		for(AccountManagement accountManagement : paymentMode.getAccountManagementList())  {
			
			if(accountManagement.getCompany().equals(company))  {
				
				return accountManagement;
				
			}
		}
		
		return null;
	}
	
	public Sequence getPaymentModeSequence(PaymentMode paymentMode, Company company) throws AxelorException  {
		
		AccountManagement accountManagement = this.getAccountManagement(paymentMode, company);
		
		if(accountManagement == null || accountManagement.getSequence() == null)  {
			throw new AxelorException(String.format(
							"%s :\n Erreur : Veuillez configurer une séquence pour la société %s et le mode de paiement %s",
							GeneralServiceAccount.getExceptionAccountingMsg(), company.getName(), paymentMode.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountManagement.getSequence();
	}
	
	public Journal getPaymentModeJournal(PaymentMode paymentMode, Company company) throws AxelorException  {
		
		AccountManagement accountManagement = this.getAccountManagement(paymentMode, company);
		
		if(accountManagement == null || accountManagement.getJournal() == null)  {
			throw new AxelorException(String.format(
							"%s :\n Erreur : Veuillez configurer un journal pour la société %s et le mode de paiement %s",
							GeneralServiceAccount.getExceptionAccountingMsg(), company.getName(), paymentMode.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return accountManagement.getJournal();
	}
	
}
