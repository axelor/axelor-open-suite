/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.base.service;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.google.inject.Inject;


public class BlockingService {
	
	private LocalDate today;

	@Inject
	public BlockingService() {

		this.today = GeneralService.getTodayDate();
	}
	

	public Blocking getBlocking(Partner partner, Company company)  {
		
		if(partner != null && company != null && partner.getBlockingList() != null)  {
			for(Blocking blocking : partner.getBlockingList())  {
				if(blocking.getCompany().equals(company))  {
					return blocking;
				}
			}
		}
		
		return null;
					
	}
	
	
	/**
	 * Le tiers est t'il bloqué en prélèvement
	 * 
	 * @return
	 */
	public boolean isDebitBlockingBlocking(Blocking blocking){
		
		if (blocking != null && blocking.getDebitBlockingOk()){
			
			if (blocking.getDebitBlockingToDate() != null && blocking.getDebitBlockingToDate().isBefore(today)){
				return false;
			}
			else {
				return true;
			}
			
		}
		
		return false;
	}
	
	
	/**
	 * Le tiers est t'il bloqué en prélèvement
	 * 
	 * @return
	 */
	public boolean isDebitBlockingBlocking(Partner partner, Company company){
		
		return this.isDebitBlockingBlocking(
				this.getBlocking(partner, company));
	}
	
	
	/**
	 * Le tiers est t'il bloqué en remboursement
	 * 
	 * @return
	 */
	public boolean isReminderBlocking(Blocking blocking){
		
		if (blocking != null && blocking.getReimbursementBlockingOk()){
			
			if (blocking.getReimbursementBlockingToDate() != null && blocking.getReimbursementBlockingToDate().isBefore(today)){
				return false;
			}
			else {
				return true;
			}
			
		}
		
		return false;
	}
	
	
	/**
	 * Le tiers est t'il bloqué en remboursement
	 * 
	 * @return
	 */
	public boolean isReminderBlocking(Partner partner, Company company){
		
		return this.isReminderBlocking(
				this.getBlocking(partner, company));
	}
	
	
}
