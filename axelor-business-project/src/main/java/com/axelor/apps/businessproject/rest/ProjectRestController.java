package com.axelor.apps.businessproject.rest;

import com.axelor.apps.businessproject.service.ProjectRestService;
import com.axelor.apps.project.db.Project;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
        Response.Status.OK, Beans.get(ProjectRestService.class).getProjectReportingValues(project));
  }
}
