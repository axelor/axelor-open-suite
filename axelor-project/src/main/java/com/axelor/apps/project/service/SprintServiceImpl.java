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
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.SprintPeriod;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.SprintPeriodRepository;
import com.axelor.apps.project.db.repo.SprintRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SprintServiceImpl implements SprintService {

  public ProjectRepository projectRepo;
  public SprintPeriodRepository sprintPeriodRepo;
  public SprintRepository sprintRepo;

  @Inject
  public SprintServiceImpl(
      ProjectRepository projectRepo,
      SprintPeriodRepository sprintPeriodRepo,
      SprintRepository sprintRepo) {

    this.projectRepo = projectRepo;
    this.sprintPeriodRepo = sprintPeriodRepo;
    this.sprintRepo = sprintRepo;
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
}
