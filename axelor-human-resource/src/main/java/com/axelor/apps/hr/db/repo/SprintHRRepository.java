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
package com.axelor.apps.hr.db.repo;

import com.axelor.apps.project.db.AllocationLine;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.SprintRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class SprintHRRepository extends SprintRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    Sprint sprint = find((Long) json.get("id"));

    BigDecimal totalAllocatedTime = BigDecimal.ZERO;
    BigDecimal totalEstimatedTime = BigDecimal.ZERO;
    BigDecimal totalPlannedTime = BigDecimal.ZERO;
    BigDecimal totalRemainingTime = BigDecimal.ZERO;

    List<AllocationLine> allocationLineList = sprint.getAllocationLineList();

    if (CollectionUtils.isNotEmpty(allocationLineList)) {

      for (AllocationLine allocationLine : allocationLineList) {
        totalAllocatedTime = totalAllocatedTime.add(allocationLine.getAllocated());
        totalPlannedTime = totalPlannedTime.add(allocationLine.getPlannedTime());
        totalRemainingTime = totalRemainingTime.add(allocationLine.getAvailableAllocation());
      }
    }

    List<ProjectTask> projectTaskList = sprint.getProjectTaskList();

    if (CollectionUtils.isNotEmpty(projectTaskList)) {

      for (ProjectTask projectTask : projectTaskList) {
        totalEstimatedTime = totalEstimatedTime.add(projectTask.getBudgetedTime());
      }
    }

    json.put("$totalAllocatedTime", totalAllocatedTime.setScale(2, RoundingMode.HALF_UP));
    json.put("$totalEstimatedTime", totalEstimatedTime.setScale(2, RoundingMode.HALF_UP));
    json.put("$totalPlannedTime", totalPlannedTime.setScale(2, RoundingMode.HALF_UP));
    json.put("$totalRemainingTime", totalRemainingTime.setScale(2, RoundingMode.HALF_UP));

    return super.populate(json, context);
  }
}
