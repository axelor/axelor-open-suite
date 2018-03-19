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

import java.time.LocalDateTime;
import java.util.Map;

import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.crm.service.ConvertLeadWizardService;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
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
	private CompanyRepository companyRepo;
	
	@Inject
	private AppBaseService appBaseService;

	@SuppressWarnings("unchecked")
	public void convertLead(ActionRequest request, ActionResponse response) throws AxelorException {

		Context context = request.getContext();

		Map<String, Object> leadContext = (Map<String, Object>) context.get("_lead");

		Lead lead = leadRepo.find(((Integer)leadContext.get("id")).longValue());

		Partner partner = null;
		Partner contactPartner = null;
		Partner prospectPartner = null;
		Opportunity opportunity = null;
		Event callEvent = null;
		Event meetingEvent = null;
		Event taskEvent = null;

		if(context.get("hasConvertIntoContact") != null && (Boolean) context.get("hasConvertIntoContact")) {

			contactPartner = convertLeadWizardService.createPartner((Map<String, Object>) context.get("contactPartner"),
																	convertLeadWizardService.createPrimaryAddress(context),
																	convertLeadWizardService.createOtherAddress(context));
			contactPartner.setIsContact(true);
			//TODO check all required fields...
		}
		else  if(context.get("selectContact") != null) {
			Map<String, Object> selectContactContext = (Map<String, Object>) context.get("selectContact");
			contactPartner = partnerRepo.find(((Integer) selectContactContext.get("id")).longValue());
		}

		if(context.get("hasConvertIntoPartner") != null && (Boolean) context.get("hasConvertIntoPartner")) {
			
			partner = convertLeadWizardService.createPartner((Map<String, Object>) context.get("partner"),
															convertLeadWizardService.createPrimaryAddress(context),
															convertLeadWizardService.createOtherAddress(context));
			//TODO check all required fields...
		}
		else  if(context.get("selectPartner") != null) {
			Map<String, Object> selectPartnerContext = (Map<String, Object>) context.get("selectPartner");
			partner = partnerRepo.find(((Integer) selectPartnerContext.get("id")).longValue());
		}
		
		if(context.get("hasConvertIntoProspect") != null && (Boolean) context.get("hasConvertIntoProspect")) {

			prospectPartner = convertLeadWizardService.createPartner((Map<String, Object>) context.get("prospectPartner"),
																	convertLeadWizardService.createPrimaryAddress(context),
																	convertLeadWizardService.createOtherAddress(context));
			prospectPartner.setIsProspect(true);
			//TODO check all required fields...
		}
		else  if(context.get("selectProspectPartner") != null) {
			Map<String, Object> selectPartnerContext = (Map<String, Object>) context.get("selectProspectPartner");
			prospectPartner = partnerRepo.find(((Integer) selectPartnerContext.get("id")).longValue());
		}

		if(context.get("hasConvertIntoOpportunity") != null && (Boolean) context.get("hasConvertIntoOpportunity")) {
			opportunity = convertLeadWizardService.createOpportunity((Map<String, Object>) context.get("opportunity"));
			//TODO check all required fields...
		}
		if(context.get("hasConvertIntoCall") != null && (Boolean) context.get("hasConvertIntoCall")) {
			callEvent = convertLeadWizardService.createEvent((Map<String, Object>) context.get("callEvent"));
			//TODO check all required fields...
		}
		if(context.get("hasConvertIntoMeeting") != null && (Boolean) context.get("hasConvertIntoMeeting")) {
			meetingEvent = convertLeadWizardService.createEvent((Map<String, Object>) context.get("meetingEvent"));
			//TODO check all required fields...
		}
		if(context.get("hasConvertIntoTask") != null && (Boolean) context.get("hasConvertIntoTask")) {
			taskEvent = convertLeadWizardService.createEvent((Map<String, Object>)context.get("taskEvent"));
			//TODO check all required fields...
		}
		
		Beans.get(LeadService.class).convertLead(lead, partner, prospectPartner, contactPartner, opportunity, callEvent, meetingEvent, taskEvent);

		response.setFlash(I18n.get(IExceptionMessage.CONVERT_LEAD_1));
		response.setCanClose(true);
	}
	
	public void setConvertLeadIntoContact(ActionRequest request, ActionResponse response) { 
		Context context = request.getContext();
		Lead lead;
		if (context.getParent() != null && context.getParent().get("_model").equals("com.axelor.apps.base.db.Wizard")) {
			lead = leadRepo.find(Long.parseLong(((Map) context.getParent().get("_lead")).get("id").toString()));
		} else {
			lead = leadRepo.find(Long.parseLong(((Map) context.get("_lead")).get("id").toString()));
		}
		AppBase appBase = appBaseService.getAppBase();
		response.setAttr("isContact", "value", false);
		response.setAttr("firstName", "value", lead.getFirstName());
		response.setAttr("name", "value", lead.getName());
		response.setAttr("titleSelect", "value", lead.getTitleSelect());
		response.setAttr("emailAddress", "value", lead.getEmailAddress());
		response.setAttr("fax", "value", lead.getFax());
		response.setAttr("mobilePhone", "value", lead.getMobilePhone());
		response.setAttr("fixedPhone", "value", lead.getFixedPhone());
		response.setAttr("webSite", "value", lead.getWebSite());
		response.setAttr("source", "value", lead.getSource());
		response.setAttr("department", "value", lead.getDepartment());
		response.setAttr("user", "value", lead.getUser());
		response.setAttr("team", "value", lead.getTeam());
		response.setAttr("jobTitle", "value", lead.getJobTitle());
		response.setAttr("language", "value", appBase.getDefaultPartnerLanguage());
	}
	
	public void setConvertLeadIntoPartner(ActionRequest request, ActionResponse response) { 
		Context context = request.getContext();
		Lead lead;
		if (context.getParent() != null && context.getParent().get("_model").equals("com.axelor.apps.base.db.Wizard")) {
			lead = leadRepo.find(Long.parseLong(((Map) context.getParent().get("_lead")).get("id").toString()));
		} else {
			lead = leadRepo.find(Long.parseLong(((Map) context.get("_lead")).get("id").toString()));
		}
		AppBase appBase = appBaseService.getAppBase();
		response.setAttr("isContact", "value", false);
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
		response.setAttr("isProspect", "value", true);
		response.setAttr("partnerTypeSelect", "value", "1");
		response.setAttr("language", "value", appBase.getDefaultPartnerLanguage());
	}
	
	public void setConvertLeadIntoOpportunity(ActionRequest request, ActionResponse response) { 
		Context context = request.getContext();
		Lead lead;
		if (context.getParent() != null && context.getParent().get("_model").equals("com.axelor.apps.base.db.Wizard")) {
			lead = leadRepo.find(Long.parseLong(((Map) context.getParent().get("_lead")).get("id").toString()));
		} else {
			lead = leadRepo.find(Long.parseLong(((Map) context.get("_lead")).get("id").toString()));
		}
		AppBase appBase = appBaseService.getAppBase();
		Company company = companyRepo.all().fetchOne();
		Long noOfCompany = companyRepo.all().count();
		response.setAttr("lead", "value", lead);
		response.setAttr("amount", "value", lead.getOpportunityAmount());
		response.setAttr("description", "value", lead.getDescription());
		response.setAttr("source", "value", lead.getSource());
		response.setAttr("user", "value", lead.getUser());
		response.setAttr("team", "value", lead.getTeam());
		response.setAttr("salesStageSelect", "value", "1");
		response.setAttr("webSite", "value", lead.getWebSite());
		response.setAttr("source", "value", lead.getSource());
		response.setAttr("department", "value", lead.getDepartment());
		response.setAttr("team", "value", lead.getTeam());
		response.setAttr("isCustomer", "value", true);
		response.setAttr("partnerTypeSelect", "value", "1");
		response.setAttr("language", "value", appBase.getDefaultPartnerLanguage());
		if(lead.getUser() != null) {
			if(lead.getUser().getActiveCompany() != null) {
				response.setAttr("company", "value", lead.getUser().getActiveCompany());
				response.setAttr("currency", "value",lead.getUser().getActiveCompany().getCurrency());
			} else if(noOfCompany == 1){
				response.setAttr("company", "value", company);
				response.setAttr("currency", "value",company);
			}
			
		} else if(noOfCompany == 1) {
			response.setAttr("company", "value", company);
			response.setAttr("currency", "value",company);
		}
	}
	
	public void setConvertLeadWizardAddress(ActionRequest request, ActionResponse response) { 
		Context context = request.getContext();
		Lead lead;
		if (context.getParent() != null && context.getParent().get("_model").equals("com.axelor.apps.base.db.Wizard")) {
			lead = leadRepo.find(Long.parseLong(((Map) context.getParent().get("_lead")).get("id").toString()));
		} else {
			lead = leadRepo.find(Long.parseLong(((Map) context.get("_lead")).get("id").toString()));
		}
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
	}
	
	public void setConvertLeadCallEvent(ActionRequest request, ActionResponse response) { 
		Context context = request.getContext();
		Lead lead;
		if (context.getParent() != null && context.getParent().get("_model").equals("com.axelor.apps.base.db.Wizard")) {
			lead = leadRepo.find(Long.parseLong(((Map) context.getParent().get("_lead")).get("id").toString()));
		} else {
			lead = leadRepo.find(Long.parseLong(((Map) context.get("_lead")).get("id").toString()));
		}
		response.setAttr("typeSelect", "value", "1");
		response.setAttr("lead", "value", lead);
		response.setAttr("description", "value", lead.getDescription());
		response.setAttr("user", "value", lead.getUser());
		response.setAttr("team", "value", lead.getTeam());
		response.setAttr("statusSelect", "value", "1");
		response.setAttr("startDateTime", "value", LocalDateTime.now());
		response.setAttr("duration", "value", 0);
		response.setAttr("callTypeSelect", "value", "2");
	}
	
	public void setConvertLeadMeetingEvent(ActionRequest request, ActionResponse response) { 
		Context context = request.getContext();
		Lead lead;
		if (context.getParent() != null && context.getParent().get("_model").equals("com.axelor.apps.base.db.Wizard")) {
			lead = leadRepo.find(Long.parseLong(((Map) context.getParent().get("_lead")).get("id").toString()));
		} else {
			lead = leadRepo.find(Long.parseLong(((Map) context.get("_lead")).get("id").toString()));
		}
		response.setAttr("typeSelect", "value", "2");
		response.setAttr("lead", "value", lead);
		response.setAttr("description", "value", lead.getDescription());
		response.setAttr("user", "value", lead.getUser());
		response.setAttr("team", "value", lead.getTeam());
		response.setAttr("statusSelect", "value", "1");
		response.setAttr("startDateTime", "value", LocalDateTime.now());
		response.setAttr("duration", "value", 0);
	}

	public void setConvertLeadTaskEvent(ActionRequest request, ActionResponse response) { 
		Context context = request.getContext();
		Lead lead;
		if (context.getParent() != null && context.getParent().get("_model").equals("com.axelor.apps.base.db.Wizard")) {
			lead = leadRepo.find(Long.parseLong(((Map) context.getParent().get("_lead")).get("id").toString()));
		} else {
			lead = leadRepo.find(Long.parseLong(((Map) context.get("_lead")).get("id").toString()));
		}
		response.setAttr("typeSelect", "value", "3");
		response.setAttr("lead", "value", lead);
		response.setAttr("description", "value", lead.getDescription());
		response.setAttr("user", "value", lead.getUser());
		response.setAttr("team", "value", lead.getTeam());
		response.setAttr("statusSelect", "value", "11");
		response.setAttr("startDateTime", "value", LocalDateTime.now());
		response.setAttr("progressSelect", "value", 0);
		response.setAttr("duration", "value", 0);
	}

}
