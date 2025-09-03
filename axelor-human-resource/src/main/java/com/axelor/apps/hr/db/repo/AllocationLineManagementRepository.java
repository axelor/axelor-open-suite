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
package com.axelor.apps.hr.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.AllocationLine;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.service.allocation.AllocationLineComputeService;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class AllocationLineManagementRepository extends AllocationLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    AllocationLine allocationLine = find((Long) json.get("id"));
    Period period = allocationLine.getPeriod();
    Employee employee = allocationLine.getEmployee();

    if (period == null || employee == null) {
      return super.populate(json, context);
    }
    BigDecimal leaves = BigDecimal.ZERO;
    BigDecimal alreadyAllocated = BigDecimal.ZERO;
    BigDecimal availableAllocation = BigDecimal.ZERO;
    BigDecimal plannedTime = BigDecimal.ZERO;
    AllocationLineComputeService allocationLineComputeService =
        Beans.get(AllocationLineComputeService.class);
    try {
      leaves =
          allocationLineComputeService.getLeaves(
              period.getFromDate(), period.getToDate(), employee);
      alreadyAllocated =
          allocationLineComputeService.getAlreadyAllocated(allocationLine, period, employee);
      availableAllocation =
          allocationLineComputeService.getAvailableAllocation(
              period.getFromDate(), period.getToDate(), employee, leaves, alreadyAllocated);
      plannedTime =
          allocationLineComputeService.computePlannedTime(
              period.getFromDate(), period.getToDate(), employee, allocationLine.getProject());
    } catch (AxelorException e) {
      TraceBackService.trace(e);
    }
    json.put("$leaves", leaves);
    json.put("$alreadyAllocated", alreadyAllocated);
    json.put("$availableAllocation", availableAllocation);
    json.put(
        "$plannedTime",
        plannedTime.setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP));
    return super.populate(json, context);
  }
}
