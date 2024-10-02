package com.axelor.apps.base.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.rest.dto.AddressPostRequest;
import com.axelor.apps.base.rest.dto.AddressResponse;
import com.axelor.apps.base.service.address.AddressCreationService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/address")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AddressRestController {
  @Operation(
      summary = "Create an address",
      tags = {"Address"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createAddress(AddressPostRequest requestBody) {
    new SecurityCheck()
        .readAccess(Country.class)
        .readAccess(City.class)
        .createAccess(Address.class);
    RequestValidator.validateBody(requestBody);

    try {
      Address address = requestBody.fetchAddress();
      if (address == null) {
        return ResponseConstructor.build(
            Response.Status.CREATED,
            I18n.get("Address created"),
            new AddressResponse(
                Beans.get(AddressCreationService.class)
                    .createAndSaveAddress(
                        requestBody.fetchCountry(),
                        requestBody.fetchCity(),
                        requestBody.getZip(),
                        requestBody.getStreetName())));
      }
      return ResponseConstructor.build(
          Response.Status.OK, I18n.get("Address found"), new AddressResponse(address));
    } catch (AxelorException e) {
      return ResponseConstructor.build(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
