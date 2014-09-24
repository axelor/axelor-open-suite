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
package com.axelor.apps.account.web;

import com.axelor.apps.account.service.MailService;
import com.axelor.apps.base.db.Mail;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ReminderController {

	@Inject
	private Provider<MailService> mailProvider;
	
	@Inject
	private PartnerService partnerService;

	public void ReminderGenerate(ActionRequest request, ActionResponse response) {

		try {			
			Partner partner = request.getContext().asType(Partner.class);
			partner = partnerService.find(partner.getId());

			MailService mailService = mailProvider.get();
			for(Mail mail : mailService.getMailList(partner))  {
				mailService.generatePdfMail(mail);
			}
			response.setReload(true);			
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
