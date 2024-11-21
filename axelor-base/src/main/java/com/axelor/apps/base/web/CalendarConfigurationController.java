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

import com.axelor.apps.base.db.CalendarConfiguration;
import com.axelor.apps.base.db.repo.CalendarConfigurationRepository;
import com.axelor.apps.base.ical.CalendarConfigurationService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class CalendarConfigurationController {

  public void createAction(ActionRequest request, ActionResponse response) {
    try {
      CalendarConfiguration calendarConfiguration =
          request.getContext().asType(CalendarConfiguration.class);
      calendarConfiguration =
          Beans.get(CalendarConfigurationRepository.class).find(calendarConfiguration.getId());

      Beans.get(CalendarConfigurationService.class).createEntryMenu(calendarConfiguration);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void deleteAction(ActionRequest request, ActionResponse response) {
    try {
      CalendarConfiguration calendarConfiguration =
          request.getContext().asType(CalendarConfiguration.class);
      calendarConfiguration =
          Beans.get(CalendarConfigurationRepository.class).find(calendarConfiguration.getId());

      Beans.get(CalendarConfigurationService.class).deleteEntryMenu(calendarConfiguration);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
