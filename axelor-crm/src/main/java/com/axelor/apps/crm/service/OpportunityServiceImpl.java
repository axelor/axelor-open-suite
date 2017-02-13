package com.axelor.apps.crm.service;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class OpportunityServiceImpl implements OpportunityService {
	
	@Inject
	protected OpportunityRepository opportunityRepo;
	
	@Inject
	protected AddressService addressService;

	@Transactional
	public void saveOpportunity(Opportunity opportunity){
		opportunityRepo.save(opportunity);
	}

	@Override
	@Transactional
	public Partner createClientFromLead(Opportunity opportunity) throws AxelorException{
		Lead lead = opportunity.getLead();
		if(lead == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.LEAD_PARTNER)),IException.CONFIGURATION_ERROR);
		}

		String name = lead.getCompanyName();
		if(Strings.isNullOrEmpty(name)){
			name = lead.getFullName();
		}

		Address address = null;
		if (lead.getPrimaryAddress() != null) {
			// avoids printing 'null'
			String addressL6 = lead.getPrimaryPostalCode() == null ? "" : lead.getPrimaryPostalCode() + " ";
			addressL6 += lead.getPrimaryCity() == null ? "" : lead.getPrimaryCity();

			address = addressService.createAddress(null, null, lead.getPrimaryAddress(), null, addressL6, lead.getPrimaryCountry());
			address.setFullName(addressService.computeFullName(address));
		}

		Partner partner = Beans.get(PartnerService.class).createPartner(name, null, lead.getFixedPhone(), lead.getMobilePhone(), lead.getEmailAddress(), opportunity.getCurrency(), address, address);

		opportunity.setPartner(partner);
		opportunityRepo.save(opportunity);

		return partner;
	}
	
}
