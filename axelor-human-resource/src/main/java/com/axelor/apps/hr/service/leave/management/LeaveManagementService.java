package com.axelor.apps.hr.service.leave.management;

import java.math.BigDecimal;
import java.util.List;

import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveManagement;

public class LeaveManagementService {
	
	public LeaveLine computeQuantityAvailable (LeaveLine leaveLine){
		List<LeaveManagement> leaveManagementList = leaveLine.getLeaveManagementList();
		leaveLine.setQuantity(BigDecimal.ZERO);
		if(leaveManagementList != null && !leaveManagementList.isEmpty()){
			for (LeaveManagement leaveManagement : leaveManagementList) {
				leaveLine.setQuantity(leaveLine.getQuantity().add(leaveManagement.getValue()));
			}
		}
		return leaveLine;
	}

}
