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
package com.axelor.apps.hr.web.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.service.project.ProjectTaskSprintService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.common.StringUtils;
import com.axelor.db.EntityHelper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProjectTaskController {

  public void validateSprintPlanification(ActionRequest request, ActionResponse response) {
    ProjectTask projectTask = request.getContext().asType(ProjectTask.class);

    String warning =
        Beans.get(ProjectTaskSprintService.class).getSprintOnChangeWarning(projectTask);
    if (StringUtils.notEmpty(warning)) {
      response.setAlert(warning);
    }
  }

  public void createSprintPlanification(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ProjectTask projectTask =
        EntityHelper.getEntity(request.getContext().asType(ProjectTask.class));
    Beans.get(ProjectTaskSprintService.class).createOrMovePlanification(projectTask);
    response.setValue("oldActiveSprint", projectTask.getActiveSprint());
    response.setValue("oldBudgetedTime", projectTask.getBudgetedTime());
    response.setValue("projectPlanningTimeList", projectTask.getProjectPlanningTimeList());
  }
}
