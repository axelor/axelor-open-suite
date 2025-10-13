package com.axelor.apps.supplychain.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.report.ITranslation;
import com.axelor.apps.supplychain.rest.dto.PackagingPostRequest;
import com.axelor.apps.supplychain.rest.dto.PackagingPutRequest;
import com.axelor.apps.supplychain.rest.dto.PackagingResponse;
import com.axelor.apps.supplychain.service.packaging.PackagingCreateService;
import com.axelor.apps.supplychain.service.packaging.PackagingDeleteService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/packaging")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PackagingRestController {

  @Operation(
      summary = "Create packaging",
      tags = {"Packaging"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createPackaging(PackagingPostRequest requestBody) throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .createAccess(Packaging.class)
        .writeAccess(LogisticalForm.class)
        .readAccess(LogisticalForm.class)
        .readAccess(Product.class)
        .check();

    Packaging packaging =
        Beans.get(PackagingCreateService.class)
            .createPackaging(
                requestBody.fetchLogisticalForm(),
                requestBody.fetchPackaging(),
                requestBody.fetchPackageUsed());

    return ResponseConstructor.buildCreateResponse(packaging, new PackagingResponse(packaging));
  }

  @Operation(
      summary = "Update packaging package used",
      tags = {"Packaging"})
  @Path("/update-package-used/{packagingId}")
  @PUT
  @HttpExceptionHandler
  public Response updatePackageUsed(
      @PathParam("packagingId") Long packagingId, PackagingPutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(Packaging.class).readAccess(Product.class).check();

    Packaging packaging = ObjectFinder.find(Packaging.class, packagingId, ObjectFinder.NO_VERSION);

    Beans.get(PackagingCreateService.class)
        .updatePackageUsed(requestBody.fetchPackageUsed(), packaging);

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.PACKAGING_UPDATED),
        new PackagingResponse(packaging));
  }

  @Operation(
      summary = "Delete packaging",
      tags = {"Packaging"})
  @Path("/{packagingId}")
  @DELETE
  @HttpExceptionHandler
  public Response deletePackaging(@PathParam("packagingId") Long packagingId) {
    new SecurityCheck().removeAccess(Packaging.class);

    Packaging packaging = ObjectFinder.find(Packaging.class, packagingId, ObjectFinder.NO_VERSION);

    Beans.get(PackagingDeleteService.class).deletePackaging(packaging);

    return ResponseConstructor.build(Response.Status.OK, I18n.get(ITranslation.PACKAGING_DELETED));
  }
}
