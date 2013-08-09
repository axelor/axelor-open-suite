package com.axelor.apps.account.web;

import com.axelor.apps.base.db.Mail;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.MailService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ReminderController {

	@Inject
	private Provider<MailService> ms;

	public void ReminderGenerate(ActionRequest request, ActionResponse response) {

		try {			
			Partner partner = request.getContext().asType(Partner.class);
			partner = Partner.find(partner.getId());

			MailService mailService = ms.get();
			for(Mail mail : mailService.getMailList(partner))  {
				mailService.generatePdfMail(mail);
			}
			response.setReload(true);			
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
