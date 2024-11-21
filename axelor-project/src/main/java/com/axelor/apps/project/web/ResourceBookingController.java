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

import com.axelor.apps.project.db.ResourceBooking;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.ResourceBookingService;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ResourceBookingController {

  public void checkIfResourceBooked(ActionRequest request, ActionResponse response) {
    if (Beans.get(AppProjectService.class).getAppProject().getCheckResourceAvailibility()) {
      ResourceBooking resourceBooking = request.getContext().asType(ResourceBooking.class);
      if (resourceBooking.getFromDate() != null
          && resourceBooking.getToDate() != null
          && Beans.get(ResourceBookingService.class).checkIfResourceBooked(resourceBooking)) {
        response.setError(I18n.get(ProjectExceptionMessage.RESOURCE_ALREADY_BOOKED_ERROR_MSG));
      }
    }
  }
}
