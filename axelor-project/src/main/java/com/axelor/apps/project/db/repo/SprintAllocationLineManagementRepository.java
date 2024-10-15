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

import com.axelor.apps.project.db.SprintAllocationLine;
import com.axelor.apps.project.service.sprint.SprintAllocationLineService;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SprintAllocationLineManagementRepository extends SprintAllocationLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    if (Objects.isNull(json) || Objects.isNull(json.get("id"))) {
      return super.populate(json, context);
    }

    SprintAllocationLine sprintAllocationLine = find((Long) json.get("id"));

    HashMap<String, BigDecimal> valueMap =
        Beans.get(SprintAllocationLineService.class)
            .computeSprintAllocationLine(sprintAllocationLine);

    json.put("$leaves", valueMap.get("leaves"));
    json.put("$plannedTime", valueMap.get("plannedTime"));
    json.put("$remainingTime", valueMap.get("remainingTime"));

    return super.populate(json, context);
  }
}
