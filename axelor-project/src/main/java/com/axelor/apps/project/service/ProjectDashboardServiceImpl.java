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
package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskCategoryRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;

public class ProjectDashboardServiceImpl implements ProjectDashboardService {

  @Inject protected ProjectRepository projectRepo;
  @Inject protected ProjectTaskRepository projectTaskRepo;
  @Inject protected ProjectTaskCategoryRepository taskCategoryRepo;
  @Inject protected ProjectToolService projectToolService;

  @Override
  public Map<String, Object> getData(Project project) {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("$projectId", project.getId());
    dataMap.put("$name", project.getFullName());

    if (StringUtils.notEmpty(project.getDescription())) {
      dataMap.put("$description", Jsoup.parse(project.getDescription()).text());
    }
    dataMap.put("$isShowCalendarView", project.getIsShowCalendarPerUser());
    dataMap.put("$isShowGanttView", project.getIsShowGantt());
    dataMap.put("$categoryList", getIssueTrackingData(project));
    dataMap.put("$membersList", getMembers(project));
    dataMap.put("$subprojectList", getSubprojects(project));
    return dataMap;
  }

  protected List<Map<String, Object>> getIssueTrackingData(Project project) {

    List<Map<String, Object>> categoryList = new ArrayList<>();

    Map<ProjectTaskCategory, List<ProjectTask>> categoryTaskMap =
        projectTaskRepo
            .all()
            .filter("self.typeSelect = :_typeSelect AND self.project.id IN :_projectIds")
            .bind("_typeSelect", ProjectTaskRepository.TYPE_TASK)
            .bind("_projectIds", projectToolService.getRelatedProjectIds(project))
            .fetch()
            .stream()
            .sorted(getTaskComparator())
            .collect(
                Collectors.toMap(
                    ProjectTask::getProjectTaskCategory,
                    x -> {
                      List<ProjectTask> taskList = new ArrayList<>();
                      taskList.add(x);
                      return taskList;
                    },
                    (left, right) -> {
                      left.addAll(right);
                      return left;
                    },
                    LinkedHashMap::new));

    Map<String, Object> othersMap = null;
    for (Map.Entry<ProjectTaskCategory, List<ProjectTask>> entry : categoryTaskMap.entrySet()) {
      Map<String, Object> categoryMap = new HashMap<>();
      List<ProjectTask> projectTaskList = entry.getValue();
      ProjectTaskCategory category = entry.getKey();
      int totalCount = projectTaskList.size();
      long closedCount =
          projectTaskList.stream().filter(task -> task.getStatus().getIsCompleted()).count();

      if (category == null) {
        categoryMap.put("categoryId", 0L);
        categoryMap.put("categoryName", "Others");
      } else {
        categoryMap.put("categoryId", category.getId());
        categoryMap.put("categoryName", category.getName());
      }
      categoryMap.put("open", totalCount - closedCount);
      categoryMap.put("closed", closedCount);
      categoryMap.put("total", totalCount);
      categoryMap.put("projectId", project.getId());

      if (category == null) {
        othersMap = categoryMap;
      } else {
        categoryList.add(categoryMap);
      }
    }

    if (othersMap != null) {
      categoryList.add(othersMap);
    }

    return categoryList;
  }

  protected Set<User> getMembers(Project project) {
    Set<User> membersSet = new HashSet<>();
    projectRepo
        .all()
        .filter("self.id IN ?1", projectToolService.getRelatedProjectIds(project))
        .fetch()
        .stream()
        .forEach(subProject -> membersSet.addAll(subProject.getMembersUserSet()));
    return membersSet;
  }

  protected List<Project> getSubprojects(Project project) {
    Set<Long> contextProjectIds = projectToolService.getRelatedProjectIds(project);
    contextProjectIds.remove(project.getId());
    if (contextProjectIds.isEmpty()) {
      return new ArrayList<>();
    }
    return projectRepo.all().filter("self.id IN ?1", contextProjectIds).fetch();
  }

  protected Comparator<ProjectTask> getTaskComparator() {
    return new Comparator<ProjectTask>() {

      @Override
      public int compare(ProjectTask task1, ProjectTask task2) {
        if (task1.getProjectTaskCategory() == null || task2.getProjectTaskCategory() == null) {
          return 1;
        }
        return 0;
      }
    };
  }
}
