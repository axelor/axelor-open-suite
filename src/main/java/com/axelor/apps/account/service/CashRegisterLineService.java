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
package com.axelor.apps.account.service;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.CashRegisterLine;
import com.axelor.apps.account.db.IAccount;
import com.axelor.apps.account.service.administration.GeneralServiceAccount;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Mail;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class CashRegisterLineService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CashRegisterLineService.class);
	
	@Inject
	private MailService ms;
	
	private DateTime todayTime;
	private User user;

	@Inject
	public CashRegisterLineService(UserService userService) {
		
		this.todayTime = GeneralService.getTodayDateTime();
		this.user = userService.getUser();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Mail closeCashRegister(CashRegisterLine cashRegisterLine) throws AxelorException  {
		Company company = this.user.getActiveCompany();
		if(company == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une société active pour l'utilisateur %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), this.user.getFullName()), IException.CONFIGURATION_ERROR);
		}
		
		LOG.debug("In closeCashRegister");

		CashRegisterLine cashRegisterLineFound = CashRegisterLine
				.filter("self.cashRegister = ?1 and self.cashRegisterDate = ?2 and self.stateSelect = '1'", 
						cashRegisterLine.getCashRegister(), cashRegisterLine.getCashRegisterDate()).fetchOne();
		
		if(cashRegisterLineFound != null)  {
			throw new AxelorException(String.format("%s :\n Une fermeture de caisse existe déjà pour la même date et la même caisse",
					GeneralServiceAccount.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
		}
		else  {
			AccountConfig accountConfig = company.getAccountConfig();
			
			if(accountConfig.getCashRegisterAddressEmail() == null)  {
				throw new AxelorException(String.format("%s :\n Veuillez configurer une adresse email Caisses pour la société %s",
						GeneralServiceAccount.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
			}
			
			cashRegisterLine.setCreateDateTime(this.todayTime);
			cashRegisterLine.setUser(this.user);
			cashRegisterLine.setStateSelect(IAccount.CLOSED_CASHREGISTERLINE);
			cashRegisterLine.save();
			
			return ms.createCashRegisterLineMail(accountConfig.getCashRegisterAddressEmail(), company, cashRegisterLine).save();
			
		}
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void openCashRegister(CashRegisterLine cashRegisterLine)  {
		 Mail.filter("cashRegisterLine = ?1", cashRegisterLine).remove();
		 cashRegisterLine.setStateSelect(IAccount.DRAFT_CASHREGISTERLINE);
		 cashRegisterLine.save();
	}
	
	
}
