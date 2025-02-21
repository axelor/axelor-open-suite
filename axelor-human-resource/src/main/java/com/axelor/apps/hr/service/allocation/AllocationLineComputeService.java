package com.axelor.apps.hr.service.allocation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.hr.db.AllocationLine;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.Sprint;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface AllocationLineComputeService {
  BigDecimal getLeaves(LocalDate fromDate, LocalDate toDate, Employee employee)
      throws AxelorException;

  BigDecimal getAlreadyAllocated(AllocationLine allocationLine, Period period, Employee employee);

  BigDecimal getAvailableAllocation(
      LocalDate fromDate,
      LocalDate toDate,
      Employee employee,
      BigDecimal leaves,
      BigDecimal alreadyAllocated);

  BigDecimal computePlannedTime(
      LocalDate fromDate, LocalDate toDate, Employee employee, Project project)
      throws AxelorException;

  BigDecimal computeSpentTime(
      LocalDate fromDate, LocalDate toDate, Employee employee, Project project)
      throws AxelorException;

  BigDecimal getAllocatedTime(Project project, Sprint sprint);

  BigDecimal getBudgetedTime(Sprint sprint, Project project) throws AxelorException;
}
