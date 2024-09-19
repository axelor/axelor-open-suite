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

import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.service.SprintService;
import com.axelor.inject.Beans;
import java.util.Map;

public class SprintManagementRepository extends SprintRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    if (json != null && json.get("id") != null) {
      SprintService sprintService = Beans.get(SprintService.class);

      Sprint sprint = find((Long) json.get("id"));

      json.put("totalAllocatedTime", sprintService.computeTotalAllocatedTime(sprint));
      json.put("totalEstimatedTime", sprintService.computeTotalEstimatedTime(sprint));
      json.put("totalPlannedTime", sprintService.computeTotalPlannedTime(sprint));
      json.put("totalRemainingTime", sprintService.computeTotalRemainingTime(sprint));
    }

    return super.populate(json, context);
  }
}
