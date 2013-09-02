package com.axelor.apps.crm.web;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.IEvent;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.service.ConvertLeadWizardService;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

public class ConvertLeadWizardController {

	@Inject
	private LeadService leadService;
	
	@Inject
	private ConvertLeadWizardService convertLeadWizardService;
	
	private static final Logger LOG = LoggerFactory.getLogger(ConvertLeadWizardController.class);
	
	@SuppressWarnings("unchecked")
	public void convertLead(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Context context = request.getContext();
		
		Map<String, Object> leadContext = (Map<String, Object>) context.get("_lead");
		
		Lead lead = Lead.find(((Integer)leadContext.get("id")).longValue());
		
		Partner partner = null;
		Partner contactPartner = null;
		Opportunity opportunity = null;
		Event callEvent = null;
		Event meetingEvent = null;
		Event taskEvent = null;
		
		if(context.get("hasConvertIntoContact") != null && (Boolean) context.get("hasConvertIntoContact")) {
			contactPartner = convertLeadWizardService.createPartner((Map<String, Object>) context.get("contactPartner"));
		}
		else  if(context.get("selectContact") != null) {
			contactPartner = Partner.find((Long) context.get("selectContactPartner"));
		}
		
		if(context.get("hasConvertIntoPartner") != null && (Boolean) context.get("hasConvertIntoPartner")) {
			partner = convertLeadWizardService.createPartner((Map<String, Object>) context.get("partner"));
		}
		else  if(context.get("selectPartner") != null) {
			partner = Partner.find((Long) context.get("selectPartner"));
		}
		
		if(context.get("hasConvertIntoOpportunity") != null && (Boolean) context.get("hasConvertIntoOpportunity")) {
			opportunity = convertLeadWizardService.createOpportunity((Map<String, Object>) context.get("opportunity"));
		}
		if(context.get("hasConvertIntoCall") != null && (Boolean) context.get("hasConvertIntoCall")) {
			callEvent = convertLeadWizardService.createEvent((Map<String, Object>) context.get("callEvent"), IEvent.CALL);
		}
		if(context.get("hasConvertIntoMeeting") != null && (Boolean) context.get("hasConvertIntoMeeting")) {
			meetingEvent = convertLeadWizardService.createEvent((Map<String, Object>) context.get("meetingEvent"), IEvent.MEETING);
		}
		if(context.get("hasConvertIntoTask") != null && (Boolean) context.get("hasConvertIntoTask")) {
			taskEvent = convertLeadWizardService.createEvent((Map<String, Object>)context.get("taskEvent"), IEvent.TASK);
		}
		
		leadService.convertLead(lead, partner, contactPartner, opportunity, callEvent, meetingEvent, taskEvent);
	}
	
	
}
