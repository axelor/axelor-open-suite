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
package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.businessproject.service.BusinessProjectSprintAllocationLineService;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.SprintAllocationLine;
import com.axelor.apps.project.db.repo.SprintAllocationLineRepository;
import com.axelor.apps.project.db.repo.SprintManagementRepository;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;

public class BusinessProjectSprintRepository extends SprintManagementRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    if (Objects.isNull(json) || Objects.isNull(json.get("id"))) {
      return super.populate(json, context);
    }

    Sprint sprint = find((Long) json.get("id"));
    List<SprintAllocationLine> sprintAllocationLineList =
        Beans.get(SprintAllocationLineRepository.class)
            .all()
            .filter("self.sprint = ?1", sprint)
            .fetch();

    if (CollectionUtils.isEmpty(sprintAllocationLineList)) {
      return super.populate(json, context);
    }

    BusinessProjectSprintAllocationLineService allocationLineService =
        Beans.get(BusinessProjectSprintAllocationLineService.class);

    BigDecimal totalPlannedTime =
        sprintAllocationLineList.stream()
            .map(allocationLineService::computeFields)
            .map(valueMap -> valueMap.get("plannedTime"))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalRemainingTime =
        sprintAllocationLineList.stream()
            .map(allocationLineService::computeFields)
            .map(valueMap -> valueMap.get("remainingTime"))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    json.put("totalPlannedTime", totalPlannedTime);
    json.put("totalRemainingTime", totalRemainingTime);

    return super.populate(json, context);
  }
}
