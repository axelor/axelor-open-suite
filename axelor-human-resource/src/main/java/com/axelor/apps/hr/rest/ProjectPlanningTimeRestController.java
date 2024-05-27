package com.axelor.apps.hr.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeResponseComputeService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/project-planning-time")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectPlanningTimeRestController {
  @Operation(
      summary = "Get planning time values restriction",
      tags = {"Project planning time"})
  @Path("/restrictions/{companyId}")
  @GET
  @HttpExceptionHandler
  public Response getPlannedTimeValuesRestriction(@PathParam("companyId") Long companyId)
      throws AxelorException {
    new SecurityCheck().readAccess(Company.class).check();
    Company company = ObjectFinder.find(Company.class, companyId, ObjectFinder.NO_VERSION);

    return ResponseConstructor.build(
        Response.Status.OK,
        Beans.get(ProjectPlanningTimeResponseComputeService.class)
            .computeProjectPlanningTimeResponse(company));
  }
}
