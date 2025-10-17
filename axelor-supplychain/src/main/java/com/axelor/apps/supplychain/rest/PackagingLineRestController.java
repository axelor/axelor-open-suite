package com.axelor.apps.supplychain.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.PackagingLine;
import com.axelor.apps.supplychain.report.ITranslation;
import com.axelor.apps.supplychain.rest.dto.PackagingLinePostRequest;
import com.axelor.apps.supplychain.rest.dto.PackagingLinePutRequest;
import com.axelor.apps.supplychain.rest.dto.PackagingLineResponse;
import com.axelor.apps.supplychain.service.LogisticalFormComputeService;
import com.axelor.apps.supplychain.service.packaging.PackagingLineCreationService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/aos/packaging-line")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PackagingLineRestController {

  @Operation(
      summary = "Create packaging line",
      tags = {"Packaging line"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createPackagingLine(PackagingLinePostRequest requestBody) throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .createAccess(PackagingLine.class)
        .readAccess(StockMoveLine.class)
        .readAccess(Packaging.class)
        .check();

    PackagingLineCreationService packagingLineCreationService =
        Beans.get(PackagingLineCreationService.class);
    StockMoveLine stockMoveLine = requestBody.fetchStockMoveLine();
    PackagingLine packagingLine =
        packagingLineCreationService.createPackagingLine(
            requestBody.fetchPackaging(), stockMoveLine, requestBody.getQuantity());
    LogisticalForm logisticalForm =
        packagingLineCreationService.getParentLogisticalForm(packagingLine);
    Beans.get(LogisticalFormComputeService.class).computeLogisticalForm(logisticalForm);

    return ResponseConstructor.buildCreateResponse(
        packagingLine, new PackagingLineResponse(packagingLine));
  }

  @Operation(
      summary = "Update packaging line quantity",
      tags = {"Packaging line"})
  @Path("/update-quantity/{packagingLineId}")
  @PUT
  @HttpExceptionHandler
  public Response updateQty(
      @PathParam("packagingLineId") Long packagingLineId, PackagingLinePutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .writeAccess(PackagingLine.class)
        .readAccess(StockMoveLine.class)
        .writeAccess(StockMoveLine.class)
        .check();

    PackagingLine packagingLine =
        ObjectFinder.find(PackagingLine.class, packagingLineId, requestBody.getVersion());

    Beans.get(PackagingLineCreationService.class)
        .updateQuantity(packagingLine, requestBody.getQuantity());

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.PACKAGING_LINE_UPDATE),
        new PackagingLineResponse(packagingLine));
  }

  @Operation(
      summary = "Delete packaging line",
      tags = {"Packaging line"})
  @Path("/{packagingLineId}")
  @DELETE
  @HttpExceptionHandler
  public Response deletePackagingLine(@PathParam("packagingLineId") Long packagingLineId)
      throws AxelorException {
    new SecurityCheck().removeAccess(PackagingLine.class).check();

    PackagingLine packagingLine =
        ObjectFinder.find(PackagingLine.class, packagingLineId, ObjectFinder.NO_VERSION);

    Beans.get(PackagingLineCreationService.class).deletePackagingLine(packagingLine);

    return ResponseConstructor.build(
        Response.Status.OK, I18n.get(ITranslation.PACKAGING_LINE_DELETE));
  }
}
