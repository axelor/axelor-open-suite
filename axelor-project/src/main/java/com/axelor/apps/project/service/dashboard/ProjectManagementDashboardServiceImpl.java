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
package com.axelor.apps.project.service.dashboard;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ProjectManagementDashboardServiceImpl implements ProjectManagementDashboardService {
  protected ProjectTaskRepository projectTaskRepo;
  protected ProjectRepository projectRepo;
  protected AppBaseService appBaseService;

  @Inject
  public ProjectManagementDashboardServiceImpl(
      ProjectTaskRepository projectTaskRepo,
      ProjectRepository projectRepo,
      AppBaseService appBaseService) {
    this.projectTaskRepo = projectTaskRepo;
    this.projectRepo = projectRepo;

    this.appBaseService = appBaseService;
  }

  @Override
  public Map<String, Object> getDate() {
    Map<String, Object> dataMap = new HashMap<>();
    LocalDate todayDate = LocalDate.now();
    dataMap.put("$fromDate", todayDate);
    dataMap.put("$toDate", todayDate.plusDays(7));

    return dataMap;
  }
}
