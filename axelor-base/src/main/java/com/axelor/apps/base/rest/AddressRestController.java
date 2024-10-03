package com.axelor.apps.base.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.rest.dto.AddressPostRequest;
import com.axelor.apps.base.rest.dto.AddressResponse;
import com.axelor.apps.base.service.address.AddressCreationService;
import com.axelor.apps.base.service.address.CountryService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Optional;
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
      CountryService countryService = Beans.get(CountryService.class);
      String countryCode = requestBody.getCountry();
      Country country =
          countryService
              .fetchCountry(countryCode)
              .orElse(countryService.createAndSaveCountry(countryCode, countryCode));

      AddressCreationService addressCreationService = Beans.get(AddressCreationService.class);
      Address address =
          addressCreationService.fetchAddress(
              country,
              requestBody.fetchCity(country),
              requestBody.getZip(),
              requestBody.getStreetName());

      if (address == null) {
      Optional<Address> address = requestBody.fetchAddress();
      if (address.isEmpty()) {
        return ResponseConstructor.build(
            Response.Status.CREATED,
            I18n.get("Address created"),
            new AddressResponse(
                addressCreationService.createAndSaveAddress(
                    country,
                    requestBody.fetchCity(country),
                    requestBody.getZip(),
                    requestBody.getStreetName())));
      }
      return ResponseConstructor.build(
          Response.Status.OK, I18n.get("Address found"), new AddressResponse(address.get()));
    } catch (AxelorException e) {
      return ResponseConstructor.build(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
