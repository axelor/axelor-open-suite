package com.axelor.apps.project.web;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectVersion;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectVersionRepository;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.roadmap.SprintGeneratorService;
import com.axelor.apps.project.service.roadmap.SprintGetService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.StringHelper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SprintController {

  public void generateSprints(ActionRequest request, ActionResponse response) {

    Object projectContext = request.getContext().get("project");
    Object projectVersionContext = request.getContext().get("targetVersion");
    Object fromDateContext = request.getContext().get("fromDate");
    Object toDateContext = request.getContext().get("toDate");
    Integer numberDaysContext = (Integer) request.getContext().get("numberDays");

    Project project =
        projectContext != null
            ? Beans.get(ProjectRepository.class)
                .find(
                    Long.valueOf(
                        ((LinkedHashMap<String, Object>) projectContext).get("id").toString()))
            : null;
    ProjectVersion projectVersion =
        projectVersionContext != null
            ? Beans.get(ProjectVersionRepository.class)
                .find(
                    Long.valueOf(
                        ((LinkedHashMap<String, Object>) projectVersionContext)
                            .get("id")
                            .toString()))
            : null;

    if ((project == null && projectVersion == null)
        || fromDateContext == null
        || toDateContext == null
        || LocalDate.parse(fromDateContext.toString())
            .isAfter(LocalDate.parse(toDateContext.toString()))
        || numberDaysContext <= 0) {
      response.setError(I18n.get(ProjectExceptionMessage.SPRINT_FIELDS_MISSING));
      return;
    }

    LocalDate fromDate = LocalDate.parse(fromDateContext.toString());
    LocalDate toDate = LocalDate.parse(toDateContext.toString());

    Set<Sprint> sprintSet =
        Beans.get(SprintGeneratorService.class)
            .generateSprints(project, projectVersion, fromDate, toDate, numberDaysContext);

    if (CollectionUtils.isNotEmpty(sprintSet)) {
      response.setInfo(
          String.format(I18n.get(ProjectExceptionMessage.SPRINT_GENERATED), sprintSet.size()));
      response.setCanClose(true);
    }
  }

  public void initDefaultWizardValues(ActionRequest request, ActionResponse response) {
    Object projectContext = request.getContext().get("project");
    Object targetVersionContext = request.getContext().get("targetVersion");

    Optional<Project> projectOpt =
        Optional.ofNullable(projectContext)
            .map(
                context ->
                    Beans.get(ProjectRepository.class)
                        .find(
                            Long.valueOf(
                                ((LinkedHashMap<String, Object>) projectContext)
                                    .get("id")
                                    .toString())));
    Optional<ProjectVersion> projectVersionOpt =
        Optional.ofNullable(targetVersionContext)
            .map(
                context ->
                    Beans.get(ProjectVersionRepository.class)
                        .find(
                            Long.valueOf(
                                ((LinkedHashMap<String, Object>) targetVersionContext)
                                    .get("id")
                                    .toString())));

    response.setValues(
        Beans.get(SprintGeneratorService.class)
            .initDefaultValues(projectOpt.orElse(null), projectVersionOpt.orElse(null)));
  }

  public void initDefaultValues(ActionRequest request, ActionResponse response) {
    Project project = null;
    ProjectVersion projectVersion = null;
    Context parentContext = request.getContext().getParent();
    if (parentContext != null) {
      if (Project.class.equals(parentContext.getContextClass())) {
        project = EntityHelper.getEntity(parentContext.asType(Project.class));
      } else if (ProjectVersion.class.equals(parentContext.getContextClass())) {
        projectVersion = EntityHelper.getEntity(parentContext.asType(ProjectVersion.class));
      }
    }

    response.setValues(
        Beans.get(SprintGeneratorService.class).initDefaultValues(project, projectVersion));
  }

  public void computeSprintDomainDashboard(ActionRequest request, ActionResponse response) {
    Long projectId = Long.valueOf(((Map) request.getContext().get("project")).get("id").toString());
    ProjectRepository projectRepo = Beans.get(ProjectRepository.class);
    List<Sprint> sprintList =
        Beans.get(SprintGeneratorService.class).getProjectSprintList(projectRepo.find(projectId));
    String domain = String.format("self.id in (%s)", StringHelper.getIdListString(sprintList));

    response.setAttr("$sprint", "domain", domain);
  }

  public void showAllSprints(ActionRequest request, ActionResponse response) {
    Context parentContext = request.getContext().getParent();
    List<Sprint> sprintList = new ArrayList<>();
    if (parentContext != null && Project.class.equals(parentContext.getContextClass())) {
      sprintList =
          Beans.get(SprintGetService.class).getSprintList(parentContext.asType(Project.class));
    } else if (parentContext != null
        && ProjectVersion.class.equals(parentContext.getContextClass())) {
      sprintList = parentContext.asType(ProjectVersion.class).getSprintList();
    }

    List<Long> sprintIdList = List.of(0L);
    if (ObjectUtils.notEmpty(sprintList)) {
      sprintIdList = sprintList.stream().map(Sprint::getId).collect(Collectors.toList());
    }

    ActionView.ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Sprints"));
    actionViewBuilder.model(Sprint.class.getName());
    actionViewBuilder.add("grid", "sprint-grid");
    actionViewBuilder.add("form", "sprint-form");
    actionViewBuilder.param("show-toolbar", Boolean.FALSE.toString());
    actionViewBuilder.domain("self.id IN (:sprintIds)");
    actionViewBuilder.context("sprintIds", sprintIdList);

    response.setView(actionViewBuilder.map());
  }

  @CallMethod
  public List<Long> computeSprintDomain(Long projectId) {
    ProjectRepository projectRepo = Beans.get(ProjectRepository.class);
    List<Sprint> sprintList = new ArrayList<>();
    if (projectId != null) {
      Project project = projectRepo.find(projectId);
      sprintList = Beans.get(SprintGeneratorService.class).getSprintDomain(project);
    }
    return sprintList.stream().map(Sprint::getId).collect(Collectors.toList());
  }
}
