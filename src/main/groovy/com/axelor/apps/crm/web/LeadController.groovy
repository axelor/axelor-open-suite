package com.axelor.apps.crm.web

import groovy.util.logging.Slf4j

import com.axelor.apps.base.db.Partner
import com.axelor.apps.crm.db.Lead
import com.axelor.apps.crm.service.LeadService
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.axelor.rpc.Context;
import com.google.inject.Inject


@Slf4j
public class LeadController {

	@Inject
	private LeadService leadService
	
	def convertLead(ActionRequest request, ActionResponse response) {
		
		Lead lead = request.context._lead
		lead = Lead.find(lead.id)
		
		Partner partner = leadService.convertLead(lead, this.createPartner(request.context));
		
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
	public Partner createPartner(Partner contextPartner) throws AxelorException  {
		
		
		
		Partner partner = new Partner();
		
		partner.setFirstName(contextPartner.firstName);
		partner.setName(contextPartner.name);
		partner.setTitleSelect(contextPartner.titleSelect);
		partner.setCustomerTypeSelect(contextPartner.customerTypeSelect);
		partner.setPartnerTypeSelect(contextPartner.partnerTypeSelect);
		partner.setIsContact(contextPartner.isContact);
		partner.setEmail(contextPartner.email);
		partner.setFax(contextPartner.fax);
		partner.setWebSite(contextPartner.webSite);
		partner.setMobilePhonePro(contextPartner.mobilePhonePro);
		partner.setSource(contextPartner.source);
		partner.setDepartment(contextPartner.department);
		
//		partner.setPartnerSeq(this.getSequence());
		
//		this.setPartnerCompany(partner);
		
		
		// add others
		return partner;
	} 
  
}
