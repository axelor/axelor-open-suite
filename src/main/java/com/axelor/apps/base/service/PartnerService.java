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
