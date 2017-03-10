package com.axelor.apps.bank.payment.ebics.web;

import com.axelor.apps.bank.payment.db.EbicsPartner;
import com.axelor.apps.bank.payment.db.repo.EbicsPartnerRepository;
import com.axelor.apps.bank.payment.ebics.service.EbicsPartnerService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class EbicsPartnerController {
	
	@Inject
	EbicsPartnerService ebicsPartnerService;
	
	@Inject
	EbicsPartnerRepository ebicsPartnerRepository;
	
	public void getBankStatement(ActionRequest request, ActionResponse response )  {
		
		try {
			EbicsPartner ebicsPartner = request.getContext().asType(EbicsPartner.class);
		
			ebicsPartnerService.getBankStatements(ebicsPartnerRepository.find(ebicsPartner.getId()));
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
		
	}
	
}
