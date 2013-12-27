package com.axelor.apps.crm.web;

import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.service.OpportunityService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class OpportunityController {
	@Inject
	OpportunityService ose;
	
	public void saveOpportunitySalesStage(ActionRequest request, ActionResponse response) throws AxelorException {
		Opportunity opportunity = request.getContext().asType(Opportunity.class);
		Opportunity persistOpportunity = Opportunity.find(opportunity.getId());
		persistOpportunity.setSalesStageSelect(opportunity.getSalesStageSelect());
		ose.saveOpportunity(persistOpportunity);
	}
}
