package com.axelor.apps.stock.rest;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.apps.stock.db.*;
import com.axelor.apps.stock.db.repo.*;
import com.axelor.apps.stock.rest.dto.StockMoveResponse;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.tool.api.ResponseBody;
import com.axelor.db.JpaSecurity;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import wslite.json.JSONException;
import wslite.json.JSONObject;

@Path("/aos/stock-move")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StockMoveRestController {

  @Path("/internal/")
  @POST
  public Response createInternalStockMove(JSONObject json) throws JSONException {
    if (!Beans.get(JpaSecurity.class).isPermitted(JpaSecurity.CAN_CREATE, StockMove.class)) {
      return Response.status(403)
          .type(MediaType.APPLICATION_JSON)
          .entity(new ResponseBody(403, "Unauthorized"))
          .build();
    }
    StockLocation fromStockLocation =
        Beans.get(StockLocationRepository.class)
            .find(Long.parseLong(json.get("idOriginStockLocation").toString()));
    StockLocation toStockLocation =
        Beans.get(StockLocationRepository.class)
            .find(Long.parseLong(json.get("idDestStockLocation").toString()));

    Product product =
        Beans.get(ProductRepository.class).find(Long.parseLong(json.get("idProduct").toString()));

    TrackingNumber trackNb = null;
    if (product.getTrackingNumberConfiguration() != null && json.containsKey("idTrackingNumber")) {
      trackNb =
          Beans.get(TrackingNumberRepository.class)
              .find(Long.parseLong(json.get("idTrackingNumber").toString()));
    }

    Company company =
        Beans.get(CompanyRepository.class).find(Long.parseLong(json.get("idCompany").toString()));

    Unit unit = Beans.get(UnitRepository.class).find(Long.parseLong(json.get("idUnit").toString()));

    BigDecimal movedQty = BigDecimal.valueOf(Long.parseLong(json.get("movedQty").toString()));

    try {
      StockMove stockmove =
          Beans.get(StockMoveService.class)
              .createStockMoveMobility(
                  fromStockLocation, toStockLocation, company, product, trackNb, movedQty, unit);

      int codeStatus = 201;
      StockMoveResponse objectBody = new StockMoveResponse(stockmove);
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
}
