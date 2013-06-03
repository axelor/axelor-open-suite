package com.axelor.apps.crm.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Lead;
import com.google.inject.persist.Transactional;

public class LeadService {

	
	@Transactional
	public Partner convertLead(Lead lead)  {
		
		Partner partner = new Partner();
		partner.setFirstName(lead.getFirstName());
		partner.setName(lead.getEnterpriseName());
		partner.setTitleSelect(lead.getTitleSelect());
		partner.setCustomerTypeSelect(2);
		// add others
		
		return partner;
	}
}
