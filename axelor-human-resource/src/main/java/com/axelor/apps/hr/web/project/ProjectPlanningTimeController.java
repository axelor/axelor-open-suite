/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.web.project;

import com.axelor.apps.hr.service.project.ProjectPlanningTimeService;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Singleton
public class ProjectPlanningTimeController {

  public void showPlanning(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    Collection<Map<String, Object>> users =
        (Collection<Map<String, Object>>) context.get("userSet");

    String userIds = "";
    if (users != null) {
      for (Map<String, Object> user : users) {
        if (userIds.isEmpty()) {
          userIds = user.get("id").toString();
        } else {
          userIds += "," + user.get("id").toString();
        }
      }
    }

    ActionViewBuilder builder =
        ActionView.define(I18n.get("Project Planning time"))
            .model(ProjectPlanningTime.class.getName());
    String url = "project/planning";

    if (!userIds.isEmpty()) {
      url += "?userIds=" + userIds;
    }

    builder.add("html", url);

    response.setView(builder.map());
    response.setCanClose(true);
  }

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

  public void removeProjectPlanningTime(ActionRequest request, ActionResponse response) {

    List<Map<String, Object>> projectPlanningTimeLines =
        (List<Map<String, Object>>) request.getContext().get("projectPlanningTimeSet");

    if (projectPlanningTimeLines != null) {
      Beans.get(ProjectPlanningTimeService.class)
          .removeProjectPlanningLines(projectPlanningTimeLines);
    }

    response.setReload(true);
  }

  public void removeProjectPlanningTimeSpent(ActionRequest request, ActionResponse response) {

    List<Map<String, Object>> projectPlanningTimeSpentLines =
        (List<Map<String, Object>>) request.getContext().get("projectPlanningTimeSpentSet");

    if (projectPlanningTimeSpentLines != null) {
      Beans.get(ProjectPlanningTimeService.class)
          .removeProjectPlanningLines(projectPlanningTimeSpentLines);
    }

    response.setReload(true);
  }
}
