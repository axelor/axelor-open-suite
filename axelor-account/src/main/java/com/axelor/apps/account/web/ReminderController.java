/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ReminderController {

	@Inject
	private PartnerRepository partnerRepo;

	public void ReminderGenerate(ActionRequest request, ActionResponse response) {

		try {			
			Partner partner = request.getContext().asType(Partner.class);
			partner = partnerRepo.find(partner.getId());

//			MailService mailService = Beans.get(MailService.class);
//			for(Mail mail : mailService.getMailList(partner))  {
//				mailService.generatePdfMail(mail);
//			}
			response.setReload(true);			
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
