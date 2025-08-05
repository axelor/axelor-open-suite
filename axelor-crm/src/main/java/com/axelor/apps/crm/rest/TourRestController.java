/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.rest;

import com.axelor.apps.crm.db.Tour;
import com.axelor.apps.crm.service.TourService;
import com.axelor.apps.crm.translation.ITranslation;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/aos/tour")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TourRestController {
  @Operation(
      summary = "Validate tour",
      tags = {"Tour"})
  @Path("/validate/{tourId}")
  @PUT
  @HttpExceptionHandler
  public Response validate(@PathParam("tourId") Long tourId) {
    new SecurityCheck().writeAccess(Tour.class).createAccess(Tour.class).check();

    Tour tour = ObjectFinder.find(Tour.class, tourId, ObjectFinder.NO_VERSION);
    Beans.get(TourService.class).setValidated(tour);

    return ResponseConstructor.build(Response.Status.OK, I18n.get(ITranslation.TOUR_VALIDATED));
  }
}
