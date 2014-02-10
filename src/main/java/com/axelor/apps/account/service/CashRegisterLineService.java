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
package com.axelor.apps.account.service;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.CashRegisterLine;
import com.axelor.apps.account.db.IAccount;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Mail;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.MailService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class CashRegisterLineService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CashRegisterLineService.class);
	
	@Inject
	private MailService ms;
	
	private DateTime todayTime;
	private UserInfo user;

	@Inject
	public CashRegisterLineService(UserInfoService uis) {
		
		this.todayTime = GeneralService.getTodayDateTime();
		this.user = uis.getUserInfo();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Mail closeCashRegister(CashRegisterLine cashRegisterLine) throws AxelorException  {
		Company company = this.user.getActiveCompany();
		if(company == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer une société active pour l'utilisateur %s",
					GeneralService.getExceptionAccountingMsg(), this.user.getFullName()), IException.CONFIGURATION_ERROR);
		}
		
		LOG.debug("In closeCashRegister");

		CashRegisterLine cashRegisterLineFound = CashRegisterLine
				.all().filter("self.cashRegister = ?1 and self.cashRegisterDate = ?2 and self.stateSelect = '1'", 
						cashRegisterLine.getCashRegister(), cashRegisterLine.getCashRegisterDate()).fetchOne();
		
		if(cashRegisterLineFound != null)  {
			throw new AxelorException(String.format("%s :\n Une fermeture de caisse existe déjà pour la même date et la même caisse",
					GeneralService.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
		}
		else  {
			AccountConfig accountConfig = company.getAccountConfig();
			
			if(accountConfig.getCashRegisterAddressEmail() == null)  {
				throw new AxelorException(String.format("%s :\n Veuillez configurer une adresse email Caisses pour la société %s",
						GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
			}
			
			cashRegisterLine.setCreateDateTime(this.todayTime);
			cashRegisterLine.setUserInfo(this.user);
			cashRegisterLine.setStateSelect(IAccount.CLOSED_CASHREGISTERLINE);
			cashRegisterLine.save();
			
			return ms.createCashRegisterLineMail(accountConfig.getCashRegisterAddressEmail(), company, cashRegisterLine).save();
			
		}
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void openCashRegister(CashRegisterLine cashRegisterLine)  {
		 Mail.all().filter("cashRegisterLine = ?1", cashRegisterLine).remove();
		 cashRegisterLine.setStateSelect(IAccount.DRAFT_CASHREGISTERLINE);
		 cashRegisterLine.save();
	}
	
	
}
