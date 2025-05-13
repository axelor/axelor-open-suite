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
package com.axelor.apps.hr.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.hr.rest.dto.ProjectUnitConversionComputeResponse;
import com.axelor.apps.hr.rest.dto.ProjectUnitConversionFilterResponse;
import com.axelor.apps.hr.rest.dto.ProjectUnitConversionPutRequest;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.service.UnitConversionForProjectService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import java.math.BigDecimal;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/project/conversion")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectUnitConversionRestController {
  @Operation(
      summary = "Filter selectable units",
      tags = {"Project"})
  @Path("/filter/{projectId}")
  @GET
  @HttpExceptionHandler
  public Response filterUnits(@PathParam("projectId") Long projectId) {
    new SecurityCheck().readAccess(Project.class, projectId).check();
    Project project = ObjectFinder.find(Project.class, projectId, ObjectFinder.NO_VERSION);

    List<Long> unitIdList =
        Beans.get(ProjectPlanningTimeService.class)
            .computeAvailableDisplayTimeUnitIds(project.getProjectTimeUnit());

    return ResponseConstructor.build(
        Response.Status.OK,
        new ProjectUnitConversionFilterResponse(project.getVersion(), unitIdList));
  }

  @Operation(
      summary = "Compute conversion units",
      tags = {"Project"})
  @Path("/compute/{projectId}")
  @PUT
  @HttpExceptionHandler
  public Response computeConversion(
      @PathParam("projectId") Long projectId, ProjectUnitConversionPutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().readAccess(Project.class, projectId).check();

    Project project = ObjectFinder.find(Project.class, projectId, ObjectFinder.NO_VERSION);
    Unit startUnit =
        ObjectFinder.find(Unit.class, requestBody.getStartingUnitId(), ObjectFinder.NO_VERSION);
    Unit endUnit =
        ObjectFinder.find(Unit.class, requestBody.getDestinationUnitId(), ObjectFinder.NO_VERSION);
    BigDecimal value = requestBody.getStartingValue();

    BigDecimal result =
        Beans.get(UnitConversionForProjectService.class)
            .convert(startUnit, endUnit, value, value.scale(), project);

    return ResponseConstructor.build(
        Response.Status.OK, new ProjectUnitConversionComputeResponse(project.getVersion(), result));
  }
}
