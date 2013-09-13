/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.crm.web;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Event;
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
			callEvent = convertLeadWizardService.createEvent((Map<String, Object>) context.get("callEvent"));
		}
		if(context.get("hasConvertIntoMeeting") != null && (Boolean) context.get("hasConvertIntoMeeting")) {
			meetingEvent = convertLeadWizardService.createEvent((Map<String, Object>) context.get("meetingEvent"));
		}
		if(context.get("hasConvertIntoTask") != null && (Boolean) context.get("hasConvertIntoTask")) {
			taskEvent = convertLeadWizardService.createEvent((Map<String, Object>)context.get("taskEvent"));
		}
		
		leadService.convertLead(lead, partner, contactPartner, opportunity, callEvent, meetingEvent, taskEvent);
	}
	
	
}
