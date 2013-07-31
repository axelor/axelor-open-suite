package com.axelor.apps.organisation.web;

import com.axelor.apps.organisation.db.OvertimeLine;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class OvertimeLineController {

	public void computeTotal(ActionRequest request, ActionResponse response) {
		
		OvertimeLine overtimeLine = request.getContext().asType(OvertimeLine.class);
		
		if (overtimeLine.getQuantity() != null && overtimeLine.getUnitPrice() != null) {
			response.setValue("total", overtimeLine.getQuantity().multiply(overtimeLine.getUnitPrice()));
		}
		else {
			response.setValue("total", 0.00);
		}
	}
}
