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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.service.sprint.SprintHRService;
import com.axelor.apps.project.db.AllocationPeriod;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.AllocationPeriodRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.db.repo.SprintRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

  public void generateAllocations(ActionRequest request, ActionResponse response) {

    Set<AllocationPeriod> allocationPeriodSet = new HashSet<>();

    AllocationPeriodRepository allocationPeriodRepo = Beans.get(AllocationPeriodRepository.class);

    Optional.ofNullable(request.getContext().get("allocationPeriodSet"))
        .ifPresent(
            context ->
                ((List<LinkedHashMap<String, Object>>) context)
                    .stream()
                        .map(
                            period ->
                                allocationPeriodRepo.find(
                                    Long.parseLong(period.get("id").toString())))
                        .forEach(allocationPeriodSet::add));

    Project project =
        Beans.get(ProjectRepository.class)
            .find(((Number) request.getContext().get("_projectId")).longValue());

    try {
      Beans.get(SprintHRService.class).generateAllocations(project, allocationPeriodSet);
      response.setInfo(I18n.get("Allocations generated successfully for all periods and users"));
      response.setCanClose(true);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }
}
