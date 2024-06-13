package com.axelor.apps.crm.rest;

import com.axelor.apps.crm.db.TourLine;
import com.axelor.apps.crm.service.TourLineService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/tour-line")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TourLineRestController {
  @Operation(
      summary = "Validate tour line",
      tags = {"Tour line"})
  @Path("/validate/{tourLineId}")
  @PUT
  @HttpExceptionHandler
  public Response validate(@PathParam("tourLineId") Long tourLineId) {
    new SecurityCheck().writeAccess(TourLine.class).createAccess(TourLine.class).check();

    TourLine tourLine = ObjectFinder.find(TourLine.class, tourLineId, ObjectFinder.NO_VERSION);
    Beans.get(TourLineService.class).setValidatedAndLastVisitDate(tourLine);

    return ResponseConstructor.build(Response.Status.OK, "Tour line successfully validated.");
  }
}
