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
package com.axelor.apps.hr.web.project;

import com.axelor.apps.hr.service.project.ProjectTaskHRService;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.ArrayList;
import java.util.List;

public class ProjectTaskHRController {

  public void updateProjectPlanningTime(ActionRequest request, ActionResponse response) {

    ProjectTask projectTask = request.getContext().asType(ProjectTask.class);

    Sprint sprint = projectTask.getSprint();
    User assignedTo = projectTask.getAssignedTo();

    ProjectTaskHRService projectTaskHRService = Beans.get(ProjectTaskHRService.class);

    if (projectTask.getId() != null) {
      ProjectTask projectTaskDb = Beans.get(ProjectTaskRepository.class).find(projectTask.getId());
      Sprint sprintDb = projectTaskDb.getSprint();
      User assignedToDb = projectTaskDb.getAssignedTo();

      boolean isSprintChanged = (sprintDb != null && !sprintDb.equals(sprint));
      boolean isAssignedToChanged = !assignedToDb.equals(assignedTo);

      if ((isSprintChanged && sprint != null) || isAssignedToChanged) {
        response.setValue(
            "projectPlanningTimeList",
            projectTaskHRService.updateProjectPlanningTime(projectTask, projectTaskDb));
      } else if (sprintDb == null && sprint != null) {
        List<ProjectPlanningTime> projectPlanningTimeList =
            projectTask.getProjectPlanningTimeList();
        ProjectPlanningTime projectPlanningTime =
            projectTaskHRService.createProjectPlanningTime(projectTask, sprint);

        if (projectPlanningTime != null) {
          projectPlanningTimeList.add(projectPlanningTime);
        }

        response.setValue("projectPlanningTimeList", projectPlanningTimeList);
      }
    } else if (sprint != null) {
      List<ProjectPlanningTime> projectPlanningTimeList = new ArrayList<>();
      projectPlanningTimeList.add(
          projectTaskHRService.createProjectPlanningTime(projectTask, sprint));

      response.setValue("projectPlanningTimeList", projectPlanningTimeList);
    }
  }
}
