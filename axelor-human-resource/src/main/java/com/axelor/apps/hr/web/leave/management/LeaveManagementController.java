package com.axelor.apps.hr.web.leave.management;

import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.service.leave.management.LeaveManagementService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class LeaveManagementController {
	
	@Inject
	protected LeaveManagementService leaveManagementService;
	
	public void computeQuantityAvailable(ActionRequest request, ActionResponse response){
		LeaveLine leaveLine = request.getContext().asType(LeaveLine.class);
		leaveLine = leaveManagementService.computeQuantityAvailable(leaveLine);
		response.setValue("quantity",leaveLine.getQuantity());
		response.setValue("leaveManagementList",leaveLine.getLeaveManagementList());
	}
}
