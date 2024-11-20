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

import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.service.sprint.SprintService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDate;

public class SprintController {

  public void computeDates(ActionRequest request, ActionResponse response) {

    Sprint sprint = request.getContext().asType(Sprint.class);

    SprintService sprintService = Beans.get(SprintService.class);
    LocalDate fromDate = sprintService.computeFromDate(sprint);
    LocalDate toDate = sprintService.computeToDate(sprint);

    if (fromDate != null && toDate != null) {
      response.setValue("fromDate", fromDate);
      response.setValue("toDate", toDate);
    }
  }
}
