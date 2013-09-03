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
	
	
	/**
	 * Convert lead into a partner
	 * @param lead
	 * @return
	 * @throws AxelorException
	 */
	@Transactional
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
		
		String ref = sequenceService.getSequence(IAdministration.PARTNER,false);
		if (ref == null)  {
			throw new AxelorException("Aucune séquence configurée pour les tiers",
							IException.CONFIGURATION_ERROR);
		}
		return ref;
	}
	
	
	/**
	 * Assign user company to partner
	 * @param partner
	 * @return
	 */
	public Partner setPartnerCompany(Partner partner)  {
		
		UserInfoService userInfoService = new UserInfoService();
		
		if(userInfoService.getUserActiveCompany() != null)  {
			partner.setCompanySet(new HashSet<Company>());
			partner.getCompanySet().add(userInfoService.getUserActiveCompany());
		}
		
		return partner;
	}
	
}
