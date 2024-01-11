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
package com.axelor.apps.hr.web.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeService;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;

@Singleton
public class ProjectPlanningTimeController {

  public void addMultipleProjectPlanningTime(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Context context = request.getContext();
    Beans.get(ProjectPlanningTimeService.class).addMultipleProjectPlanningTime(context);

    response.setCanClose(true);
  }

  /**
   * Invert value of 'isIncludeInTuronverForecast' field and save the record.
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  @Transactional
  public void updateIsIncludeInTuronverForecast(ActionRequest request, ActionResponse response) {

    try {
      ProjectPlanningTime projectPlanningTime =
          request.getContext().asType(ProjectPlanningTime.class);

      projectPlanningTime =
          Beans.get(ProjectPlanningTimeRepository.class).find(projectPlanningTime.getId());

      projectPlanningTime.setIsIncludeInTurnoverForecast(
          !projectPlanningTime.getIsIncludeInTurnoverForecast());

      Beans.get(ProjectPlanningTimeRepository.class).save(projectPlanningTime);

      response.setValue(
          "isIncludeInTurnoverForecast", projectPlanningTime.getIsIncludeInTurnoverForecast());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void removeProjectPlanningTime(ActionRequest request, ActionResponse response) {

    List<Map<String, Object>> projectPlanningTimeLines =
        (List<Map<String, Object>>) request.getContext().get("projectPlanningTimeSet");

    if (projectPlanningTimeLines != null) {
      Beans.get(ProjectPlanningTimeService.class)
          .removeProjectPlanningLines(projectPlanningTimeLines);
    }

    response.setReload(true);
  }
}
