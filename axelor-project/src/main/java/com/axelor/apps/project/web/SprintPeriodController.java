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

import com.axelor.apps.project.service.SprintPeriodService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDate;
import java.util.LinkedHashMap;

public class SprintPeriodController {

  @SuppressWarnings("unchecked")
  public void sprintPeriodGenerate(ActionRequest request, ActionResponse response) {

    Object companyContext = request.getContext().get("company");
    Object fromDateContext = request.getContext().get("fromDate");
    Object toDateContext = request.getContext().get("toDate");
    Object nbOfDaysPerSprintContext = request.getContext().get("nbOfDaysPerSprint");
    Object considerWeekendContext = request.getContext().get("considerWeekend");

    if (companyContext != null
        && fromDateContext != null
        && toDateContext != null
        && nbOfDaysPerSprintContext != null) {

      Long companyId =
          Long.valueOf(((LinkedHashMap<String, Object>) companyContext).get("id").toString());
      LocalDate fromDate = LocalDate.parse(fromDateContext.toString());
      LocalDate toDate = LocalDate.parse(toDateContext.toString());
      int nbOfDaysPerSprint = (Integer) nbOfDaysPerSprintContext;
      boolean considerWeekend = Boolean.TRUE.equals(considerWeekendContext);

      Beans.get(SprintPeriodService.class)
          .sprintPeriodGenerate(companyId, fromDate, toDate, nbOfDaysPerSprint, considerWeekend);
      response.setInfo(I18n.get("Periods created"));
    }

    response.setReload(true);
  }
}
