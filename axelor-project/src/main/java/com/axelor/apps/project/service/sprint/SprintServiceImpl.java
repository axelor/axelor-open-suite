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

import com.axelor.apps.project.db.AllocationPeriod;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class SprintServiceImpl implements SprintService {

  protected ProjectTaskRepository projectTaskRepo;

  @Inject
  public SprintServiceImpl(ProjectTaskRepository projectTaskRepo) {

    this.projectTaskRepo = projectTaskRepo;
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
}
