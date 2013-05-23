package com.axelor.csv.script

import org.joda.time.LocalDate

import com.axelor.apps.account.db.Account
import com.axelor.apps.account.db.AccountingSituation
import com.axelor.apps.account.db.Period
import com.axelor.apps.account.db.Year
import com.axelor.apps.base.db.General
import com.axelor.apps.base.db.Partner
import com.axelor.apps.base.db.Status
import com.axelor.apps.base.db.Company
import com.google.inject.persist.Transactional


class ImportPartner {
		
		Object importAccountingSituation(Object bean, Map values) {
			assert bean instanceof Partner
	        try{
	            Partner partner = (Partner) bean
				
				for(Company company : partner.companySet)  {
					
					AccountingSituation accountingSituation = new AccountingSituation()
					accountingSituation.partner = partner
					accountingSituation.company = company
					accountingSituation.customerAccount = Account.all().filter("self.code = ?1 AND self.company = ?2",values['customerAccount_code'].toString(),company).fetchOne()
					accountingSituation.supplierAccount = Account.all().filter("self.code = ?1 AND self.company = ?2",values['supplierAccount_code'].toString(),company).fetchOne()
					if(partner.accountingSituationList == null)  {
						partner.accountingSituationList = new ArrayList<AccountingSituation>()
					}
					partner.accountingSituationList.add(accountingSituation)
					
				}
				
				return partner
	        }catch(Exception e){
	            e.printStackTrace()
	        }
		}
		
}



