package com.axelor.apps.project.service.roadmap;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectVersion;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.SprintRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SprintGetServiceImpl implements SprintGetService {

  protected AppBaseService appBaseService;
  protected SprintRepository sprintRepository;

  @Inject
  public SprintGetServiceImpl(AppBaseService appBaseService, SprintRepository sprintRepository) {
    this.appBaseService = appBaseService;
    this.sprintRepository = sprintRepository;
  }

  @Override
  public List<Sprint> getSprintToDisplay(Project project) {
    List<Sprint> sprintList = new ArrayList<>();
    if (Objects.equals(
        ProjectRepository.SPRINT_MANAGEMENT_NONE, project.getSprintManagementSelect())) {
      return sprintList;
    }

    if (project.getBacklogSprint() != null) {
      sprintList.add(project.getBacklogSprint());
    }

    sprintList.addAll(filterSprintsWithCurrentDate(getSprintList(project), project.getCompany()));

    return sprintList;
  }

  protected List<Sprint> getSprintList(Project project) {
    List<Sprint> slist = new ArrayList<>();
    if (Objects.equals(
            ProjectRepository.SPRINT_MANAGEMENT_PROJECT, project.getSprintManagementSelect())
        && ObjectUtils.notEmpty(project.getSprintList())) {
      slist = project.getSprintList();
    } else if (Objects.equals(
            ProjectRepository.SPRINT_MANAGEMENT_VERSION, project.getSprintManagementSelect())
        && ObjectUtils.notEmpty(project.getRoadmapSet())) {
      slist =
          project.getRoadmapSet().stream()
              .map(ProjectVersion::getSprintList)
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
    }
    return slist;
  }

  @Override
  public String getSprintIdsToExclude(List<Sprint> sprintList) {
    String sprintIdsStr =
        sprintList.stream()
            .map(Sprint::getId)
            .map(Object::toString)
            .collect(Collectors.joining(","));
    return sprintRepository
        .all()
        .filter(String.format("self.id NOT IN (%s)", sprintIdsStr))
        .fetchStream()
        .map(Sprint::getId)
        .map(Object::toString)
        .collect(Collectors.joining(","));
  }

  protected List<Sprint> filterSprintsWithCurrentDate(List<Sprint> sprintList, Company company) {
    if (ObjectUtils.isEmpty(sprintList)) {
      return new ArrayList<>();
    }

    return sprintList.stream()
        .filter(sprint -> sprint.getToDate().isAfter(appBaseService.getTodayDate(company)))
        .collect(Collectors.toList());
  }
}
