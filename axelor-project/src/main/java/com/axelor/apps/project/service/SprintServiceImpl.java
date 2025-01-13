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
import com.axelor.apps.project.db.ProjectVersion;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.SprintRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SprintServiceImpl implements SprintService {

  protected SprintRepository sprintRepository;

  @Inject
  public SprintServiceImpl(SprintRepository sprintRepository) {
    this.sprintRepository = sprintRepository;
  }

  @Override
  @Transactional
  public void generateBacklogSprint(Project project) {
    Sprint sprint = new Sprint("Backlog - " + project.getName());
    sprint.setProject(project);
    project.setBacklogSprint(sprint);
    sprintRepository.save(sprint);
  }

  @Override
  @Transactional
  public void generateBacklogSprint(ProjectVersion projectVersion) {
    Sprint sprint = new Sprint("Backlog - " + projectVersion.getTitle());
    sprint.setTargetVersion(projectVersion);
    projectVersion.setBacklogSprint(sprint);
    sprintRepository.save(sprint);
  }
}
