package com.axelor.apps.project.service.taskLink;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskLink;
import com.axelor.apps.project.db.ProjectTaskLinkType;
import com.axelor.apps.project.db.repo.ProjectTaskLinkRepository;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectTaskLinkServiceImpl implements ProjectTaskLinkService {

  protected ProjectTaskLinkRepository projectTaskLinkRepository;

  @Inject
  public ProjectTaskLinkServiceImpl(ProjectTaskLinkRepository projectTaskLinkRepository) {
    this.projectTaskLinkRepository = projectTaskLinkRepository;
  }

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
    List<Long> unselectableTaskIdList = new ArrayList<>();
    if (projectTask.getId() != null) {
      unselectableTaskIdList.add(projectTask.getId());
    } else {
      unselectableTaskIdList.add(0L);
    }

    if (!ObjectUtils.isEmpty(projectTask.getProjectTaskLinkList())) {
      unselectableTaskIdList.addAll(
          projectTask.getProjectTaskLinkList().stream()
              .map(ProjectTaskLink::getRelatedTask)
              .map(ProjectTask::getId)
              .collect(Collectors.toList()));
    }

    return String.format(
        "self.id NOT IN (%s)",
        unselectableTaskIdList.stream().map(String::valueOf).collect(Collectors.joining(",")));
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void removeLink(ProjectTaskLink projectTaskLink) {
    if (projectTaskLink == null || projectTaskLink.getProjectTaskLink() == null) {
      return;
    }

    projectTaskLink = projectTaskLinkRepository.find(projectTaskLink.getId());
    ProjectTaskLink oppositeLink = EntityHelper.getEntity(projectTaskLink.getProjectTaskLink());
    projectTaskLink = emptyTaskLink(projectTaskLink);
    oppositeLink = emptyTaskLink(oppositeLink);
    projectTaskLinkRepository.remove(projectTaskLink);
    projectTaskLinkRepository.remove(oppositeLink);
    JPA.flush();
  }

  protected ProjectTaskLink emptyTaskLink(ProjectTaskLink projectTaskLink) {
    projectTaskLink.setProjectTask(null);
    projectTaskLink.setRelatedTask(null);
    projectTaskLink.setProjectTaskLinkType(null);
    projectTaskLink.setProjectTaskLink(null);
    return projectTaskLink;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void generateTaskLink(
      ProjectTask projectTask, ProjectTask relatedTask, ProjectTaskLinkType projectTaskLinkType)
      throws AxelorException {
    if (projectTask == null || relatedTask == null || projectTaskLinkType == null) {
      return;
    }

    ProjectTaskLink projectTaskLink =
        createProjectTaskLink(projectTask, relatedTask, projectTaskLinkType);
    ProjectTaskLink oppositeProjectTaskLink =
        createProjectTaskLink(
            relatedTask,
            projectTask,
            projectTaskLinkType.getOppositeLinkType() != null
                ? projectTaskLinkType.getOppositeLinkType()
                : projectTaskLinkType);

    checkTypeAvailableInProjectConfig(
        oppositeProjectTaskLink.getProjectTaskLinkType(), relatedTask.getProject());

    projectTaskLink.setProjectTaskLink(oppositeProjectTaskLink);
    projectTask.addProjectTaskLinkListItem(projectTaskLink);
    oppositeProjectTaskLink.setProjectTaskLink(projectTaskLink);
    relatedTask.addProjectTaskLinkListItem(oppositeProjectTaskLink);
    projectTaskLinkRepository.save(projectTaskLink);
    projectTaskLinkRepository.save(oppositeProjectTaskLink);
    projectTask.addProjectTaskLinkListItem(projectTaskLink);
  }

  protected ProjectTaskLink createProjectTaskLink(
      ProjectTask projectTask, ProjectTask relatedTask, ProjectTaskLinkType projectTaskLinkType) {
    ProjectTaskLink projectTaskLink = new ProjectTaskLink();
    projectTaskLink.setProjectTask(projectTask);
    projectTaskLink.setRelatedTask(relatedTask);
    projectTaskLink.setProjectTaskLinkType(projectTaskLinkType);

    return projectTaskLink;
  }

  protected void checkTypeAvailableInProjectConfig(
      ProjectTaskLinkType projectTaskLinkType, Project project) throws AxelorException {
    if (project != null
        && projectTaskLinkType != null
        && !ObjectUtils.isEmpty(project.getProjectTaskLinkTypeSet())
        && !project.getProjectTaskLinkTypeSet().contains(projectTaskLinkType)) {
      throw new AxelorException(
          project,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(ProjectExceptionMessage.LINK_TYPE_UNAVAILABLE_IN_PROJECT_CONFIG),
              project.getFullName(),
              projectTaskLinkType.getName()));
    }
  }
}
