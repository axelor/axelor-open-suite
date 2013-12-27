package com.axelor.apps.crm.service;

import com.axelor.apps.crm.db.Opportunity;
import com.google.inject.persist.Transactional;

public class OpportunityService {
	
	@Transactional
	public void saveOpportunity(Opportunity opportunity){
		opportunity.save();
	}

}
