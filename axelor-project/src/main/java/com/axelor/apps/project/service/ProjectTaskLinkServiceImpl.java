package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskLink;
import com.axelor.apps.project.db.ProjectTaskLinkType;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectTaskLinkServiceImpl implements ProjectTaskLinkService {

  @Inject
  public ProjectTaskLinkServiceImpl() {}

  @Override
  public String getLinkTypeDomain(Project project) {
    if (project != null && !ObjectUtils.isEmpty(project.getProjectTaskLinkTypeSet())) {
      return String.format(
          "self.id IN (%s)",
          project.getProjectTaskLinkTypeSet().stream()
              .map(ProjectTaskLinkType::getId)
              .map(String::valueOf)
              .collect(Collectors.joining(",")));
    } else {
      return "self.id > 0";
    }
  }

  @Override
  public String getProjectTaskDomain(ProjectTask projectTask) {
    List<ProjectTask> unselectableTaskList = new ArrayList<>();
    unselectableTaskList.add(projectTask);

    if (!ObjectUtils.isEmpty(projectTask.getLinkedFromProjectTaskLinkList())) {
      unselectableTaskList.addAll(
          projectTask.getLinkedFromProjectTaskLinkList().stream()
              .map(ProjectTaskLink::getProjectTask2)
              .collect(Collectors.toList()));
    }
    if (!ObjectUtils.isEmpty(projectTask.getLinkedToProjectTaskLinkList())) {
      unselectableTaskList.addAll(
          projectTask.getLinkedToProjectTaskLinkList().stream()
              .map(ProjectTaskLink::getProjectTask1)
              .collect(Collectors.toList()));
    }

    return String.format(
        "self.id NOT IN (%s)",
        unselectableTaskList.stream()
            .map(ProjectTask::getId)
            .map(String::valueOf)
            .collect(Collectors.joining(",")));
  }
}
