package com.axelor.apps.crm.web

import groovy.util.logging.Slf4j

import com.axelor.apps.base.db.Address
import com.axelor.apps.base.db.Partner
import com.axelor.apps.crm.db.Event
import com.axelor.apps.crm.db.Lead
import com.axelor.apps.crm.db.Opportunity
import com.axelor.apps.crm.service.LeadService

import com.axelor.exception.AxelorException
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.axelor.rpc.Context
import com.google.inject.Inject


@Slf4j
public class ConvertLeadWizardController {

	@Inject
	private LeadService leadService
	
//	def convertLead(ActionRequest request, ActionResponse response) {
//		
//		Lead lead = request.context as Lead
//		
//		Partner partner = leadService.convertLead(lead);
//		
//	}
//  
	
	def convertLead(ActionRequest request, ActionResponse response) {
		
		Context context = request.context
		
		int leadId = context._lead.id
		Lead lead = Lead.find(leadId)
		
		Partner partner = null
		Partner contactPartner = null
		Opportunity opportunity = null
		Event callEvent = null
		Event meetingEvent = null
		
		if(context.hasConvertIntoContact)  {
			contactPartner = this.createPartner(context, true)
		}
		else  if(context.selectContact)  {
			contactPartner =  Partner.find(context.selectContactPartner)
		}
		
		if(context.hasConvertIntoPartner)  {
			partner = this.createPartner(context, false)
		}
		else  if(context.selectPartner)  {
			partner = Partner.find(context.selectPartner)
		}
		
		if(context.hasConvertIntoOpportunity)  {
			opportunity = this.createOpportunity(context)
		}
		if(context.hasConvertIntoCall)  {
			callEvent = this.createEvent(context, false)
		}
		if(context.hasConvertIntoMeeting)  {
			meetingEvent = this.createEvent(context, true)
		}
		
		leadService.convertLead(lead, partner, contactPartner, opportunity, callEvent, meetingEvent);
		
//		response.reload = true
		
//		if(partner != null)  {
//			response.flash = "Lead converted"
//		}
//		else  {
//			response.flash = "Lead not converted"
//		}
		
		// Display partner on second page ?
	}
	
	
	/**
	 * Create a partner from a lead
	 * @param lead
	 * @return
	 * @throws AxelorException
	 */
	public Partner createPartner(Context context, boolean isContact) throws AxelorException  {
		
		Partner partner = new Partner();
		
		def contextPartner = null
		if(isContact)  {
			contextPartner = context.contactPartner
		}
		else  {
			contextPartner = context.partner
		}
		if(contextPartner != null)  {
		log.debug("ContextPartner"+contextPartner)
		log.debug("partnerTypeSelect"+contextPartner.partnerTypeSelect)
			partner.firstName = contextPartner.firstName;
			partner.name = contextPartner.name;
			partner.titleSelect = contextPartner.titleSelect;
			partner.customerTypeSelect = contextPartner.customerTypeSelect;
			partner.partnerTypeSelect = contextPartner.partnerTypeSelect
			partner.isContact = contextPartner.isContact;
			partner.email = contextPartner.email;
			partner.fax = contextPartner.fax;
			partner.webSite = contextPartner.webSite;
			partner.mobilePhonePro = contextPartner.mobilePhonePro;
			partner.source = contextPartner.source;
			partner.department = contextPartner.department;
			partner.picture = contextPartner.picture
			partner.mainInvoicingAddress = contextPartner.mainInvoicingAddress
			partner.deliveryAddress = contextPartner.deliverymainInvoicingAddress
			Address deliveryAddress = new Address()
			deliveryAddress.addressL4 = contextPartner.deliveryAddress.addressL4
			partner.deliveryAddress = deliveryAddress
			partner.bankDetails = context.bankDetails
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
	public Opportunity createOpportunity(Context context) throws AxelorException  {

		def contextOpportunity = context.opportunity
				
		if(contextOpportunity)  {
			Opportunity opportunity = new Opportunity();
			
			opportunity.amount = contextOpportunity.amount
			opportunity.campaign = contextOpportunity.campaign
			opportunity.company = contextOpportunity.company
			opportunity.bestCase = contextOpportunity.bestCase
			opportunity.currency = contextOpportunity.currency
			opportunity.description = contextOpportunity.description
			opportunity.expectedCloseDate = contextOpportunity.expectedCloseDate
			opportunity.name = contextOpportunity.name
			opportunity.nextStep = contextOpportunity.nextStep
			opportunity.opportunityType = contextOpportunity.opportunityType
			opportunity.partner = contextOpportunity.partner
			opportunity.probability = contextOpportunity.probability
			opportunity.salesStageSelect = contextOpportunity.salesStageSelect
			opportunity.source = contextOpportunity.source
			opportunity.team = contextOpportunity.team
			opportunity.userInfo = contextOpportunity.userInfo
			opportunity.worstCase = contextOpportunity.worstCase
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
	public Event createEvent(Context context, boolean isMeeting) throws AxelorException  {
		
		def contextEvent = null
		if(isMeeting)  {
			contextEvent = context.meetingEvent
		}
		else  {
			contextEvent = context.callEvent
		}
				
		if(contextEvent)  {
			Event event = new Event();
			
			event.description = context.description
			event.durationHours = context.durationHours
			event.durationMinutesSelect = context.durationMinutesSelect
			event.endDateTime = context.endDateTime
			event.eventCategory = context.eventCategory
			event.isTimesheetAffected = context.isTimesheetAffected
			event.location = context.location
			event.meetingType = context.meetingType
			event.primaryStatusSelect = context.primaryStatusSelect
			event.priority = context.priority
			event.prioritySelect = context.prioritySelect
			event.progressSelect = context.progressSelect
			event.project = context.project
			event.quaternaryStatusSelect = context.quaternaryStatusSelect
			event.relatedToSelect = context.relatedToSelect
			event.relatedToSelectId = context.relatedToSelectId
			event.reminder = context.reminder
			event.responsibleUserInfo = context.responsibleUserInfo
			event.secondaryStatusSelect = context.secondaryStatusSelect
			event.startDateTime = context.startDateTime
			event.subject = context.subject
			event.task = context.task
			event.taskPartner = context.taskPartner
			event.team = context.team
			event.tertiaryStatusSelect = context.tertiaryStatusSelect
			event.ticketNumberSeq = context.ticketNumberSeq
			event.typeSelect = context.typeSelect
			event.userInfo = context.userInfo
			return event;
		}
		// add others
		return null;
	}
	
	
	
	
	
}
