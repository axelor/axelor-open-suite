package com.axelor.apps.stock.rest;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.rest.dto.StockTrackingNumberPostRequest;
import com.axelor.apps.stock.rest.dto.StockTrackingNumberResponse;
import com.axelor.apps.stock.service.TrackingNumberService;
import com.axelor.apps.tool.api.HttpExceptionHandler;
import com.axelor.apps.tool.api.RequestValidator;
import com.axelor.apps.tool.api.ResponseConstructor;
import com.axelor.apps.tool.api.SecurityCheck;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/stock-tracking-number")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StockTrackingNumberRestController {

  /**
   * Create new tracking number for given product. Full path to request is
   * /ws/aos/stock-tracking-number/
   */
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createTrackingNumber(StockTrackingNumberPostRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().createAccess(TrackingNumber.class).check();

    TrackingNumber trackingNumber =
        Beans.get(TrackingNumberService.class)
            .generateTrackingNumber(
                requestBody.fetchProduct(),
                requestBody.fetchCompany(),
                Beans.get(AppBaseService.class).getTodayDate(requestBody.fetchCompany()),
                requestBody.getOrigin(),
                requestBody.getNotes());

    return ResponseConstructor.build(
        Response.Status.CREATED,
        "Resource successfully created",
        new StockTrackingNumberResponse(trackingNumber));
  }
}
