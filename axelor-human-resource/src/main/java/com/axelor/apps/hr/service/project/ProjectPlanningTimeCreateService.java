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
package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Site;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public interface ProjectPlanningTimeCreateService {

  void addMultipleProjectPlanningTime(Map<String, Object> datas) throws AxelorException;

  ProjectPlanningTime createProjectPlanningTime(
      LocalDateTime fromDate,
      ProjectTask projectTask,
      Project project,
      Integer timePercent,
      Employee employee,
      Product activity,
      BigDecimal dailyWorkHrs,
      LocalDateTime taskEndDateTime,
      Site site,
      Unit defaultTimeUnit)
      throws AxelorException;

  BigDecimal getOldBudgetedTime(ProjectTask projectTask);

  Unit getTimeUnit(ProjectTask projectTask);
}
