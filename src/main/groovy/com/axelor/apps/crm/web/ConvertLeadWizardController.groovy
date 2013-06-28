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
		Event taskEvent = null
		
		if(context.hasConvertIntoContact)  {
			contactPartner = this.createPartner(context.contactPartner)
		}
		else  if(context.selectContact)  {
			contactPartner =  Partner.find(context.selectContactPartner)
		}
		
		if(context.hasConvertIntoPartner)  {
			partner = this.createPartner(context.partner)
		}
		else  if(context.selectPartner)  {
			partner = Partner.find(context.selectPartner)
		}
		
		if(context.hasConvertIntoOpportunity)  {
			opportunity = this.createOpportunity( context.opportunity)
		}
		if(context.hasConvertIntoCall)  {
			callEvent = this.createEvent(context.callEvent, 1)
		}
		if(context.hasConvertIntoMeeting)  {
			meetingEvent = this.createEvent(context.meetingEvent, 2)
		}
		if(context.hasConvertIntoTask)  {
			taskEvent = this.createEvent(context.taskEvent, 3)
		}
		
		leadService.convertLead(lead, partner, contactPartner, opportunity, callEvent, meetingEvent, taskEvent);
		
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
	public Partner createPartner(def context) throws AxelorException  {
		
		Partner partner = new Partner();
		
		if(context != null)  {
		log.debug("ContextPartner"+context)
		log.debug("partnerTypeSelect"+context.partnerTypeSelect)
			partner.firstName = context.firstName;
			partner.name = context.name;
			partner.titleSelect = context.titleSelect;
			partner.customerTypeSelect = context.customerTypeSelect;
			partner.partnerTypeSelect = context.partnerTypeSelect
			partner.isContact = context.isContact;
			partner.email = context.email;
			partner.fax = context.fax;
			partner.webSite = context.webSite;
			partner.mobilePhonePro = context.mobilePhonePro;
			partner.source = context.source;
			partner.department = context.department;
			partner.picture = context.picture
//			partner.mainInvoicingAddress = context.mainInvoicingAddress
//			partner.deliveryAddress = context.deliverymainInvoicingAddress
//			Address deliveryAddress = new Address()
//			deliveryAddress.addressL4 = context.deliveryAddress.addressL4
//			partner.deliveryAddress = deliveryAddress
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
	public Opportunity createOpportunity(def context) throws AxelorException  {

		if(context)  {
			Opportunity opportunity = new Opportunity();
			
			opportunity.amount = new BigDecimal(context.amount)
			opportunity.campaign = context.campaign
			opportunity.company = context.company
			opportunity.bestCase = context.bestCase
			opportunity.currency = context.currency
			opportunity.description = context.description
			opportunity.expectedCloseDate = context.expectedCloseDate
			opportunity.name = context.name
			opportunity.nextStep = context.nextStep
			opportunity.opportunityType = context.opportunityType
			opportunity.partner = context.partner
			opportunity.probability = context.probability
			opportunity.salesStageSelect = context.salesStageSelect
			opportunity.source = context.source
			opportunity.team = context.team
			opportunity.userInfo = context.userInfo
			opportunity.worstCase = context.worstCase
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
	public Event createEvent(def context, int type) throws AxelorException  {
		
				
		if(context)  {
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
			event.typeSelect = type
			
			return event;
		}
		// add others
		return null;
	}
	
	
	
	
	
}
