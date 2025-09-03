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
package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.rest.dto.ProjectPlanningTimeRestrictedValueResponse;
import com.axelor.apps.project.db.PlannedTimeValue;
import com.axelor.apps.project.db.ProjectConfig;
import com.axelor.apps.project.service.config.ProjectConfigService;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectPlanningTimeResponseComputeServiceImpl
    implements ProjectPlanningTimeResponseComputeService {

  protected ProjectConfigService projectConfigService;

  @Inject
  public ProjectPlanningTimeResponseComputeServiceImpl(ProjectConfigService projectConfigService) {
    this.projectConfigService = projectConfigService;
  }

  @Override
  public ProjectPlanningTimeRestrictedValueResponse computeProjectPlanningTimeResponse(
      Company company) throws AxelorException {
    ProjectConfig projectConfig = projectConfigService.getProjectConfig(company);
    boolean isSelectionOnDisplayPlannedTime = projectConfig.getIsSelectionOnDisplayPlannedTime();

    if (isSelectionOnDisplayPlannedTime) {
      List<Long> plannedTimeValueIdList =
          projectConfig.getPlannedTimeValueList().stream()
              .map(PlannedTimeValue::getId)
              .collect(Collectors.toList());
      return new ProjectPlanningTimeRestrictedValueResponse(
          projectConfig.getVersion(), true, plannedTimeValueIdList);
    }
    return new ProjectPlanningTimeRestrictedValueResponse(projectConfig.getVersion(), false, null);
  }
}
