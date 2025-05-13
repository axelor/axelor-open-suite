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

    sprintList.addAll(filterSprintsWithCurrentDate(getSprintList(project), project.getCompany()));

    return sprintList;
  }

  @Override
  public List<Sprint> getSprintToDisplayIncludingBacklog(Project project) {
    List<Sprint> sprintList = getSprintToDisplay(project);

    if (project.getBacklogSprint() != null) {
      sprintList.add(project.getBacklogSprint());
    }
    return sprintList;
  }

  @Override
  public List<Sprint> getSprintList(Project project) {
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
