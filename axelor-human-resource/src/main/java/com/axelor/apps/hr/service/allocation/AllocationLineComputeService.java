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
package com.axelor.apps.hr.service.allocation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.hr.db.AllocationLine;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

  BigDecimal getAllocatedTime(
      Project project, LocalDate fromDate, LocalDate toDate, Employee employee);

  BigDecimal getBudgetedTime(Sprint sprint, Project project) throws AxelorException;

  BigDecimal getBudgetedTime(List<ProjectTask> projectTaskList, Project project)
      throws AxelorException;
}
