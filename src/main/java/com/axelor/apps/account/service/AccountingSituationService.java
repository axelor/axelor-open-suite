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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class AccountingSituationService {

	@Inject
	private AccountConfigService accountConfigService;

	public boolean checkAccountingSituationList(List<AccountingSituation> accountingSituationList, Company company) {

		if(accountingSituationList != null)  { 
			for(AccountingSituation accountingSituation : accountingSituationList) {
	
				if(accountingSituation.getCompany().equals(company))  { 
					return true;
				}
			}
		}
	
		return false;
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public List<AccountingSituation> createAccountingSituation(Partner partner) throws AxelorException {

		Set<Company> companySet = partner.getCompanySet();

		if(companySet != null) {

			List<AccountingSituation> accountingSituationList = partner.getAccountingSituationList();

			if(accountingSituationList == null) {
				accountingSituationList = new ArrayList<AccountingSituation>();
			}

			for(Company company : companySet) {

				if(!checkAccountingSituationList(accountingSituationList, company)) {

					AccountingSituation accountingSituation = this.createAccountingSituation(company);

					accountingSituationList.add(accountingSituation);
					
				}
			}
			return accountingSituationList;
		}
		return null;
	}
	
	
	public AccountingSituation createAccountingSituation(Company company) throws AxelorException {

		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
		
		AccountingSituation accountingSituation = new AccountingSituation();

		accountingSituation.setCompany(company);
		accountingSituation.setCustomerAccount(accountConfigService.getCustomerAccount(accountConfig));
		accountingSituation.setSupplierAccount(accountConfigService.getSupplierAccount(accountConfig));
		accountingSituation.save();
			
		return accountingSituation;
	}
}
