package com.axelor.apps.account.service.invoice.workflow.validate;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.workflow.WorkflowInvoice;
import com.axelor.apps.base.db.Status;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.exception.AxelorException;

public class ValidateState extends WorkflowInvoice {
	
	protected UserInfo user;
	
	public ValidateState(UserInfoService userInfoService, Invoice invoice) {
		
		super(invoice);
		this.user = userInfoService.getUserInfo();
		
	}
	
	@Override
	public void process( ) throws AxelorException {
		
		invoice.setStatus( Status.all().filter("self.code = 'val'").fetchOne() );
		invoice.setValidatedByUserInfo( UserInfo.find( user.getId() ) );
		
	}
	
}