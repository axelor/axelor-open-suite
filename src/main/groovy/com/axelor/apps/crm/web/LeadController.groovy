package com.axelor.apps.crm.web

import groovy.util.logging.Slf4j

import com.axelor.apps.base.db.Partner
import com.axelor.apps.crm.db.Lead
import com.axelor.apps.crm.service.LeadService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject


@Slf4j
public class LeadController {

	@Inject
	private LeadService leadService
	
	def convertLead(ActionRequest request, ActionResponse response) {
		
		Lead lead = request.context as Lead
		
		Partner partner = leadService.convertLead(lead);
		
		response.reload = true
		
		if(partner != null)  {
			response.flash = "Lead converted"
		}	
		else  {
			response.flash = "Lead not converted"
		}
		
	}
  
}
