package com.axelor.apps.crm.service;

import java.util.HashSet;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.IPartner;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.apps.crm.db.Lead;
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
	public Partner convertLead(Lead lead) throws AxelorException  {
		
		Partner partner = this.createPartner(lead);
		lead.setPartner(partner);
		lead.setIsConvertedInPartner(true);
		lead.save();
		
		return partner;
		
	}
	
	
	/**
	 * Create a partner from a lead
	 * @param lead
	 * @return
	 * @throws AxelorException
	 */
	public Partner createPartner(Lead lead) throws AxelorException  {
		
		Partner partner = new Partner();
		partner.setFirstName(lead.getFirstName());
		partner.setName(lead.getName());
		partner.setTitleSelect(lead.getTitleSelect());
		partner.setCustomerTypeSelect(2);
		partner.setPartnerTypeSelect(IPartner.PARTNER_TYPE_SELECT_INDIVIDUAL);
		partner.setIsContact(true);
		partner.setEmail(lead.getEmail());
		partner.setFax(lead.getFax());
		partner.setWebSite(lead.getWebSite());
		partner.setMobilePhonePro(lead.getMobile());
		partner.setSource(lead.getSource());
		partner.setDepartment(lead.getDepartment());
		
		partner.setPartnerSeq(this.getSequence());
		
		
		if(lead.getEnterpriseName()!=null && !lead.getEnterpriseName().isEmpty())  {
			
			//TODO : create partner ?
		}
		
		// add others
		return partner;
	}
	
	
	/**
	 * Get sequence for partner
	 * @return
	 * @throws AxelorException
	 */
	public String getSequence() throws AxelorException  {
		
		String ref = sequenceService.getSequence(IAdministration.PARTNER,false);
		if (ref == null || ref.isEmpty())  {
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
