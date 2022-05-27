/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businesssupport.service.web;

import com.axelor.apps.project.service.ProjectActivityDashboardService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Singleton
public class ProjectActivityDashboardController {

  public void getData(ActionRequest request, ActionResponse response) {
    LocalDate annoucementDate =
        LocalDate.parse(
            request.getContext().get("announcementDate").toString(), DateTimeFormatter.ISO_DATE);
    LocalDate toDate = annoucementDate.plusDays(30);
    LocalDate todayDate = LocalDate.now();
    if (todayDate.isBefore(toDate)) {
      toDate = todayDate;
    }
    response.setValues(
        Beans.get(ProjectActivityDashboardService.class).getData(annoucementDate, toDate, null));
  }
}
