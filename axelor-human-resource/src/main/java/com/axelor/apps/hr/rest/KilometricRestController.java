package com.axelor.apps.hr.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.rest.dto.KilometricGetRequest;
import com.axelor.apps.hr.rest.dto.KilometricResponse;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.translation.ITranslation;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/kilometric")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class KilometricRestController {
  @Operation(
      summary = "Compute distance between two cities",
      tags = {"Kilometric"})
  @Path("/distance")
  @GET
  @HttpExceptionHandler
  public Response computeDistance(KilometricGetRequest requestBody) throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(ExpenseLine.class).createAccess(ExpenseLine.class).check();

    String fromCity = requestBody.getFromCity();
    String toCity = requestBody.getToCity();

    return ResponseConstructor.build(
        Response.Status.OK,
        String.format(I18n.get(ITranslation.DISTANCE_BETWEEN_CITIES), fromCity, toCity),
        new KilometricResponse(
            Beans.get(KilometricService.class).computeDistance(fromCity, toCity)));
  }
}
