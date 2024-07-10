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
package com.axelor.apps.hr.web;

import com.axelor.apps.hr.service.project.PlannedTimeValueService;
import com.axelor.apps.project.db.ProjectConfig;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class ProjectConfigHRController {

  public void generateDefaultPlannedTimeValue(ActionRequest request, ActionResponse response) {
    ProjectConfig projectConfig = request.getContext().asType(ProjectConfig.class);
    if (projectConfig.getIsSelectionOnDisplayPlannedTime()) {
      PlannedTimeValueService plannedTimeValueService = Beans.get(PlannedTimeValueService.class);
      plannedTimeValueService.createAndSavePlannedTimeValue(
          projectConfig.getValueByDefaultOnDisplayPlannedTime());
    }
  }
}
