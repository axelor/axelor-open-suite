/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.rest.dto.AddressPostRequest;
import com.axelor.apps.base.rest.dto.AddressResponse;
import com.axelor.apps.base.service.address.AddressCreationService;
import com.axelor.apps.base.service.address.CityService;
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
      CityService cityService = Beans.get(CityService.class);
      String cityName = requestBody.getCity();
      String zip = requestBody.getZip();
      City city = cityService.fetchCity(country, cityName, zip).orElse(null);
      String streetName = requestBody.getStreetName();

      AddressCreationService addressCreationService = Beans.get(AddressCreationService.class);
      Optional<Address> address =
          addressCreationService.fetchAddress(country, city, zip, streetName);

      if (address.isEmpty()) {
        if (city == null) {
          if (cityName != null) {
            city = cityService.createAndSaveCity(cityName, zip, country);
          } else {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_INCONSISTENCY,
                I18n.get(BaseExceptionMessage.NO_ADDRESS_FOUND_WITH_INFO));
          }
        }
        return ResponseConstructor.build(
            Response.Status.CREATED,
            I18n.get("Address created"),
            new AddressResponse(
                addressCreationService.createAndSaveAddress(country, city, zip, streetName)));
      }
      return ResponseConstructor.build(
          Response.Status.OK, I18n.get("Address found"), new AddressResponse(address.get()));
    } catch (AxelorException e) {
      return ResponseConstructor.build(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
