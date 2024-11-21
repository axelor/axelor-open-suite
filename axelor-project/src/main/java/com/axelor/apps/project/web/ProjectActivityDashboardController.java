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
package com.axelor.apps.project.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.project.service.ProjectActivityDashboardService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.time.LocalDate;

@Singleton
public class ProjectActivityDashboardController {

  public void getData(ActionRequest request, ActionResponse response) {

    try {
      LocalDate todayDate = LocalDate.now();
      Long projectId = (Long) request.getContext().get("id");
      response.setValues(
          Beans.get(ProjectActivityDashboardService.class)
              .getData(todayDate.minusDays(29), todayDate, projectId));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getPreviousData(ActionRequest request, ActionResponse response) {

    try {
      String startDate = request.getContext().get("startDate").toString();
      Long projectId = (Long) request.getContext().get("id");
      response.setValues(
          Beans.get(ProjectActivityDashboardService.class).getPreviousData(startDate, projectId));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getNextData(ActionRequest request, ActionResponse response) {
    try {
      String endDate = request.getContext().get("endDate").toString();
      Long projectId = (Long) request.getContext().get("id");
      response.setValues(
          Beans.get(ProjectActivityDashboardService.class).getNextData(endDate, projectId));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
