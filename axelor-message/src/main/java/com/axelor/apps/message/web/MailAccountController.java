/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
		String validationError = mailAccountService.validateSmtpMailAccount(account);
		if(validationError == null){
			response.setValue("isValid",new Boolean(true));
			response.setFlash("Connection successful");
			response.setReadonly("loginPanel", true);
			response.setReadonly("configPanel",true);
		}else{
			response.setFlash("Provided settings are wrong, please modify them and try again<br/>Error:"+validationError);
			response.setValue("isValid",new Boolean(false));
		}
		
	}

}
