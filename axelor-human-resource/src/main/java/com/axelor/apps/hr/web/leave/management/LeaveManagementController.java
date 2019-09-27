/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.web.leave.management;

import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.service.leave.management.LeaveManagementService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class LeaveManagementController {

  @Inject protected LeaveManagementService leaveManagementService;

  public void computeQuantityAvailable(ActionRequest request, ActionResponse response) {
    LeaveLine leaveLine = request.getContext().asType(LeaveLine.class);
    leaveLine = leaveManagementService.computeQuantityAvailable(leaveLine);
    response.setValue("quantity", leaveLine.getQuantity());
    response.setValue("totalQuantity", leaveLine.getTotalQuantity());
    response.setValue("leaveManagementList", leaveLine.getLeaveManagementList());
  }
}
