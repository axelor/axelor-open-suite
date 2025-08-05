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
package com.axelor.apps.base.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.rest.dto.PartnerPostRequest;
import com.axelor.apps.base.rest.dto.PartnerResponse;
import com.axelor.apps.base.service.partner.PartnerCreationService;
import com.axelor.apps.base.translation.ITranslation;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/aos/partner")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PartnerRestController {

  @Operation(
      summary = "Add partner address",
      tags = {"Add"})
  @Path("/{partnerId}/address/{addressId}")
  @PUT
  @HttpExceptionHandler
  public Response addPartnerAddress(
      @PathParam("partnerId") Long partnerId,
      @PathParam("addressId") Long addressId,
      RequestStructure requestBody) {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .createAccess(PartnerAddress.class)
        .writeAccess(Partner.class, partnerId)
        .readAccess(Address.class, addressId)
        .check();

    Partner partner = ObjectFinder.find(Partner.class, partnerId, requestBody.getVersion());
    Address address = ObjectFinder.find(Address.class, addressId, ObjectFinder.NO_VERSION);

    Beans.get(PartnerRestService.class).addPartnerAddress(partner, address);

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(ITranslation.PARTNER_ADDRESS_UPDATED),
        new PartnerResponse(partner));
  }

  @Operation(
      summary = "Create a partner",
      tags = {"Partner"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createPartner(PartnerPostRequest requestBody) throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().createAccess(Partner.class).readAccess(Partner.class).check();

    Partner partner =
        Beans.get(PartnerCreationService.class)
            .createPartner(
                requestBody.getPartnerTypeSelect(),
                requestBody.getTitleSelect(),
                requestBody.getFirstName(),
                requestBody.getName(),
                requestBody.fetchMainPartner(),
                requestBody.getDescription(),
                requestBody.getIsContact(),
                requestBody.getIsCustomer(),
                requestBody.getIsSupplier(),
                requestBody.getIsProspect());

    return ResponseConstructor.buildCreateResponse(partner, new PartnerResponse(partner));
  }
}
