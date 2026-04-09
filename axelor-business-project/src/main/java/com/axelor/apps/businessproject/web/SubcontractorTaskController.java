package com.axelor.apps.businessproject.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.db.ProjectType;
import com.axelor.apps.businessproject.db.SubcontractorTask;
import com.axelor.apps.businessproject.db.repo.ProjectTypeRepository;
import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class SubcontractorTaskController {
  @Inject ProjectRepository projectRepository;
  @Inject ProjectBusinessService projectService;
  @Inject ProjectTypeRepository projectTypeRepository;

  public void syncTimeSpent(ActionRequest request, ActionResponse response) {
    SubcontractorTask task = request.getContext().asType(SubcontractorTask.class);

    BigDecimal minutes = task.getTimeSpentMinutes();
    BigDecimal hours =
        minutes != null
            ? minutes.divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

    response.setValue("timeSpent", hours);
  }

  public void setSubcontractorTaskDefaults(ActionRequest request, ActionResponse response) {
    if (request.getContext().get("_projectId") == null) {
      response.setError(
          I18n.get("Please first save the project before proceeding"),
          I18n.get("No project attached"));
      return;
    }
    long projectId = ((Number) request.getContext().get("_projectId")).longValue();

    Project project = projectRepository.find(projectId);
    if (project == null) {
      response.setError(
          I18n.get("The project linked to this task could not be found. It may have been deleted."),
          I18n.get("Project not found"));
      return;
    }

    ProjectType projectType = project.getProjectType();
    if (projectType == null) {
      response.setError(
          I18n.get(
              "The project has no project type. Please select a project type for this project before proceeding"),
          I18n.get("Project Type Not set"));
      return;
    }
    projectType = projectTypeRepository.find(projectType.getId());

    response.setValue("project", project);

    if (Objects.equals(
        projectType.getSequence(), ProjectTypeRepository.SUBCONTRACTOR_PROJECT_TYPE)) {
      try {
        Partner subcontractor = projectService.getSubcontractor(project);
        response.setValue("subcontractor", subcontractor);
      } catch (AxelorException e) {
        TraceBackService.trace(response, e);
      }
    }
  }

  public void setSubcontractorTaskEmployeeFilter(ActionRequest request, ActionResponse response) {
    SubcontractorTask subcontractorTask = request.getContext().asType(SubcontractorTask.class);

    if (subcontractorTask == null || subcontractorTask.getProject() == null) {
      return;
    }

    Project project = projectRepository.find(subcontractorTask.getProject().getId());
    ProjectType projectType = project.getProjectType();

    if (projectType == null || !Boolean.TRUE.equals(projectType.getRequiresRecordInvoicingData())) {
      return;
    }

    if (Objects.equals(
        ProjectTypeRepository.SUBCONTRACTOR_PROJECT_TYPE, projectType.getSequence())) {
      response.setAttr("employee", "hidden", true);
      return;
    }

    // Set employee domain based on project members + assignedTo
    Set<User> members =
        new HashSet<>(
            Optional.ofNullable(project.getMembersUserSet()).orElse(Collections.emptySet()));
    if (project.getAssignedTo() != null) {
      members.add(project.getAssignedTo());
    }

    String employeeIds =
        members.stream()
            .map(User::getEmployee)
            .filter(Objects::nonNull)
            .map(e -> String.valueOf(e.getId()))
            .collect(Collectors.joining(","));

    response.setAttr(
        "employee",
        "domain",
        employeeIds.isEmpty() ? "self.id IN (null)" : "self.id IN (" + employeeIds + ")");

    response.setValue("employee", project.getAssignedTo());
  }

  public void validateData(ActionRequest request, ActionResponse response) {
    SubcontractorTask subcontractorTask = request.getContext().asType(SubcontractorTask.class);

    if (subcontractorTask == null) {
      return;
    }
    Project project = subcontractorTask.getProject();
    if (project == null) {
      response.setError(
          I18n.get("A project is required to be attached to this task"),
          I18n.get("No project attached"));
      return;
    }

    ProjectType projectType = project.getProjectType();
    if (projectType == null) {
      response.setError(
          I18n.get(
              "The project has no project type. Please set a project type on the project before saving qualifications."),
          I18n.get("Project Type Not Set"));
      return;
    }

    if (Objects.equals(projectType.getSequence(), ProjectTypeRepository.SUBCONTRACTOR_PROJECT_TYPE)
        && subcontractorTask.getSubcontractor() == null) {
      response.setError(
          I18n.get("Please select a subcontractor for the project this task is for"),
          I18n.get("Subcontractor Not Set"));
    }
  }
}
