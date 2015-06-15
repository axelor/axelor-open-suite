package com.axelor.apps.hr.service.leave.management;

import java.util.List;

import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveManagement;

public class LeaveManagementService {
	
	public LeaveLine computeQuantityAvailable (LeaveLine leaveLine){
		List<LeaveManagement> leaveManagementList = leaveLine.getLeaveManagementList();
		for (LeaveManagement leaveManagement : leaveManagementList) {
			if(!leaveManagement.getCounted()){
				leaveLine.setQuantity(leaveLine.getQuantity().subtract(leaveManagement.getOldValue()));
				leaveLine.setQuantity(leaveLine.getQuantity().add(leaveManagement.getValue()));
				leaveManagement.setOldValue(leaveManagement.getValue());
				leaveManagement.setCounted(true);
			}
		}
		
		return leaveLine;
	}

}
