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
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionResponse;
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
  @Inject protected ProjectService projectService;

  @Override
  public Map<String, Object> getData(Project project) {
    Map<String, Object> dataMap = new HashMap<>();
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
    User currentUser = AuthUtils.getUser();

    List<Map<String, Object>> categoryList = new ArrayList<>();

    Map<ProjectTaskCategory, List<ProjectTask>> categoryTaskMap =
        projectTaskRepo.all()
            .filter("self.typeSelect = :_typeSelect AND self.project.id IN :_projectIds")
            .bind("_typeSelect", ProjectTaskRepository.TYPE_TASK)
            .bind("_project", currentUser.getContextProject())
            .bind("_projectIds", projectService.getContextProjectIds()).fetch().stream()
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

    for (Map.Entry<ProjectTaskCategory, List<ProjectTask>> entry : categoryTaskMap.entrySet()) {
      Map<String, Object> categoryMap = new HashMap<>();
      List<ProjectTask> projectTaskList = entry.getValue();
      ProjectTaskCategory category = entry.getKey();
      int totalCount = projectTaskList.size();
      long closedCount =
          projectTaskList.stream().filter(task -> task.getStatus().getIsCompleted()).count();

      if (category == null) {
        categoryMap.put("categoryId", 0);
        categoryMap.put("categoryName", "Others");
      } else {
        categoryMap.put("categoryId", category.getId());
        categoryMap.put("categoryName", category.getName());
      }
      categoryMap.put("open", totalCount - closedCount);
      categoryMap.put("closed", closedCount);
      categoryMap.put("total", totalCount);

      categoryList.add(categoryMap);
    }

    return categoryList;
  }

  protected Set<User> getMembers(Project project) {
    Set<User> membersSet = new HashSet<>();
    projectRepo.all().filter("self.id IN ?1", projectService.getContextProjectIds()).fetch()
        .stream()
        .forEach(subProject -> membersSet.addAll(subProject.getMembersUserSet()));
    return membersSet;
  }

  protected List<Project> getSubprojects(Project project) {
    Set<Long> contextProjectIds = projectService.getContextProjectIds();
    contextProjectIds.remove(project.getId());
    if (contextProjectIds.isEmpty()) {
      return new ArrayList<>();
    }
    return projectRepo.all().filter("self.id IN ?1", contextProjectIds).fetch();
  }

  @Override
  public ActionResponse getTasksPerCategoryView(Long id) {
    User currentUser = AuthUtils.getUser();
    ActionResponse response = new ActionResponse();
    response.setView(
        ActionView.define(I18n.get("Project Tasks"))
            .model(ProjectTask.class.getName())
            .add("grid", "project-task-grid")
            .add("form", "project-task-form")
            .domain(
                "self.typeSelect = :typeSelect AND self.project.id IN :projectIds AND (self.projectTaskCategory = :taskCategory OR (self.projectTaskCategory is null AND :taskCategory is null))")
            .context("typeSelect", ProjectTaskRepository.TYPE_TASK)
            .context("_project", currentUser.getContextProject())
            .context("projectIds", projectService.getContextProjectIds())
            .context("taskCategory", taskCategoryRepo.find(id))
            .param("search-filters", "project-task-filters")
            .map());
    return response;
  }

  @Override
  public ActionResponse getTasksOpenedPerCategoryView(Long id) {
    User currentUser = AuthUtils.getUser();
    ActionResponse response = new ActionResponse();
    response.setView(
        ActionView.define(I18n.get("Project Tasks"))
            .model(ProjectTask.class.getName())
            .add("grid", "project-task-grid")
            .add("form", "project-task-form")
            .domain(
                "self.typeSelect = :typeSelect AND self.project.id IN :projectIds AND self.status.isCompleted = false AND (self.projectTaskCategory = :taskCategory OR (self.projectTaskCategory is null AND :taskCategory is null))")
            .context("typeSelect", ProjectTaskRepository.TYPE_TASK)
            .context("_project", currentUser.getContextProject())
            .context("projectIds", projectService.getContextProjectIds())
            .context("taskCategory", taskCategoryRepo.find(id))
            .param("search-filters", "project-task-filters")
            .map());
    return response;
  }

  @Override
  public ActionResponse getTasksClosedPerCategoryView(Long id) {
    User currentUser = AuthUtils.getUser();
    ActionResponse response = new ActionResponse();
    response.setView(
        ActionView.define(I18n.get("Project Tasks"))
            .model(ProjectTask.class.getName())
            .add("grid", "project-task-grid")
            .add("form", "project-task-form")
            .domain(
                "self.typeSelect = :typeSelect AND self.project.id IN :projectIds AND self.status.isCompleted = true AND (self.projectTaskCategory = :taskCategory OR (self.projectTaskCategory is null AND :taskCategory is null))")
            .context("typeSelect", ProjectTaskRepository.TYPE_TASK)
            .context("_project", currentUser.getContextProject())
            .context("projectIds", projectService.getContextProjectIds())
            .context("taskCategory", taskCategoryRepo.find(id))
            .param("search-filters", "project-task-filters")
            .map());
    return response;
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
