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

import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectVersion;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectVersionRepository;
import com.axelor.apps.project.db.repo.SprintRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.meta.CallMethod;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SprintGeneratorServiceImpl implements SprintGeneratorService {

  protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d");

  protected AppBaseService appBaseService;
  protected ProjectRepository projectRepository;
  protected ProjectVersionRepository projectVersionRepository;
  protected SprintRepository sprintRepository;

  @Inject
  public SprintGeneratorServiceImpl(
      AppBaseService appBaseService,
      ProjectRepository projectRepository,
      ProjectVersionRepository projectVersionRepository,
      SprintRepository sprintRepository) {
    this.appBaseService = appBaseService;
    this.projectRepository = projectRepository;
    this.projectVersionRepository = projectVersionRepository;
    this.sprintRepository = sprintRepository;
  }

  @Override
  public Map<String, Object> initDefaultValues(Project project, ProjectVersion projectVersion) {
    Map<String, Object> valuesMap = new HashMap<>();

    addDatesFields(project, projectVersion, valuesMap);
    valuesMap.put("project", project);
    valuesMap.put("targetVersion", projectVersion);

    return valuesMap;
  }

  protected void addDatesFields(
      Project project, ProjectVersion projectVersion, Map<String, Object> valuesMap) {
    LocalDate fromDate = getFromDate(project, projectVersion);
    valuesMap.put("fromDate", fromDate);
    LocalDate toDate = getToDate(project, projectVersion);

    if (fromDate != null && toDate != null && !toDate.isBefore(fromDate)) {
      valuesMap.put("toDate", toDate);
    }

    valuesMap.put("numberDays", 7);
  }

  protected LocalDate getFromDate(Project project, ProjectVersion projectVersion) {
    if (project != null) {
      LocalDate toDate = getLastToDate(project.getSprintList());
      if (toDate != null) {
        return toDate;
      }

      if (project.getFromDate() != null) {
        return project.getFromDate().toLocalDate();
      } else {
        return appBaseService.getTodayDate(project.getCompany());
      }
    } else if (projectVersion != null) {
      LocalDate toDate = getLastToDate(projectVersion.getSprintList());
      if (toDate != null) {
        return toDate;
      }
      return appBaseService.getTodayDate(null);
    }

    return null;
  }

  protected LocalDate getLastToDate(List<Sprint> sprintList) {
    if (ObjectUtils.isEmpty(sprintList)) {
      return null;
    }

    LocalDate toDate =
        sprintList.stream()
            .filter(sprint -> sprint.getToDate() != null)
            .sorted(Comparator.comparing(Sprint::getToDate).reversed())
            .map(Sprint::getToDate)
            .findFirst()
            .orElse(null);
    if (toDate == null) {
      return null;
    }
    return toDate.plusDays(1);
  }

  protected LocalDate getToDate(Project project, ProjectVersion projectVersion) {
    if (project != null && project.getToDate() != null) {
      return project.getToDate().toLocalDate();
    } else if (projectVersion != null) {
      return projectVersion.getTestingServerDate();
    }
    return null;
  }

  @Override
  public Set<Sprint> generateSprints(
      Project project,
      ProjectVersion projectVersion,
      LocalDate fromDate,
      LocalDate toDate,
      Integer numberDays) {
    Set<Sprint> sprintSet = new HashSet<>();
    if (fromDate == null
        || toDate == null
        || toDate.isBefore(fromDate)
        || numberDays == null
        || numberDays <= 0) {
      return sprintSet;
    }

    int defaultSequence = 1;
    if (project != null) {
      defaultSequence = project.getSprintList().size() + 1;
    } else if (projectVersion != null) {
      defaultSequence = projectVersion.getSprintList().size() + 1;
    }

    int i = 0;
    while (!fromDate.isAfter(toDate)) {
      project = project != null ? projectRepository.find(project.getId()) : null;
      projectVersion =
          projectVersion != null ? projectVersionRepository.find(projectVersion.getId()) : null;

      i++;

      LocalDate endDate = fromDate.plusDays(numberDays - 1);
      if (!toDate.isAfter(endDate)) {
        endDate = toDate;
      }
      sprintSet.add(createSprint(project, projectVersion, fromDate, endDate, defaultSequence));
      fromDate = endDate.plusDays(1);
      defaultSequence++;

      if (i % AbstractBatch.FETCH_LIMIT == 0) {
        JPA.clear();
      }
    }

    return sprintSet;
  }

  @Override
  public List<Sprint> getProjectSprintList(Project project) {
    List<Sprint> sprintList = new ArrayList<>();
    if (Objects.equals(
        project.getSprintManagementSelect(), ProjectRepository.SPRINT_MANAGEMENT_PROJECT)) {
      sprintList = project.getSprintList();
    }
    if (Objects.equals(
        project.getSprintManagementSelect(), ProjectRepository.SPRINT_MANAGEMENT_VERSION)) {
      sprintList =
          project.getRoadmapSet().stream()
              .map(ProjectVersion::getSprintList)
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
    }
    return sprintList;
  }

  @Transactional(rollbackOn = Exception.class)
  protected Sprint createSprint(
      Project project,
      ProjectVersion projectVersion,
      LocalDate fromDate,
      LocalDate toDate,
      int sequence) {

    Sprint sprint = new Sprint();
    sprint.setProject(project);
    sprint.setTargetVersion(projectVersion);
    sprint.setFromDate(fromDate);
    sprint.setToDate(toDate);
    sprint.setSequence(sequence);
    sprint.setName(
        I18n.get("Sprint")
            + " "
            + DATE_FORMATTER.format(fromDate)
            + " - "
            + DATE_FORMATTER.format(toDate));

    return sprintRepository.save(sprint);
  }

  @CallMethod
  public List<Long> getSprintList(Long projectId) {
    List<Sprint> sprintList = new ArrayList<>();
    if (projectId != null) {
      Project project = projectRepository.find(projectId);
      sprintList = getProjectSprintList(project);
    }
    return sprintList.stream().map(Sprint::getId).collect(Collectors.toList());
  }
}
