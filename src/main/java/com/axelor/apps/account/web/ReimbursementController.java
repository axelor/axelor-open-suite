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
		rs.updateContractCurrentRIB(reimbursement);
		
		if (reimbursement.getBankDetails() != null) {
			response.setValue("status", Status.all().filter("self.code = 'val'").fetchOne());
		}
		else {
			response.setFlash("Vous devez configurer un RIB");
		}
	}
}
