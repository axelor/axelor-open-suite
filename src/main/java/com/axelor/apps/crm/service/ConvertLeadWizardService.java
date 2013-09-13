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
package com.axelor.apps.crm.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.wizard.ConvertWizardService;
import com.axelor.apps.crm.db.Event;
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
	public Event createEvent(Map<String, Object> context) throws AxelorException  {
		
		Mapper mapper = Mapper.of(Event.class);
		Event event = Mapper.toBean(Event.class, null);
		
		event = (Event) convertWizardService.createObject(context, event, mapper);
		
		return event;
	}
	
	
}
