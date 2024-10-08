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
package com.axelor.apps.project.db.repo;

import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.SprintAllocationLine;
import com.axelor.apps.project.service.sprint.SprintAllocationLineService;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;

public class SprintManagementRepository extends SprintRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    if (Objects.isNull(json) || Objects.isNull(json.get("id"))) {
      return super.populate(json, context);
    }

    Sprint sprint = find((Long) json.get("id"));

    BigDecimal totalPlannedTime = BigDecimal.ZERO;
    BigDecimal totalRemainingTime = BigDecimal.ZERO;
    BigDecimal totalAllocatedTime = BigDecimal.ZERO;
    BigDecimal totalEstimatedTime = BigDecimal.ZERO;

    SprintAllocationLineService sprintAllocationLineService =
        Beans.get(SprintAllocationLineService.class);

    List<SprintAllocationLine> sprintAllocationLines = sprint.getSprintAllocationLineList();

    if (CollectionUtils.isNotEmpty(sprintAllocationLines)) {

      for (SprintAllocationLine line : sprintAllocationLines) {
        Map<String, BigDecimal> valueMap =
            sprintAllocationLineService.computeSprintAllocationLine(line);
        totalPlannedTime =
            totalPlannedTime.add(valueMap.getOrDefault("plannedTime", BigDecimal.ZERO));
        totalRemainingTime =
            totalRemainingTime.add(valueMap.getOrDefault("remainingTime", BigDecimal.ZERO));
        totalAllocatedTime = totalAllocatedTime.add(line.getAllocated());
      }
    }

    List<ProjectTask> projectTasks = sprint.getProjectTaskList();

    if (CollectionUtils.isNotEmpty(projectTasks)) {
      totalEstimatedTime =
          projectTasks.stream()
              .map(task -> task.getBudgetedTime())
              .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    json.put("$totalPlannedTime", totalPlannedTime.setScale(2, RoundingMode.HALF_UP));
    json.put("$totalRemainingTime", totalRemainingTime.setScale(2, RoundingMode.HALF_UP));
    json.put("$totalAllocatedTime", totalAllocatedTime.setScale(2, RoundingMode.HALF_UP));
    json.put("$totalEstimatedTime", totalEstimatedTime.setScale(2, RoundingMode.HALF_UP));

    return super.populate(json, context);
  }
}
