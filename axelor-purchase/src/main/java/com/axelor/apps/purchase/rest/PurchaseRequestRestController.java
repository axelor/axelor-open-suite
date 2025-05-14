/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.purchase.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.db.PurchaseRequestLine;
import com.axelor.apps.purchase.db.repo.PurchaseRequestRepository;
import com.axelor.apps.purchase.rest.dto.PurchaseRequestLineRequest;
import com.axelor.apps.purchase.rest.dto.PurchaseRequestPostRequest;
import com.axelor.apps.purchase.rest.dto.PurchaseRequestResponse;
import com.axelor.apps.purchase.service.PurchaseRequestLineService;
import com.axelor.apps.purchase.service.PurchaseRequestRestService;
import com.axelor.apps.purchase.service.PurchaseRequestWorkflowService;
import com.axelor.apps.purchase.translation.ITranslation;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/purchase-request")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PurchaseRequestRestController {

  @Operation(
      summary = "Request purchase request",
      tags = {"Request"})
  @Path("/request/{id}")
  @PUT
  @HttpExceptionHandler
  public Response requestPurchaseRequest(
      @PathParam("id") Long purchaseRequestId, RequestStructure requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(PurchaseRequest.class, purchaseRequestId).check();

    PurchaseRequest purchaseRequest =
        ObjectFinder.find(PurchaseRequest.class, purchaseRequestId, requestBody.getVersion());
    Beans.get(PurchaseRequestWorkflowService.class).requestPurchaseRequest(purchaseRequest);

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.PURCHASE_REQUEST_UPDATED),
        new PurchaseRequestResponse(purchaseRequest));
  }

  @Operation(
      summary = "Accept purchase request",
      tags = {"Accept"})
  @Path("/accept/{id}")
  @PUT
  @HttpExceptionHandler
  public Response acceptPurchaseRequest(
      @PathParam("id") Long purchaseRequestId, RequestStructure requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(PurchaseRequest.class, purchaseRequestId).check();

    PurchaseRequest purchaseRequest =
        ObjectFinder.find(PurchaseRequest.class, purchaseRequestId, requestBody.getVersion());
    Beans.get(PurchaseRequestWorkflowService.class).acceptPurchaseRequest(purchaseRequest);

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.PURCHASE_REQUEST_UPDATED),
        new PurchaseRequestResponse(purchaseRequest));
  }

  @Operation(
      summary = "Refuse purchase request",
      tags = {"Refuse"})
  @Path("/refuse/{id}")
  @PUT
  @HttpExceptionHandler
  public Response refusePurchaseRequest(
      @PathParam("id") Long purchaseRequestId, RequestStructure requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(PurchaseRequest.class, purchaseRequestId).check();

    PurchaseRequest purchaseRequest =
        ObjectFinder.find(PurchaseRequest.class, purchaseRequestId, requestBody.getVersion());
    Beans.get(PurchaseRequestWorkflowService.class).refusePurchaseRequest(purchaseRequest);

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.PURCHASE_REQUEST_UPDATED),
        new PurchaseRequestResponse(purchaseRequest));
  }

  @Operation(
      summary = "Cancel purchase request",
      tags = {"Cancel"})
  @Path("/cancel/{id}")
  @PUT
  @HttpExceptionHandler
  public Response cancelPurchaseRequest(
      @PathParam("id") Long purchaseRequestId, RequestStructure requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(PurchaseRequest.class, purchaseRequestId).check();

    PurchaseRequest purchaseRequest =
        ObjectFinder.find(PurchaseRequest.class, purchaseRequestId, requestBody.getVersion());
    Beans.get(PurchaseRequestWorkflowService.class).cancelPurchaseRequest(purchaseRequest);

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.PURCHASE_REQUEST_UPDATED),
        new PurchaseRequestResponse(purchaseRequest));
  }

  @Operation(
      summary = "Create purchase request",
      tags = {"Create"})
  @Path("/create")
  @POST
  @HttpExceptionHandler
  public Response createPurchaseRequest(PurchaseRequestPostRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .createAccess(PurchaseRequest.class)
        .writeAccess(PurchaseRequest.class)
        .createAccess(PurchaseRequestLine.class)
        .check();

    if (requestBody.getStatus() != null
        && requestBody.getStatus() > PurchaseRequestRepository.STATUS_REQUESTED) {
      return ResponseConstructor.build(
          Response.Status.BAD_REQUEST, I18n.get(ITranslation.PURCHASE_REQUEST_CREATE_WRONG_STATUS));
    }
    PurchaseRequest purchaseRequest =
        Beans.get(PurchaseRequestRestService.class).createPurchaseRequest(requestBody);

    return ResponseConstructor.buildCreateResponse(
        purchaseRequest, new PurchaseRequestResponse(purchaseRequest));
  }

  @Operation(
      summary = "Create purchase request line",
      tags = {"Create line"})
  @Path("/add-line/{id}")
  @PUT
  @HttpExceptionHandler
  public Response createPurchaseRequestLine(
      @PathParam("id") Long purchaseRequestId, PurchaseRequestLineRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .createAccess(PurchaseRequestLine.class)
        .writeAccess(PurchaseRequest.class, purchaseRequestId)
        .check();

    if (requestBody.fetchProduct() == null && StringUtils.isEmpty(requestBody.getProductTitle())) {
      return ResponseConstructor.build(
          Response.Status.BAD_REQUEST,
          I18n.get(ITranslation.MISSING_PRODUCT_INFORMATION_FOR_PURCHASE_REQUEST_LINE));
    }

    PurchaseRequest purchaseRequest =
        ObjectFinder.find(PurchaseRequest.class, purchaseRequestId, requestBody.getVersion());

    Beans.get(PurchaseRequestLineService.class)
        .createPurchaseRequestLine(
            purchaseRequest,
            requestBody.fetchProduct(),
            requestBody.getProductTitle(),
            requestBody.fetchUnit(),
            requestBody.getQuantity());

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.PURCHASE_REQUEST_UPDATED),
        new PurchaseRequestResponse(purchaseRequest));
  }

  @Operation(
      summary = "Draft purchase request",
      tags = {"Draft"})
  @Path("/draft/{id}")
  @PUT
  @HttpExceptionHandler
  public Response draftPurchaseRequest(
      @PathParam("id") Long purchaseRequestId, RequestStructure requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(PurchaseRequest.class, purchaseRequestId).check();

    PurchaseRequest purchaseRequest =
        ObjectFinder.find(PurchaseRequest.class, purchaseRequestId, requestBody.getVersion());
    Beans.get(PurchaseRequestWorkflowService.class).draftPurchaseRequest(purchaseRequest);

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.PURCHASE_REQUEST_UPDATED),
        new PurchaseRequestResponse(purchaseRequest));
  }
}
