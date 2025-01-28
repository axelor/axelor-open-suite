package com.axelor.apps.hr.service.allocation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.hr.db.AllocationLine;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;
import java.math.BigDecimal;

public interface AllocationLineComputeService {
  BigDecimal getLeaves(Period period, Employee employee) throws AxelorException;

  BigDecimal getAlreadyAllocated(AllocationLine allocationLine, Period period, Employee employee);

  BigDecimal getAvailableAllocation(
      Period period, Employee employee, BigDecimal leaves, BigDecimal alreadyAllocated);

  BigDecimal computePlannedTime(Period period, Employee employee, Project project)
      throws AxelorException;
}
