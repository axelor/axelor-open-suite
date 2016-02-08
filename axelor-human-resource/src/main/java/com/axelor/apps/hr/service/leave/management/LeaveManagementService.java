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
