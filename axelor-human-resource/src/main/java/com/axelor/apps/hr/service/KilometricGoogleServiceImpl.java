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
package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.MapGoogleService;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import jakarta.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import org.apache.http.client.utils.URIBuilder;

public class KilometricGoogleServiceImpl implements KilometricGoogleService {

  protected final KilometricResponseToolService kilometricResponseToolService;
  protected final MapGoogleService mapGoogleService;

  @Inject
  public KilometricGoogleServiceImpl(
      KilometricResponseToolService kilometricResponseToolService,
      MapGoogleService mapGoogleService) {
    this.kilometricResponseToolService = kilometricResponseToolService;
    this.mapGoogleService = mapGoogleService;
  }

  @Override
  public BigDecimal getDistanceUsingGoogle(String fromCity, String toCity)
      throws AxelorException, URISyntaxException, IOException {
    User user = AuthUtils.getUser();
    Map<String, Object> json =
        getGoogleMapsDistanceMatrixResponse(fromCity, toCity, user.getLanguage());
    String status = json.get("status").toString();

    if (status.equals("OK")) {
      Map<String, Object> row = ((List<Map<String, Object>>) json.get("rows")).getFirst();
      Map<String, Object> element = ((List<Map<String, Object>>) row.get("elements")).getFirst();
      status = element.get("status").toString();
      if (status.equals("OK")) {
        double distance = (double) element.get("distance");
        return BigDecimal.valueOf(distance / 1000);
      }
    }

    String msg =
        json.get("error_message") != null
            ? String.format("%s / %s", status, json.get("error_message"))
            : status;

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        HumanResourceExceptionMessage.KILOMETRIC_ALLOWANCE_GOOGLE_MAPS_ERROR,
        msg);
  }

  /**
   * Get JSON response from Google Maps Distance Matrix API.
   *
   * @param origins
   * @param destinations
   * @param language
   * @return
   * @throws URISyntaxException
   * @throws IOException
   * @throws AxelorException
   */
  protected Map<String, Object> getGoogleMapsDistanceMatrixResponse(
      String origins, String destinations, String language)
      throws URISyntaxException, IOException, AxelorException {

    URIBuilder ub = new URIBuilder("https://maps.googleapis.com/maps/api/distancematrix/json");
    ub.addParameter("origins", origins);
    ub.addParameter("destinations", destinations);
    ub.addParameter("language", language);
    ub.addParameter("key", mapGoogleService.getGoogleMapsApiKey());

    return kilometricResponseToolService.getApiResponse(
        ub.toString(), HumanResourceExceptionMessage.KILOMETRIC_ALLOWANCE_GOOGLE_MAPS_ERROR);
  }
}
