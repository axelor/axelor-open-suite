package com.axelor.apps.message.web;

import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.service.MailAccountService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class MailAccountController {
	@Inject
	MailAccountService mailAccountService;
	public void validateSmtpAccount(ActionRequest request,ActionResponse response){
		MailAccount account=request.getContext().asType(MailAccount.class);
		boolean isValidAccount=mailAccountService.validateSmtpMailAccount(account);
		if(isValidAccount){
			response.setValue("isValid",new Boolean(true));
			response.setFlash("Connection successful");
			response.setReadonly("loginPanel", true);
			response.setReadonly("configPanel",true);
		}else{
			response.setFlash("Provided settings are wrong, please modify them and try again");
			response.setValue("isValid",new Boolean(false));
		}
		
	}

}
