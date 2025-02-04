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
package com.axelor.apps.hr.service.allocation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;
import java.math.BigDecimal;
import java.util.List;

public interface AllocationLineService {

  String getEmployeeDomain(Project project);

  void addAllocationLines(
      Project project,
      List<Employee> employeeList,
      List<Period> periodList,
      BigDecimal allocated,
      boolean initWithPlanningTime)
      throws AxelorException;

  void removeAllocationLines(List<Integer> allocationLineIds);

  void createOrUpdateAllocationLine(
      Project project,
      Employee employee,
      Period period,
      BigDecimal allocated,
      boolean initWithPlanningTime)
      throws AxelorException;
}
