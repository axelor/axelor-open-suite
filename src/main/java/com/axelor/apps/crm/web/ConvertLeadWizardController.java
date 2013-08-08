package com.axelor.apps.crm.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

public class ConvertLeadWizardController {

	@Inject
	private LeadService leadService;
	
	private static final Logger LOG = LoggerFactory.getLogger(ConvertLeadWizardController.class);
	
	public void convertLead(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Context context = request.getContext();
		
		Lead leadContext = (Lead) context.get("_lead");
		Lead lead = Lead.find(leadContext.getId());
		
		Partner partner = null;
		Partner contactPartner = null;
		Opportunity opportunity = null;
		Event callEvent = null;
		Event meetingEvent = null;
		Event taskEvent = null;
		
		if((Boolean) context.get("hasConvertIntoContact")) {
			contactPartner = this.createPartner((Partner) context.get("contactPartner"));
		}
		else  if(context.get("selectContact") != null) {
			contactPartner = Partner.find((Long) context.get("selectContactPartner"));
		}
		
		if((Boolean) context.get("hasConvertIntoPartner")) {
			partner = this.createPartner((Partner) context.get("partner"));
		}
		else  if(context.get("selectPartner") != null) {
			partner = Partner.find((Long) context.get("selectPartner"));
		}
		
		if((Boolean) context.get("hasConvertIntoOpportunity")) {
			opportunity = this.createOpportunity((Opportunity) context.get("opportunity"));
		}
		if((Boolean) context.get("hasConvertIntoCall")) {
			callEvent = this.createEvent((Event) context.get("callEvent"), 1);
		}
		if((Boolean) context.get("hasConvertIntoMeeting")) {
			meetingEvent = this.createEvent((Event) context.get("meetingEvent"), 2);
		}
		if((Boolean) context.get("hasConvertIntoTask")) {
			taskEvent = this.createEvent((Event) context.get("taskEvent"), 3);
		}
		
		leadService.convertLead(lead, partner, contactPartner, opportunity, callEvent, meetingEvent, taskEvent);
	}
	
	/**
	 * Create a partner from a lead
	 * @param lead
	 * @return
	 * @throws AxelorException
	 */
	public Partner createPartner(Partner context) throws AxelorException  {
		
		Partner partner = new Partner();
		
		if(context != null)  {
			LOG.debug("ContextPartner"+context);
			LOG.debug("partnerTypeSelect"+context.getPartnerTypeSelect());
			partner.setFirstName(context.getFirstName());
			partner.setName(context.getName());
			partner.setTitleSelect(context.getTitleSelect());
			partner.setCustomerTypeSelect(context.getCustomerTypeSelect());
			partner.setPartnerTypeSelect(context.getPartnerTypeSelect());
			partner.setIsContact(context.getIsContact());
			partner.setEmail(context.getEmail());
			partner.setFax(context.getFax());
			partner.setWebSite(context.getWebSite());
			partner.setMobilePhonePro(context.getMobilePhonePro());
			partner.setSource(context.getSource());
			partner.setDepartment(context.getDepartment());
			partner.setPicture(context.getPicture());
			partner.setBankDetails(context.getBankDetails());
			partner.setPartnerSeq(leadService.getSequence());
		}	
		// add others
		return partner;
	}
	
	/**
	 * Create an opportunity from a lead
	 * @param lead
	 * @return
	 * @throws AxelorException
	 */
	public Opportunity createOpportunity(Opportunity context) throws AxelorException  {

		if(context != null)  {
			Opportunity opportunity = new Opportunity();
			
			opportunity.setAmount(context.getAmount()); 
			opportunity.setCampaign(context.getCampaign());
			opportunity.setCompany(context.getCompany());
			opportunity.setBestCase(context.getBestCase());
			opportunity.setCurrency(context.getCurrency());
			opportunity.setDescription(context.getDescription());
			opportunity.setExpectedCloseDate(context.getExpectedCloseDate());
			opportunity.setName(context.getName());
			opportunity.setNextStep(context.getNextStep());
			opportunity.setOpportunityType(context.getOpportunityType());
			opportunity.setPartner(context.getPartner());
			opportunity.setProbability(context.getProbability());
			opportunity.setSalesStageSelect(context.getSalesStageSelect());
			opportunity.setSource(context.getSource());
			opportunity.setTeam(context.getTeam());
			opportunity.setUserInfo(context.getUserInfo());
			opportunity.setWorstCase(context.getWorstCase());
			return opportunity;
		}
		// add others
		return null;
	}
	
	/**
	 * Create an event from a lead (Call or Meeting)
	 * @param lead
	 * @return
	 * @throws AxelorException
	 */
	public Event createEvent(Event context, int type) throws AxelorException  {
		
		if(context != null)  {
			Event event = new Event();
			
			event.setDescription(context.getDescription());
			event.setDurationHours(context.getDurationHours());
			event.setDurationMinutesSelect(context.getDurationMinutesSelect());
			event.setEndDateTime(context.getEndDateTime());
			event.setEventCategory(context.getEventCategory());
			event.setIsTimesheetAffected(context.getIsTimesheetAffected());
			event.setLocation(context.getLocation());
			event.setMeetingType(context.getMeetingType());
			event.setPrioritySelect(context.getPrioritySelect());
			event.setProgressSelect(context.getProgressSelect());
			event.setProject(context.getProject());
			event.setRelatedToSelect(context.getRelatedToSelect());
			event.setRelatedToSelectId(context.getRelatedToSelectId());
			event.setReminder(context.getReminder());
			event.setResponsibleUserInfo(context.getResponsibleUserInfo());
			event.setStartDateTime(context.getStartDateTime());
			event.setSubject(context.getSubject());
			event.setTask(context.getTask());
			event.setTaskPartner(context.getTaskPartner()); 
			event.setTeam(context.getTeam());
			event.setTicketNumberSeq(context.getTicketNumberSeq());
			event.setTypeSelect(context.getTypeSelect());
			event.setUserInfo(context.getUserInfo());
			event.setTypeSelect(type);
			
			return event;
		}
		// add others
		return null;
	}
}
