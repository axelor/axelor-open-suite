/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.crm.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.wizard.ConvertWizardService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class ConvertLeadWizardService {

	@Inject
	private LeadService leadService;
	
	@Inject
	private ConvertWizardService convertWizardService;
	
	@Inject 
	private AddressService addressService;
	
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
		
		this.setEmailAddress(partner);
		
		partner.setPartnerSeq(leadService.getSequence());
		
		this.setAddress(partner, context);
		
		return partner;
	}
	
	
	public void setEmailAddress(Partner partner)  {
		
		EmailAddress emailAddress = partner.getEmailAddress();
		
		if(emailAddress != null)  {
			partner.setEmailAddress(this.createEmailAddress(emailAddress.getAddress(), null, partner));
		}
	}
	
	
	public void setAddress(Partner partner, Map<String, Object> context)  {
		
		if(partner.getIsContact())  {
			partner.setMainInvoicingAddress(this.createPrimaryAddress(context));
		}
		else  {
			partner.setMainInvoicingAddress(this.createPrimaryAddress(context));
			partner.setDeliveryAddress(this.createOtherAddress(context));
		}
		
	}
	
	
	public Address createPrimaryAddress(Map<String, Object> context)  {
		
		String addressL4 = (String) context.get("primaryAddress");
		String addressL5 = (String) context.get("primaryState");
		String addressL6 = (String) context.get("primaryPostalCode") + " "+ (String) context.get("primaryCity");;
		Country addressL7Country = null;
		Map<String, Object> countryContext = (Map<String, Object>) context.get("primaryCountry");
		if(countryContext!= null)  {
			addressL7Country = Country.find(((Integer) countryContext.get("id")).longValue());
		}
		
		Address address = addressService.getAddress(null, null, addressL4, addressL5, addressL6, addressL7Country);
		
		if(address == null)  {
			addressService.createAddress(null, null, addressL4, addressL5, addressL6, addressL7Country);
		}
		
		return address;
	}
		
	
	public Address createOtherAddress(Map<String, Object> context)  {
		
		String addressL4 = (String) context.get("otherAddress");
		String addressL5 = (String) context.get("otherState");
		String addressL6 = (String) context.get("otherPostalCode") + " "+ (String) context.get("otherCity");
		
		Country addressL7Country = null;
		Map<String, Object> countryContext = (Map<String, Object>) context.get("otherCountry");
		if(countryContext!= null)  {
			addressL7Country = Country.find(((Integer) countryContext.get("id")).longValue());
		}
		
		Address address = addressService.getAddress(null, null, addressL4, addressL5, addressL6, addressL7Country);
		
		if(address == null)  {
			addressService.createAddress(null, null, addressL4, addressL5, addressL6, addressL7Country);
		}
		
		return address;
	}
	
	
	
	public EmailAddress createEmailAddress(String address, Lead lead, Partner partner)  {
		EmailAddress emailAddress = new EmailAddress();
		emailAddress.setAddress(address);
		emailAddress.setLead(lead);
		emailAddress.setPartner(partner);
		
		return emailAddress;
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
