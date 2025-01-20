package com.axelor.apps.hr.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.AllocationLine;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.service.allocation.AllocationLineComputeService;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.util.Map;

public class AllocationLineManagementRepository extends AllocationLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    AllocationLine allocationLine = find((Long) json.get("id"));
    Period period = allocationLine.getPeriod();
    Employee employee = allocationLine.getEmployee();
    BigDecimal leaves = BigDecimal.ZERO;
    BigDecimal alreadyAllocated = BigDecimal.ZERO;
    BigDecimal availableAllocation = BigDecimal.ZERO;
    BigDecimal plannedTime = BigDecimal.ZERO;
    AllocationLineComputeService allocationLineComputeService =
        Beans.get(AllocationLineComputeService.class);
    try {
      leaves = allocationLineComputeService.getLeaves(period, employee);
      alreadyAllocated =
          allocationLineComputeService.getAlreadyAllocated(allocationLine, period, employee);
      availableAllocation =
          allocationLineComputeService.getAvailableAllocation(
              period, employee, leaves, alreadyAllocated);
      plannedTime =
          allocationLineComputeService.computePlannedTime(
              period, employee, allocationLine.getProject());
    } catch (AxelorException e) {
      TraceBackService.trace(e);
    }
    json.put("$leaves", leaves);
    json.put("$alreadyAllocated", alreadyAllocated);
    json.put("$availableAllocation", availableAllocation);
    json.put("$plannedTime", plannedTime);
    return super.populate(json, context);
  }
}
