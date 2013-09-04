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
package com.axelor.apps.base.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.google.inject.persist.Transactional;


public class PartnerService {


	public boolean checkAccountingSituationList(List<AccountingSituation> accountingSituationList, Company company) {

		for(AccountingSituation accountingSituation : accountingSituationList) {

			if(accountingSituation.getCompany().equals(company))  { 
				return true;
			}
		}
		return false;
	}

	@Transactional
	public List<AccountingSituation> createAccountingSituation(Partner partner) {

		Set<Company> companySet = partner.getCompanySet();
		boolean emptyList = false;

		if(companySet != null) {

			List<AccountingSituation> accountingSituationList = partner.getAccountingSituationList();

			if(accountingSituationList == null) {
				accountingSituationList = new ArrayList<AccountingSituation>();
				emptyList = true;
			}

			for(Company company : companySet) {

				if(company.getCustomerAccount() != null && company.getSupplierAccount() != null) {

					if(emptyList || (emptyList == false && !checkAccountingSituationList(accountingSituationList, company))) {

						AccountingSituation as = new AccountingSituation();

						as.setCompany(company);
						as.setCustomerAccount(company.getCustomerAccount());
						as.setSupplierAccount(company.getSupplierAccount());
						accountingSituationList.add(as);
						as.save();
					}
				}
			}
			return accountingSituationList;
		}
		return null;
	}
}
