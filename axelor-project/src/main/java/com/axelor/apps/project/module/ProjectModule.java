/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.project.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.project.db.repo.ProjectManagementRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.TeamProjectRepository;
import com.axelor.apps.project.db.repo.TeamTaskProjectRepository;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.apps.project.service.ProjectServiceImpl;
import com.axelor.apps.project.service.TeamTaskService;
import com.axelor.apps.project.service.TeamTaskServiceImpl;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.project.service.app.AppProjectServiceImpl;
import com.axelor.team.db.repo.TeamRepository;
import com.axelor.team.db.repo.TeamTaskRepository;

public class ProjectModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(ProjectRepository.class).to(ProjectManagementRepository.class);
    bind(AppProjectService.class).to(AppProjectServiceImpl.class);
    bind(TeamTaskRepository.class).to(TeamTaskProjectRepository.class);
    bind(ProjectService.class).to(ProjectServiceImpl.class);
    bind(TeamTaskService.class).to(TeamTaskServiceImpl.class);
    bind(TeamRepository.class).to(TeamProjectRepository.class);
  }
}
