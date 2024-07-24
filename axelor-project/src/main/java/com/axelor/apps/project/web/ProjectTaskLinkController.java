package com.axelor.apps.project.web;

import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskLink;
import com.axelor.apps.project.db.ProjectTaskLinkType;
import com.axelor.common.ObjectUtils;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProjectTaskLinkController {

  @ErrorException
  public void setLinkTypeDomain(ActionRequest request, ActionResponse response) {
    Context parentContext = request.getContext().getParent();
    if (parentContext == null || !ProjectTask.class.equals(parentContext.getContextClass())) {
      return;
    }

    Project project =
        Optional.of(parentContext.asType(ProjectTask.class))
            .map(ProjectTask::getProject)
            .orElse(null);

    if (project != null && !ObjectUtils.isEmpty(project.getProjectTaskLinkTypeSet())) {
      response.setAttr(
          "projectTaskLinkType",
          "domain",
          String.format(
              "self.id IN (%s)",
              project.getProjectTaskLinkTypeSet().stream()
                  .map(ProjectTaskLinkType::getId)
                  .map(String::valueOf)
                  .collect(Collectors.joining(","))));
    } else {
      response.setAttr("projectTaskLinkType", "domain", "self.id > 0");
    }
  }

  @ErrorException
  public void setTaskDomain(ActionRequest request, ActionResponse response) {
    Context parentContext = request.getContext().getParent();
    if (parentContext == null || !ProjectTask.class.equals(parentContext.getContextClass())) {
      return;
    }

    ProjectTask projectTask = parentContext.asType(ProjectTask.class);
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

    String domain =
        String.format(
            "self.id NOT IN (%s)",
            unselectableTaskList.stream()
                .map(ProjectTask::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(",")));

    response.setAttr("projectTask1", "domain", domain);
    response.setAttr("projectTask2", "domain", domain);
  }
}
