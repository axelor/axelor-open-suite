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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.repo.EventsPlanningRepository;
import com.axelor.apps.base.service.publicHoliday.PublicHolidayService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EventsPlanningController {

  public void generateEventsPlanningLines(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    LocalDate startDate =
        LocalDate.parse(context.get("startDate").toString(), DateTimeFormatter.ISO_DATE);
    LocalDate endDate =
        LocalDate.parse(context.get("endDate").toString(), DateTimeFormatter.ISO_DATE);
    String description = context.get("description").toString();

    EventsPlanning eventsPlanning =
        Beans.get(EventsPlanningRepository.class)
            .find(Long.valueOf(context.get("_eventsPlanning").toString()));

    Beans.get(PublicHolidayService.class)
        .generateEventsPlanningLines(eventsPlanning, startDate, endDate, description);
  }
}
