/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.service.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class PaymentModeService {
	
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
	
	
	public Account getCompanyAccount(PaymentMode paymentMode,Company company) throws AxelorException{
		
		LOG.debug("Récupération du compte comptable du mode de paiement associé à la société :" +
			" Société : {}, Mode de paiement : {}", new Object[]{ company.getName(),paymentMode.getName() });
		
		Account account = null;
		
		if(paymentMode.getAccountManagementList() != null && !paymentMode.getAccountManagementList().isEmpty()){
			
			for (AccountManagement am : paymentMode.getAccountManagementList()) {
				if(am.getCompany().equals(company)){
					
					account = am.getCashAccount();
					
				}
			}
			
		}
		
		if (account == null) {
			throw new AxelorException(String.format("Société : %s, Mode de paiement : %S: Compte comptable associé non configuré",
					company.getName(),paymentMode.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return account;
	}

}
