package com.axelor.apps.account.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Vat;
import com.axelor.apps.account.db.VatAccount;
import com.axelor.apps.base.db.Company;
import com.google.inject.Inject;

public class VatAccountService {
	
	private static final Logger LOG = LoggerFactory.getLogger(VatAccountService.class);

	@Inject
	private VatService vs;
	
	
	public Account getAccount(Vat vat, Company company)  {
		
		VatAccount vatAccount =  this.getVatAccount(vat, company);
		
		if(vatAccount != null)  {
			return vatAccount.getAccount();
		}
		
		return null;
		
	}
	
	
	public VatAccount getVatAccount(Vat vat, Company company)  {
		
		if(vat.getVatAccountList()!= null)  {
			
			
			for(VatAccount vatAccount : vat.getVatAccountList())  {
				
				if(vatAccount.getCompany().equals(company))  {
					return vatAccount;
				}
			}
		}
		
		return null;
		
	}
	
	
}
