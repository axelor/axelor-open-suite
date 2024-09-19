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
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.SprintAllocationLine;
import com.axelor.apps.project.db.SprintPeriod;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.SprintAllocationLineRepository;
import com.axelor.apps.project.db.repo.SprintPeriodRepository;
import com.axelor.apps.project.db.repo.SprintRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SprintServiceImpl implements SprintService {

  public ProjectRepository projectRepo;
  public SprintPeriodRepository sprintPeriodRepo;
  public SprintRepository sprintRepo;
  public SprintAllocationLineRepository sprintAllocationLineRepo;

  @Inject
  public SprintServiceImpl(
      ProjectRepository projectRepo,
      SprintPeriodRepository sprintPeriodRepo,
      SprintRepository sprintRepo,
      SprintAllocationLineRepository sprintAllocationLineRepo) {

    this.projectRepo = projectRepo;
    this.sprintPeriodRepo = sprintPeriodRepo;
    this.sprintRepo = sprintRepo;
    this.sprintAllocationLineRepo = sprintAllocationLineRepo;
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional
  public void sprintGenerate(Object projectsContext, Object sprintPeriodsContext) {

    Set<Project> projects =
        ((List<LinkedHashMap<String, Object>>) projectsContext)
            .stream()
                .map(project -> projectRepo.find(Long.valueOf(project.get("id").toString())))
                .collect(Collectors.toSet());

    Set<SprintPeriod> sprintPeriods =
        ((List<LinkedHashMap<String, Object>>) sprintPeriodsContext)
            .stream()
                .map(
                    sprintPeriod ->
                        sprintPeriodRepo.find(Long.valueOf(sprintPeriod.get("id").toString())))
                .collect(Collectors.toSet());

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");

    projects.forEach(
        project -> {
          sprintPeriods.forEach(
              sprintPeriod -> {
                Sprint sprint = new Sprint();
                sprint.setName(
                    I18n.get("Sprint")
                        + " "
                        + formatter.format(sprintPeriod.getFromDate())
                        + " to "
                        + formatter.format(sprintPeriod.getToDate()));
                sprint.setSprintPeriod(sprintPeriod);
                sprint.setProject(project);
                sprintRepo.save(sprint);
              });
        });
  }

  @Override
  public BigDecimal computeTotalAllocatedTime(Sprint sprint) {

    BigDecimal totalAllocatedTime = BigDecimal.ZERO;

    List<SprintAllocationLine> sprintAllocationLineList =
        sprintAllocationLineRepo.all().filter("self.sprint = ?1", sprint).fetch();

    if (CollectionUtils.isNotEmpty(sprintAllocationLineList)) {
      totalAllocatedTime =
          sprintAllocationLineList.stream()
              .map(SprintAllocationLine::getAllocated)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    return totalAllocatedTime;
  }

  @Override
  public BigDecimal computeTotalEstimatedTime(Sprint sprint) {

    BigDecimal totalEstimatedTime = BigDecimal.ZERO;

    List<ProjectTask> projectTaskList = sprint.getProjectTaskList();

    if (CollectionUtils.isNotEmpty(projectTaskList)) {
      totalEstimatedTime =
          projectTaskList.stream()
              .map(ProjectTask::getBudgetedTime)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    return totalEstimatedTime;
  }

  @Override
  public BigDecimal computeTotalPlannedTime(Sprint sprint) {

    BigDecimal totalPlannedTime = BigDecimal.ZERO;

    List<SprintAllocationLine> sprintAllocationLineList =
        sprintAllocationLineRepo.all().filter("self.sprint = ?1", sprint).fetch();

    if (CollectionUtils.isNotEmpty(sprintAllocationLineList)) {
      totalPlannedTime =
          sprintAllocationLineList.stream()
              .map(SprintAllocationLine::getPlannedTime)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    return totalPlannedTime;
  }

  @Override
  public BigDecimal computeTotalRemainingTime(Sprint sprint) {

    BigDecimal totalRemainingTime = BigDecimal.ZERO;

    List<SprintAllocationLine> sprintAllocationLineList =
        sprintAllocationLineRepo.all().filter("self.sprint = ?1", sprint).fetch();

    if (CollectionUtils.isNotEmpty(sprintAllocationLineList)) {
      totalRemainingTime =
          sprintAllocationLineList.stream()
              .map(SprintAllocationLine::getRemainingTime)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    return totalRemainingTime;
  }
}
