package com.axelor.apps.hr.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.hr.rest.dto.ProjectUnitConversionComputeResponse;
import com.axelor.apps.hr.rest.dto.ProjectUnitConversionFilterResponse;
import com.axelor.apps.hr.rest.dto.ProjectUnitConversionPutRequest;
import com.axelor.apps.hr.service.UnitConversionForProjectService;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeService;
import com.axelor.apps.project.db.Project;
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
    new SecurityCheck().readAccess(Project.class).readAccess(Unit.class).check();
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
    new SecurityCheck().readAccess(Project.class).readAccess(Unit.class).check();

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
