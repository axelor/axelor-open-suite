package com.axelor.apps.crm.rest;

import com.axelor.apps.crm.db.Tour;
import com.axelor.apps.crm.service.TourService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
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

    return ResponseConstructor.build(Response.Status.OK, "Tour successfully validated.");
  }
}
