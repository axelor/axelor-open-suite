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

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.service.sprint.SprintHRService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.db.repo.SprintRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectController {

  public void attachTasksToSprintWithProjectPlannings(
      ActionRequest request, ActionResponse response) {

    List<LinkedHashMap<String, Object>> projectTaskMaps =
        (List<LinkedHashMap<String, Object>>) request.getContext().get("projectTasks");

    Long sprintId =
        Long.valueOf(
            ((LinkedHashMap<String, Object>) request.getContext().get("sprint"))
                .get("id")
                .toString());

    Sprint sprint = Beans.get(SprintRepository.class).find(sprintId);

    ProjectTaskRepository projectTaskRepo = Beans.get(ProjectTaskRepository.class);

    List<ProjectTask> projectTasks =
        projectTaskMaps.stream()
            .map(task -> Long.valueOf(task.get("id").toString()))
            .map(projectTaskRepo::find)
            .collect(Collectors.toList());

    Beans.get(SprintHRService.class).attachTasksToSprintWithProjectPlannings(sprint, projectTasks);

    response.setCanClose(true);
  }

  public void defaultSprintAndPeriods(ActionRequest request, ActionResponse response) {

    Project project = request.getContext().asType(Project.class);

    LocalDate todayDate = Beans.get(AppBaseService.class).getTodayDate(project.getCompany());
    Sprint sprint = Beans.get(SprintRepository.class).findByProjectAndDate(project, todayDate);

    if (sprint != null) {
      response.setValue("sprint", sprint);
      response.setValue(
          "allocationPeriodSet",
          Beans.get(SprintHRService.class).defaultAllocationPeriods(sprint, todayDate));
    }
  }
}
