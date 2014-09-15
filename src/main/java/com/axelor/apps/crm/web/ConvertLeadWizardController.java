/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.crm.web;

import java.util.Map;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PartnerService;
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
	
	@Inject
	private PartnerService partnerService;
	
	
	@SuppressWarnings("unchecked")
	public void convertLead(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Context context = request.getContext();
		
		Map<String, Object> leadContext = (Map<String, Object>) context.get("_lead");
		
		Lead lead = leadService.find(((Integer)leadContext.get("id")).longValue());
		
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
			Map<String, Object> selectContactContext = (Map<String, Object>) context.get("selectContact");
			contactPartner = partnerService.find(((Integer) selectContactContext.get("id")).longValue());
		}
		
		if(context.get("hasConvertIntoPartner") != null && (Boolean) context.get("hasConvertIntoPartner")) {
			partner = convertLeadWizardService.createPartner((Map<String, Object>) context.get("partner"));
		}
		else  if(context.get("selectPartner") != null) {
			Map<String, Object> selectPartnerContext = (Map<String, Object>) context.get("selectPartner");
			partner = partnerService.find(((Integer) selectPartnerContext.get("id")).longValue());
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
		
		response.setFlash("Prospect converti");
	}
	
	
}
