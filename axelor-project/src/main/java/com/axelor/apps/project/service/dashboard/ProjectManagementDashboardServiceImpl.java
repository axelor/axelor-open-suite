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
package com.axelor.apps.project.service.dashboard;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.roadmap.SprintGeneratorService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectManagementDashboardServiceImpl implements ProjectManagementDashboardService {
  protected ProjectTaskRepository projectTaskRepo;
  protected ProjectRepository projectRepo;
  protected SprintGeneratorService sprintGeneratorService;

  @Inject
  public ProjectManagementDashboardServiceImpl(
      ProjectTaskRepository projectTaskRepo,
      ProjectRepository projectRepo,
      SprintGeneratorService sprintGeneratorService) {
    this.projectTaskRepo = projectTaskRepo;
    this.projectRepo = projectRepo;
    this.sprintGeneratorService = sprintGeneratorService;
  }

  @Override
  public Map<String, Object> getDate() {
    Map<String, Object> dataMap = new HashMap<>();
    LocalDate todayDate = LocalDate.now();
    dataMap.put("$fromDate", todayDate);
    dataMap.put("$toDate", todayDate.plusDays(7));

    return dataMap;
  }

  @Override
  public Map<String, Object> getData(Project project) {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("$sprintComputedList", getSprintComputedList(project));
    return dataMap;
  }

  protected List<Map<String, Object>> getSprintComputedList(Project project) {
    List<Sprint> sprintList = sprintGeneratorService.getSprintDomain(project);
    List<Map<String, Object>> mapList = new ArrayList<>();

    for (Sprint sprint : sprintList) {
      BigDecimal budgetedTime =
          sprint.getProjectTaskList().stream()
              .map(ProjectTask::getBudgetedTime)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
      Map<String, Object> map = new HashMap<>();
      map.put("sprintId",sprint.getId());
      map.put("sprint", sprint.getName());
      map.put("budgetedTime", budgetedTime);
      mapList.add(map);
    }

    return mapList;
  }
}
