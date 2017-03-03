package com.axelor.apps.bank.payment.ebics.web;

import com.axelor.apps.bank.payment.db.EbicsPartner;
import com.axelor.apps.bank.payment.ebics.service.EbicsPartnerService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class EbicsPartnerController {
	
	
	public void getBankStatement(ActionRequest request, ActionResponse response )  {
		
		try {
			EbicsPartner ebicsPartner = request.getContext().asType(EbicsPartner.class);
		
			Beans.get(EbicsPartnerService.class).getBankStatements(ebicsPartner);
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
		
	}
	
}
