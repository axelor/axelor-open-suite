package com.axelor.apps.supplychain.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.rest.dto.LogisticalFormResponse;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.PackagingLine;
import com.axelor.apps.supplychain.report.ITranslation;
import com.axelor.apps.supplychain.rest.dto.LogisticalFormStockMovePutRequest;
import com.axelor.apps.supplychain.service.LogisticalFormStockMoveService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/aos/logistical-form")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LogisticalFormSupplychainRestController {
  @Operation(
      summary = "Add stock move to logistical form",
      tags = {"Logistical forl"})
  @Path("/add-stock-move/{logisticalFormId}")
  @PUT
  @HttpExceptionHandler
  public Response addStockMove(
      @PathParam("logisticalFormId") Long logisticalFormId,
      LogisticalFormStockMovePutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .writeAccess(LogisticalForm.class)
        .readAccess(LogisticalForm.class)
        .readAccess(StockMove.class)
        .check();

    LogisticalForm logisticalForm =
        ObjectFinder.find(LogisticalForm.class, logisticalFormId, requestBody.getVersion());

    Beans.get(LogisticalFormStockMoveService.class)
        .addStockMoveToLogisticalForm(logisticalForm, requestBody.fetchStockMove());

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.LOGISTICAL_FORM_UPDATED),
        new LogisticalFormResponse(logisticalForm));
  }

  @Operation(
      summary = "Remove stock move to logistical form",
      tags = {"Logistical form"})
  @Path("/remove-stock-move/{logisticalFormId}")
  @PUT
  @HttpExceptionHandler
  public Response removeStockMove(
      @PathParam("logisticalFormId") Long logisticalFormId,
      LogisticalFormStockMovePutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .writeAccess(LogisticalForm.class)
        .readAccess(LogisticalForm.class)
        .readAccess(StockMove.class)
        .readAccess(Packaging.class)
        .readAccess(PackagingLine.class)
        .check();

    LogisticalForm logisticalForm =
        ObjectFinder.find(LogisticalForm.class, logisticalFormId, requestBody.getVersion());

    Beans.get(LogisticalFormStockMoveService.class)
        .removeStockMoveFromLogisticalForm(logisticalForm, requestBody.fetchStockMove());

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.LOGISTICAL_FORM_UPDATED),
        new LogisticalFormResponse(logisticalForm));
  }
}
