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
package com.axelor.csv.script;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;


public class ImportPartner {
		
		public Object importAccountingSituation(Object bean, Map values) {
			assert bean instanceof Partner;
	        try{
	            Partner partner = (Partner) bean;
				partner.setContactPartnerSet(new HashSet<Partner>());
				List<? extends Partner> partnerList = Partner.all().filter("self.mainPartner.id = ?1",partner.getId()).fetch();
				for(Partner pt : partnerList)
					partner.getContactPartnerSet().add(pt);
				for(Company company : partner.getCompanySet())  {
					AccountingSituation accountingSituation = new AccountingSituation();
					accountingSituation.setPartner(partner);
					accountingSituation.setCompany(company);
					accountingSituation.setCustomerAccount(Account.all().filter("self.code = ?1 AND self.company = ?2",values.get("customerAccount_code").toString(),company).fetchOne());
					accountingSituation.setSupplierAccount(Account.all().filter("self.code = ?1 AND self.company = ?2",values.get("supplierAccount_code").toString(),company).fetchOne());
					if(partner.getAccountingSituationList() == null)  {
						partner.setAccountingSituationList(new ArrayList<AccountingSituation>());
					}
					partner.getAccountingSituationList().add(accountingSituation);
						
				}
				return partner;
	        }catch(Exception e){
	            e.printStackTrace();
	        }
	        return bean;
		}
		
}



