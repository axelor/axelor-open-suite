package com.axelor.apps.crm.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.wizard.ConvertWizardService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.IEvent;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class ConvertLeadWizardService {

	@Inject
	private LeadService leadService;
	
	@Inject
	private ConvertWizardService convertWizardService;
	
	private static final Logger LOG = LoggerFactory.getLogger(ConvertLeadWizardService.class);
	
	
	/**
	 * Create a partner from a lead
	 * @param lead
	 * @return
	 * @throws AxelorException
	 */
	public Partner createPartner(Map<String, Object> context) throws AxelorException  {
		
		Mapper mapper = Mapper.of(Partner.class);
		Partner partner = Mapper.toBean(Partner.class, null);
		
		partner = (Partner) convertWizardService.createObject(context, partner, mapper);
		
		partner.setPartnerSeq(leadService.getSequence());
		
		return partner;
	}
	
	
	
	/**
	 * Create an opportunity from a lead
	 * @param lead
	 * @return
	 * @throws AxelorException
	 */
	public Opportunity createOpportunity(Map<String, Object> context) throws AxelorException  {
		
		Mapper mapper = Mapper.of(Opportunity.class);
		Opportunity opportunity = Mapper.toBean(Opportunity.class, null);
		
		opportunity = (Opportunity) convertWizardService.createObject(context, opportunity, mapper);
		
		return opportunity;
	}
	
	
	/**
	 * Create an event from a lead (Call, Task or Meeting)
	 * @param lead
	 * @return
	 * @throws AxelorException
	 */
	public Event createEvent(Map<String, Object> context, int type) throws AxelorException  {
		
		Mapper mapper = Mapper.of(Event.class);
		Event event = Mapper.toBean(Event.class, null);
		
		event = (Event) convertWizardService.createObject(context, event, mapper);
		
		event.setTypeSelect(type);
		if(type == IEvent.CALL || type == IEvent.MEETING)  {
			event.setStatusSelect(1);
		}
		else if (type == IEvent.TASK)  {
			event.setTaskStatusSelect(1);
		}
		
		return event;
	}
	
	
}
