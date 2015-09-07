package com.axelor.apps.crm.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.db.Repository;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface OpportunityService extends Repository<Opportunity>{

	@Transactional
	public void saveOpportunity(Opportunity opportunity);

	@Transactional
	public Partner createClientFromLead(Opportunity opportunity) throws AxelorException;
	
	
}
