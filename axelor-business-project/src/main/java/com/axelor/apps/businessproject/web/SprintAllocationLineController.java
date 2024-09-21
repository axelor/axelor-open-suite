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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.businessproject.service.sprint.SprintAllocationLineService;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.SprintAllocationLine;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.math.BigDecimal;
import java.util.HashMap;

public class SprintAllocationLineController {

  public void userDomain(ActionRequest request, ActionResponse response) {

    Sprint sprint = null;

    Context parent = request.getContext().getParent();

    if (parent != null && parent.get("_model").equals(Sprint.class.getName())) {
      sprint = parent.asType(Sprint.class);
    } else {
      SprintAllocationLine sprintAllocationLine =
          request.getContext().asType(SprintAllocationLine.class);
      sprint = sprintAllocationLine.getSprint();
    }

    String domain =
        (sprint != null && sprint.getProject() != null)
            ? sprint.getProject().getId() + " member of self.projectSet"
            : "self.id in (0)";

    response.setAttr("user", "domain", domain);
  }

  public void computeSprintAllocationLine(ActionRequest request, ActionResponse response) {

    SprintAllocationLine sprintAllocationLine =
        request.getContext().asType(SprintAllocationLine.class);

    HashMap<String, BigDecimal> valueMap =
        Beans.get(SprintAllocationLineService.class)
            .computeSprintAllocationLine(sprintAllocationLine);

    response.setValue("$leaves", valueMap.get("leaves"));
    response.setValue("$plannedTime", valueMap.get("plannedTime"));
    response.setValue("$remainingTime", valueMap.get("remainingTime"));
  }
}
