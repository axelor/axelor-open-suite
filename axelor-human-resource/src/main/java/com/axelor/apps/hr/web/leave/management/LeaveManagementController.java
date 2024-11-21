/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.web.leave.management;

import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.service.leave.management.LeaveManagementService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class LeaveManagementController {

  public void computeQuantityAvailable(ActionRequest request, ActionResponse response) {
    LeaveLine leaveLine = request.getContext().asType(LeaveLine.class);
    leaveLine = Beans.get(LeaveManagementService.class).computeQuantityAvailable(leaveLine);
    response.setValue("quantity", leaveLine.getQuantity());
    response.setValue("totalQuantity", leaveLine.getTotalQuantity());
    response.setValue("leaveManagementList", leaveLine.getLeaveManagementList());
  }
}
