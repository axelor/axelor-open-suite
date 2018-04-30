/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.crm.service.ConvertLeadWizardService;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ConvertLeadWizardController {

	@Inject
	private LeadRepository leadRepo;

	@Inject
	private ConvertLeadWizardService convertLeadWizardService;

	@Inject
	private PartnerRepository partnerRepo;
	
	@Inject
	private AppBaseService appBaseService;
	
	@Inject
	private LeadService leadService;

	@SuppressWarnings("unchecked")
	public void convertLead(ActionRequest request, ActionResponse response) throws AxelorException {

		Context context = request.getContext();

		Map<String, Object> leadContext = (Map<String, Object>) context.get("_lead");

		Lead lead = leadRepo.find(((Integer)leadContext.get("id")).longValue());
		
		Partner partner = null;
		Partner contactPartner = null;
		
		Integer leadToPartnerSelect = (Integer) context.get("leadToPartnerSelect");
		Integer leadToContactSelect = (Integer) context.get("leadToContactSelect");


		if(leadToPartnerSelect == LeadRepository.CONVERT_LEAD_CREATE_PARTNER) {
			
			partner = convertLeadWizardService.createPartner((Map<String, Object>) context.get("partner"),
															convertLeadWizardService.createPrimaryAddress(context),
															convertLeadWizardService.createOtherAddress(context));
			//TODO check all required fields...
		}
		else  if(leadToPartnerSelect == LeadRepository.CONVERT_LEAD_SELECT_PARTNER) {
			Map<String, Object> selectPartnerContext = (Map<String, Object>) context.get("selectPartner");
			partner = partnerRepo.find(((Integer) selectPartnerContext.get("id")).longValue());
		}
		
		if(leadToPartnerSelect == LeadRepository.CONVERT_LEAD_CREATE_PARTNER 
				|| leadToContactSelect == LeadRepository.CONVERT_LEAD_CREATE_CONTACT) {

			contactPartner = convertLeadWizardService.createPartner((Map<String, Object>) context.get("contactPartner"),
																	convertLeadWizardService.createPrimaryAddress(context),
																	convertLeadWizardService.createOtherAddress(context));
			contactPartner.setIsContact(true);
			//TODO check all required fields...
		}
		else  if(leadToPartnerSelect == LeadRepository.CONVERT_LEAD_SELECT_PARTNER 
				&& leadToContactSelect == LeadRepository.CONVERT_LEAD_SELECT_CONTACT) {
			Map<String, Object> selectContactContext = (Map<String, Object>) context.get("selectContact");
			contactPartner = partnerRepo.find(((Integer) selectContactContext.get("id")).longValue());
		}
		
		
		leadService.convertLead(lead, partner, contactPartner);

		response.setFlash(I18n.get(IExceptionMessage.CONVERT_LEAD_1));
		response.setCanClose(true);
	}
	
	public void setDefaults(ActionRequest request, ActionResponse response) throws AxelorException { 
		
		Lead lead = findLead(request);
		
		response.setAttr("$primaryAddress", "value", lead.getPrimaryAddress());
		response.setAttr("$primaryCity", "value", lead.getPrimaryCity());
		response.setAttr("$primaryState", "value", lead.getPrimaryState());
		response.setAttr("$primaryPostalCode", "value", lead.getPrimaryPostalCode());
		response.setAttr("$primaryCountry", "value", lead.getPrimaryCountry());
		response.setAttr("$otherAddress", "value", lead.getOtherAddress());
		response.setAttr("$otherCity", "value", lead.getOtherCity());
		response.setAttr("$otherState", "value", lead.getOtherState());
		response.setAttr("$otherPostalCode", "value", lead.getOtherPostalCode());
		response.setAttr("$otherCountry", "value", lead.getOtherCountry());
		response.setAttr("$contactAddress", "value", lead.getPrimaryAddress());
		response.setAttr("$contactCity", "value", lead.getPrimaryCity());
		response.setAttr("$contactState", "value", lead.getPrimaryState());
		response.setAttr("$contactPostalCode", "value", lead.getPrimaryPostalCode());
		response.setAttr("$contactCountry", "value", lead.getPrimaryCountry());
		response.setAttr("leadToPartnerSelect", "value", 0);
		response.setAttr("leadToContactSelect", "value", 0);
	}
	
	public void setPartnerDefaults(ActionRequest request, ActionResponse response) throws AxelorException { 
		
		Lead lead = findLead(request);
		
		AppBase appBase = appBaseService.getAppBase();
		response.setAttr("name", "value", lead.getEnterpriseName());
		response.setAttr("industrySector", "value", lead.getIndustrySector());
		response.setAttr("titleSelect", "value", lead.getTitleSelect());
		response.setAttr("emailAddress", "value", lead.getEmailAddress());
		response.setAttr("fax", "value", lead.getFax());
		response.setAttr("mobilePhone", "value", lead.getMobilePhone());
		response.setAttr("fixedPhone", "value", lead.getFixedPhone());
		response.setAttr("webSite", "value", lead.getWebSite());
		response.setAttr("source", "value", lead.getSource());
		response.setAttr("department", "value", lead.getDepartment());
		response.setAttr("team", "value", lead.getTeam());
		response.setAttr("user", "value", lead.getUser());
		response.setAttr("isProspect", "value", true);
		response.setAttr("partnerTypeSelect", "value", "1");
		response.setAttr("language", "value", appBase.getDefaultPartnerLanguage());
	}
	
	
	public void setContactDefaults(ActionRequest request, ActionResponse response) throws AxelorException { 
		
		Lead lead = findLead(request);
		
		response.setAttr("firstName", "value", lead.getFirstName());
		response.setAttr("name", "value", lead.getName());
		response.setAttr("titleSelect", "value", lead.getTitleSelect());
		response.setAttr("emailAddress", "value", lead.getEmailAddress());
		response.setAttr("fax", "value", lead.getFax());
		response.setAttr("mobilePhone", "value", lead.getMobilePhone());
		response.setAttr("fixedPhone", "value", lead.getFixedPhone());
		response.setAttr("user", "value", lead.getUser());
		response.setAttr("team", "value", lead.getTeam());
		response.setAttr("jobTitle", "value", lead.getJobTitle());
	}

	
	private Lead findLead(ActionRequest request) throws AxelorException {
		
		Context context = request.getContext();
		
		Lead lead = null;
		
		if (context.getParent() != null && context.getParent().get("_model").equals("com.axelor.apps.base.db.Wizard")) {
			context = context.getParent();
		}
		
		Map leadMap = (Map) context.get("_lead");
		if (leadMap != null && leadMap.get("id") != null) {
			lead = leadRepo.find(Long.parseLong(leadMap.get("id").toString()));
		}
		
		if (lead == null) {
            throw new AxelorException(IException.NO_VALUE, I18n.get(IExceptionMessage.CONVERT_LEAD_MISSING));
        }
		
		return lead;
	}

}
