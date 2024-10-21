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
package com.axelor.apps.project.service.sprint;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.project.db.AllocationPeriod;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.db.repo.SprintRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class SprintServiceImpl implements SprintService {

  protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d");

  protected ProjectTaskRepository projectTaskRepo;
  protected SprintRepository sprintRepo;
  protected AllocationPeriodService allocationPeriodService;

  @Inject
  public SprintServiceImpl(
      ProjectTaskRepository projectTaskRepo,
      SprintRepository sprintRepo,
      AllocationPeriodService allocationPeriodService) {

    this.projectTaskRepo = projectTaskRepo;
    this.sprintRepo = sprintRepo;
    this.allocationPeriodService = allocationPeriodService;
  }

  @Override
  public LocalDate computeFromDate(Sprint sprint) {

    Set<AllocationPeriod> allocationPeriodSet = sprint.getAllocationPeriodSet();

    if (CollectionUtils.isEmpty(allocationPeriodSet)) {
      return null;
    }

    return allocationPeriodSet.stream()
        .map(AllocationPeriod::getFromDate)
        .filter(Objects::nonNull)
        .min(LocalDate::compareTo)
        .orElse(null);
  }

  @Override
  public LocalDate computeToDate(Sprint sprint) {

    Set<AllocationPeriod> allocationPeriodSet = sprint.getAllocationPeriodSet();

    if (CollectionUtils.isEmpty(allocationPeriodSet)) {
      return null;
    }

    return allocationPeriodSet.stream()
        .map(AllocationPeriod::getToDate)
        .filter(Objects::nonNull)
        .max(LocalDate::compareTo)
        .orElse(null);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void attachTasksToSprint(Sprint sprint, List<ProjectTask> projectTasks) {

    projectTasks.stream()
        .forEach(
            task -> {
              task.setSprint(sprint);
              projectTaskRepo.save(task);
            });
  }

  @Override
  public Set<Sprint> generateSprints(
      Company company, Set<Project> projectSet, LocalDate fromDate, LocalDate toDate) {

    Set<Sprint> sprintSet = new HashSet<>();

    if (CollectionUtils.isNotEmpty(projectSet)) {
      Set<AllocationPeriod> allocationPeriodSet =
          allocationPeriodService.generateAllocationPeriods(company, fromDate, toDate, 1);

      for (Project project : projectSet) {
        sprintSet.add(createSprint(project, allocationPeriodSet, fromDate, toDate));
      }
    }

    return sprintSet;
  }

  @Transactional(rollbackOn = Exception.class)
  protected Sprint createSprint(
      Project project,
      Set<AllocationPeriod> allocationPeriodSet,
      LocalDate fromDate,
      LocalDate toDate) {

    Sprint sprint = new Sprint();
    sprint.setProject(project);
    sprint.setAllocationPeriodSet(allocationPeriodSet);
    sprint.setFromDate(fromDate);
    sprint.setToDate(toDate);
    sprint.setName(
        I18n.get("Sprint")
            + " "
            + DATE_FORMATTER.format(fromDate)
            + " - "
            + DATE_FORMATTER.format(toDate));

    return sprintRepo.save(sprint);
  }
}
