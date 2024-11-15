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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.service.sprint.AllocationLineService;
import com.axelor.apps.project.db.AllocationLine;
import com.axelor.apps.project.db.AllocationPeriod;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.AllocationLineRepository;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.util.Map;

public class AllocationLineHRRepository extends AllocationLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    AllocationLine allocationLine = find((Long) json.get("id"));

    AllocationPeriod period = allocationLine.getAllocationPeriod();
    User user = allocationLine.getUser();
    Project project = allocationLine.getProject();

    BigDecimal leaves = BigDecimal.ZERO;
    BigDecimal alreadyAllocated = BigDecimal.ZERO;
    BigDecimal availableAllocation = BigDecimal.ZERO;

    AllocationLineService allocationLineService = Beans.get(AllocationLineService.class);

    try {
      leaves = allocationLineService.getLeaves(period, user);
      alreadyAllocated = allocationLineService.getAlreadyAllocated(project, period, user);
      availableAllocation =
          allocationLineService.getAvailableAllocation(period, user, leaves, alreadyAllocated);
    } catch (AxelorException e) {
      TraceBackService.trace(e);
    }

    json.put("leaves", leaves);
    json.put("alreadyAllocated", alreadyAllocated);
    json.put("availableAllocation", availableAllocation);

    return super.populate(json, context);
  }
}
