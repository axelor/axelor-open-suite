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
package com.axelor.csv.script

import org.joda.time.LocalDate

import com.axelor.apps.account.db.Account
import com.axelor.apps.account.db.AccountingSituation
import com.axelor.apps.base.db.Period
import com.axelor.apps.base.db.Year
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
				partner.contactPartnerSet = new HashSet<Partner>()
				Partner.all().filter("self.mainPartner.id = ?1",partner.id).fetch().each{it->partner.contactPartnerSet.add(it)}
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



