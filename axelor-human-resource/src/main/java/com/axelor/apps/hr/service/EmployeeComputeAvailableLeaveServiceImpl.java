/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import java.math.BigDecimal;
import org.apache.commons.collections.CollectionUtils;

public class EmployeeComputeAvailableLeaveServiceImpl
    implements EmployeeComputeAvailableLeaveService {

  @Override
  public BigDecimal computeAvailableLeaveQuantityForActiveUser(
      Employee employee, LeaveReason leaveReason) {
    if (employee == null
        || leaveReason == null
        || leaveReason.getLeaveReasonTypeSelect()
            == LeaveReasonRepository.TYPE_SELECT_EXCEPTIONAL_DAYS
        || CollectionUtils.isEmpty(employee.getLeaveLineList())) {
      return BigDecimal.ZERO;
    }

    return employee.getLeaveLineList().stream()
        .filter(ll -> ll.getLeaveReason().equals(leaveReason))
        .map(LeaveLine::getQuantity)
        .findFirst()
        .orElse(BigDecimal.ZERO);
  }
}
