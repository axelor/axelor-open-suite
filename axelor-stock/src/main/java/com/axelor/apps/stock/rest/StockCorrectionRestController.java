package com.axelor.apps.stock.rest;

import com.axelor.apps.stock.db.StockCorrection;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockCorrectionRepository;
import com.axelor.apps.stock.rest.dto.StockCorrectionCreateRequest;
import com.axelor.apps.stock.rest.dto.StockCorrectionResponse;
import com.axelor.apps.stock.service.StockCorrectionService;
import com.axelor.apps.tool.api.RequestValidator;
import com.axelor.apps.tool.api.ResponseBody;
import com.axelor.apps.tool.api.SecurityCheck;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.util.Arrays;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import wslite.json.JSONException;
import wslite.json.JSONObject;

@Path("/aos/stock-correction")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StockCorrectionRestController {

  /*
  @Path("/")
  @POST
  public Response createStockCorrection(Request request) {
    return Beans.get(StockCorrectionRestService).createStockCorrection(request);
  }*/

  @Path("/")
  @POST
  public Response createStockCorrection(StockCorrectionCreateRequest requestBody) {
    RequestValidator.validateBody(requestBody);

    new SecurityCheck().createAccess(Arrays.asList(StockCorrection.class, StockMove.class)).check();

    try {
      StockCorrection stockCorrection =
          Beans.get(StockCorrectionService.class)
              .generateStockCorrection(
                  requestBody.getStockLocation(),
                  requestBody.getProduct(),
                  requestBody.getTrackingNumber(),
                  requestBody.getRealQty(),
                  requestBody.getReason());

      if (requestBody.getStatus() == StockCorrectionRepository.STATUS_VALIDATED) {
        Beans.get(StockCorrectionService.class).validate(stockCorrection);
      }

      int codeStatus = 201;
      StockCorrectionResponse objectBody = new StockCorrectionResponse(stockCorrection);
      ResponseBody responseBody =
          new ResponseBody(codeStatus, "Resource successfully created", objectBody);
      return Response.status(codeStatus)
          .type(MediaType.APPLICATION_JSON)
          .entity(responseBody)
          .build();
    } catch (Exception e) {
      int codeStatus = 500;
      ResponseBody responseBody = new ResponseBody(codeStatus, "Error while creating resource");
      return Response.status(codeStatus)
          .type(MediaType.APPLICATION_JSON)
          .entity(responseBody)
          .build();
    }
  }

  @Path("/{id}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response saveStockCorrection(@PathParam("id") long idStockCorrection, JSONObject json)
      throws JSONException, AxelorException {
    StockCorrection stockCorrection =
        Beans.get(StockCorrectionRepository.class).find(idStockCorrection);

    String message = "";

    if (json.containsKey("realQty")) {
      BigDecimal realQty = BigDecimal.valueOf(Long.parseLong(json.get("realQty").toString()));
      Beans.get(StockCorrectionService.class).updateCorrectionQtys(stockCorrection, realQty);
      message += "real qty updated; ";
    }

    boolean success = true;

    // Stock correction is not already validated
    if (stockCorrection.getStatusSelect() != StockCorrectionRepository.STATUS_VALIDATED
        && json.containsKey("status")) {
      int status = Integer.parseInt(json.get("status").toString());
      // user wants to validate stock correction
      if (status == StockCorrectionRepository.STATUS_VALIDATED) {
        if (Beans.get(StockCorrectionService.class).validate(stockCorrection)) {
          message += "status updated; ";
        } else {
          success = false;
        }
      }
    }

    if (success) {
      int codeStatus = 200;
      StockCorrectionResponse objectBody = new StockCorrectionResponse(stockCorrection);
      ResponseBody responseBody = new ResponseBody(codeStatus, message, objectBody);
      return Response.status(codeStatus)
          .type(MediaType.APPLICATION_JSON)
          .entity(responseBody)
          .build();
    } else {
      int codeStatus = 500;
      ResponseBody responseBody =
          new ResponseBody(codeStatus, message + "Error while updating status");
      return Response.status(codeStatus)
          .type(MediaType.APPLICATION_JSON)
          .entity(responseBody)
          .build();
    }
  }
}
