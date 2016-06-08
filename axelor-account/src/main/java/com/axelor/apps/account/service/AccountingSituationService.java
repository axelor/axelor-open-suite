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
package com.axelor.apps.account.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class AccountingSituationService	{

	protected AccountConfigService accountConfigService;
	protected AccountingSituationRepository situationRepository;
	
	@Inject
	public AccountingSituationService(AccountConfigService accountConfigService, AccountingSituationRepository situationRepository)  {
		
		this.accountConfigService = accountConfigService;
		this.situationRepository = situationRepository;
	}

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
					accountingSituation.setPartner(partner);
					accountingSituation.setCompanyBankDetails(company.getDefaultBankDetails());
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
		situationRepository.save(accountingSituation);
			
		return accountingSituation;
	}
	
	public AccountingSituation getAccountingSituation(Partner partner, Company company)  {
		
		if(partner.getAccountingSituationList() == null)  {  return null;  }
		
		for(AccountingSituation accountingSituation : partner.getAccountingSituationList())  {
			
			if(accountingSituation.getCompany().equals(company))  {
				
				return accountingSituation;
				
			}
		}
		
		return null;
		
	}
	
}
