package com.axelor.apps.cash.management.web;

import com.axelor.apps.cash.management.db.CashManagementRecap;
import com.axelor.apps.cash.management.exception.IExceptionMessage;
import com.axelor.apps.cash.management.service.CashManagementRecapService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class CashManagementRecapController {
	
	@Inject
	protected CashManagementRecapService cashManagementRecapService;
	
	public void populate(ActionRequest request, ActionResponse response) throws AxelorException{
		CashManagementRecap cashManagementRecap = request.getContext().asType(CashManagementRecap.class);
		if(cashManagementRecap.getCompany() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.CASH_MGT_COMPANY)), IException.CONFIGURATION_ERROR);
		}
		cashManagementRecapService.populate(cashManagementRecap);
		response.setValues(cashManagementRecap);
		
	}
		
}
