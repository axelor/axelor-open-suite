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

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.service.allocation.AllocationLineComputeService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.SprintRepository;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.util.Map;

public class SprintManagementRepository extends SprintRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    try {
      if (context.get("project") == null) {
        return super.populate(json, context);
      }
      Long projectId = Long.valueOf(((Map) context.get("project")).get("id").toString());
      Project project = Beans.get(ProjectRepository.class).find(projectId);
      Long sprintId = (Long) json.get("id");
      Sprint sprint = this.find(sprintId);

      AllocationLineComputeService allocationLineComputeService =
          Beans.get(AllocationLineComputeService.class);

      BigDecimal allocatedTime = allocationLineComputeService.getAllocatedTime(project, sprint);
      BigDecimal budgetedTime = allocationLineComputeService.getBudgetedTime(sprint, project);
      json.put("$totalAllocatedTime", allocatedTime);
      json.put("$totalEstimatedTime", budgetedTime);

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return super.populate(json, context);
  }
}
