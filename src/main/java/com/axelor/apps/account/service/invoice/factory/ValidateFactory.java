package com.axelor.apps.account.service.invoice.factory;

import javax.inject.Inject;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.workflow.validate.ValidateState;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.base.service.user.UserInfoService;

public class ValidateFactory {
	
	@Inject
	private UserInfoService userInfoService;
	
	@Inject
	private AlarmEngineService<Invoice> alarmEngineService;

	public ValidateState getValidator(Invoice invoice){
		
		return new ValidateState(userInfoService, invoice);
		
	}
	
}
