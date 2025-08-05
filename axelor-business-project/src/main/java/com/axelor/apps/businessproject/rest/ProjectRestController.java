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
package com.axelor.apps.businessproject.rest;

import com.axelor.apps.businessproject.service.ProjectRestService;
import com.axelor.apps.businessproject.translation.ITranslation;
import com.axelor.apps.project.db.Project;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/aos/project")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectRestController {
  @Operation(
      summary = "Get project reporting indicators",
      tags = {"Project"})
  @Path("/reporting-values/{projectId}")
  @GET
  @HttpExceptionHandler
  public Response getProjectReportingValues(@PathParam("projectId") Long projectId) {
    new SecurityCheck().readAccess(Project.class, projectId).check();
    Project project = ObjectFinder.find(Project.class, projectId, ObjectFinder.NO_VERSION);

    return ResponseConstructor.build(
        Response.Status.OK,
        String.format(I18n.get(ITranslation.REPORTING_VALUES_FOR_PROJECT), projectId),
        Beans.get(ProjectRestService.class).getProjectReportingValues(project));
  }
}
