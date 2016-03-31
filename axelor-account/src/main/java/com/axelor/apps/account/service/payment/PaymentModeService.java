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
package com.axelor.apps.account.service.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;

public class PaymentModeService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

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


	public Account getPaymentModeAccount(PaymentMode paymentMode, Company company) throws AxelorException{

		log.debug("Récupération du compte comptable du mode de paiement associé à la société :" +
			" Société : {}, Mode de paiement : {}", new Object[]{ company.getName(),paymentMode.getName() });

		AccountManagement accountManagement = this.getAccountManagement(paymentMode, company);

		if(accountManagement != null)  {
			return accountManagement.getCashAccount();
		}

		throw new AxelorException(String.format(I18n.get("Company")+" : %s, "+I18n.get("Payment mode")+" : %s: "+I18n.get(IExceptionMessage.PAYMENT_MODE_1),
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
							I18n.get(IExceptionMessage.PAYMENT_MODE_2),
							GeneralServiceImpl.EXCEPTION, company.getName(), paymentMode.getName()), IException.CONFIGURATION_ERROR);
		}

		return accountManagement.getSequence();
	}

	public Journal getPaymentModeJournal(PaymentMode paymentMode, Company company) throws AxelorException  {

		AccountManagement accountManagement = this.getAccountManagement(paymentMode, company);

		if(accountManagement == null || accountManagement.getJournal() == null)  {
			throw new AxelorException(String.format(
							I18n.get(IExceptionMessage.PAYMENT_MODE_3),
							GeneralServiceImpl.EXCEPTION, company.getName(), paymentMode.getName()), IException.CONFIGURATION_ERROR);
		}

		return accountManagement.getJournal();
	}

}
