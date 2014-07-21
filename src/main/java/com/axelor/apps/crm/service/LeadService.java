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
package com.axelor.apps.crm.service;

import java.util.HashSet;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.ILead;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class LeadService {

	@Inject
	private SequenceService sequenceService;
	
	@Inject
	private UserInfoService userInfoService;
	
	
	/**
	 * Convert lead into a partner
	 * @param lead
	 * @return
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Lead convertLead(Lead lead, Partner partner, Partner contactPartner, Opportunity opportunity, Event callEvent, Event meetingEvent, Event taskEvent) throws AxelorException  {
		
//		lead.setEvent(meeting);
//		lead.setCall(call);
//		lead.setOpportunity(opportunity);
//		lead.setContactPartner(contact);
		
		if(partner != null && contactPartner != null)  {
			if(partner.getContactPartnerSet()==null)  {
				partner.setContactPartnerSet(new HashSet<Partner>());
			}
			partner.getContactPartnerSet().add(contactPartner);
		}
		
		if(opportunity != null && partner != null)  {
			opportunity.setPartner(partner);
		}
		
		if(partner != null)  {
			lead.setPartner(partner);
			partner.save();
		}
		if(contactPartner!=null)  {
			contactPartner.save();
		}
		if(opportunity!=null)  {
			opportunity.save();
		}
		if(callEvent!=null)  {
			callEvent.save();
		}
		if(meetingEvent!=null)  {
			meetingEvent.save();
		}
		if(taskEvent!=null)  {
			taskEvent.save();
		}
		
		lead.setPartner(partner);
		lead.setStatusSelect(ILead.STATUS_CONVERTED);
		lead.save();
		
		return lead;
		
	}
	
	
	/**
	 * Get sequence for partner
	 * @return
	 * @throws AxelorException
	 */
	public String getSequence() throws AxelorException  {
		
		String seq = sequenceService.getSequenceNumber(IAdministration.PARTNER);
		if (seq == null)  {
			throw new AxelorException("Aucune séquence configurée pour les tiers",
							IException.CONFIGURATION_ERROR);
		}
		return seq;
	}
	
	
	/**
	 * Assign user company to partner
	 * @param partner
	 * @return
	 */
	public Partner setPartnerCompany(Partner partner)  {
		
//		UserInfoService userInfoService = new UserInfoService();
		
		if(userInfoService.getUserActiveCompany() != null)  {
			partner.setCompanySet(new HashSet<Company>());
			partner.getCompanySet().add(userInfoService.getUserActiveCompany());
		}
		
		return partner;
	}
	
}
