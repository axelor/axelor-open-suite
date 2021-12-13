/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.web;

import com.axelor.apps.project.service.ProjectActivityDashboardService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.time.LocalDate;

@Singleton
public class ProjectActivityDashboardController {

  public void getData(ActionRequest request, ActionResponse response) {
    LocalDate todayDate = LocalDate.now();
    response.setValues(
        Beans.get(ProjectActivityDashboardService.class)
            .getData(todayDate.minusDays(29), todayDate));
  }

  public void getPreviousData(ActionRequest request, ActionResponse response) {
    String fromDate = request.getContext().get("fromDate").toString();
    response.setValues(Beans.get(ProjectActivityDashboardService.class).getPreviousData(fromDate));
  }

  public void getNextData(ActionRequest request, ActionResponse response) {
    String toDate = request.getContext().get("toDate").toString();
    response.setValues(Beans.get(ProjectActivityDashboardService.class).getNextData(toDate));
  }
}
