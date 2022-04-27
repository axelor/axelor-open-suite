package com.axelor.apps.stock.rest;

import com.axelor.apps.stock.db.StockCorrection;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockCorrectionRepository;
import com.axelor.apps.stock.rest.dto.StockCorrectionCreateRequest;
import com.axelor.apps.stock.rest.dto.StockCorrectionResponse;
import com.axelor.apps.stock.rest.dto.StockCorrectionUpdateRequest;
import com.axelor.apps.stock.service.StockCorrectionService;
import com.axelor.apps.tool.api.*;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import java.util.Arrays;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/stock-correction")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StockCorrectionRestController {

  /*
  // Proposition de structure de controller
  @Path("/")
  @POST
  public Response createStockCorrection(Request request) {
    return Beans.get(StockCorrectionRestService).createStockCorrection(request);
  }*/

  @Path("/")
  @POST
  public Response createStockCorrection(StockCorrectionCreateRequest requestBody) {
    try {
      RequestValidator.validateBody(requestBody);
      new SecurityCheck().createAccess(Arrays.asList(StockCorrection.class, StockMove.class)).check();

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

      return ResponseConstructor.build(201, "Resource successfully created", new StockCorrectionResponse(stockCorrection));
  } catch (ForbiddenException e) {
    TraceBackService.trace(e);
      return ResponseConstructor.build(403, e.getMessage(), null);
  } catch (Exception e) {
      TraceBackService.trace(e);
      return ResponseConstructor.build(500, "Error while creating resource", null);
    }
  }

  @Path("/{id}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response saveStockCorrection(
      @PathParam("id") long idStockCorrection, StockCorrectionUpdateRequest requestBody) {
    try {
      RequestValidator.validateBody(requestBody);
      new SecurityCheck().writeAccess(StockCorrection.class).createAccess(StockMove.class).check();

      StockCorrection stockCorrection = ObjectFinder.find(StockCorrection.class, idStockCorrection);

      String message = "";
      if (requestBody.getRealQty() != null) {
        Beans.get(StockCorrectionService.class)
            .updateCorrectionQtys(stockCorrection, requestBody.getRealQty());
        message += "real qty updated; ";
      }

      // Stock correction is not already validated
      if (stockCorrection.getStatusSelect() != StockCorrectionRepository.STATUS_VALIDATED
          && requestBody.getStatus() != null) {
        int status = requestBody.getStatus();
        // user wants to validate stock correction
        if (status == StockCorrectionRepository.STATUS_VALIDATED) {
          if (Beans.get(StockCorrectionService.class).validate(stockCorrection)) {
            message += "status updated; ";
          }
        }
      }

      StockCorrectionResponse objectBody = new StockCorrectionResponse(stockCorrection);
      return ResponseConstructor.build(200, message, objectBody);
    } catch (ForbiddenException e) {
      TraceBackService.trace(e);
      return ResponseConstructor.build(403, e.getMessage(), null);
    } catch (NotFoundException e) {
      TraceBackService.trace(e);
      return ResponseConstructor.build(404, e.getMessage(), null);
    } catch (Exception e) {
      TraceBackService.trace(e);
      return ResponseConstructor.build(500, "Error while creating resource", null);
    }
  }
}
