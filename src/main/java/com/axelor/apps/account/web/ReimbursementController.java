/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.service.ReimbursementService;
import com.axelor.apps.base.db.Status;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ReimbursementController {

	@Inject
	private ReimbursementService rs;
	
	public void validateReimbursement(ActionRequest request, ActionResponse response) {
		
		Reimbursement reimbursement = request.getContext().asType(Reimbursement.class);
		rs.updatePartnerCurrentRIB(reimbursement);
		
		if (reimbursement.getBankDetails() != null) {
			response.setValue("status", Status.all().filter("self.code = 'val'").fetchOne());
		}
		else {
			response.setFlash("Vous devez configurer un RIB");
		}
	}
}
