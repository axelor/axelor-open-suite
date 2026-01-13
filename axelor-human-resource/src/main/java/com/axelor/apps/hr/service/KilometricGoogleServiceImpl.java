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
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.MapGoogleService;
import com.axelor.i18n.I18n;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
  public BigDecimal getDistanceUsingGoogle(String fromCity, String toCity) throws AxelorException {
    HttpResponse<String> response = getGoogleMapsDistanceMatrixResponse(fromCity, toCity);
    try {
      if (response.statusCode() == 200) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.body());
        double distance = json.get("routes").get(0).get("distanceMeters").doubleValue();
        return BigDecimal.valueOf(distance / 1000);
      }
    } catch (JsonProcessingException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.GOOGLE_MAP_API_ERROR_2));
    }
    return BigDecimal.ZERO;
  }

  /**
   * Get JSON response from Google Maps Distance Matrix API.
   *
   * @param origins
   * @param destinations
   * @return
   * @throws URISyntaxException
   * @throws IOException
   * @throws AxelorException
   */
  protected HttpResponse<String> getGoogleMapsDistanceMatrixResponse(
      String origins, String destinations) throws AxelorException {

    try {
      String apiKey = mapGoogleService.getGoogleMapsApiKey();
      ObjectMapper mapper = new ObjectMapper();

      ObjectNode root = mapper.createObjectNode();
      ObjectNode originNode = root.putObject("origin");
      originNode.put("address", origins);
      ObjectNode destinationNode = root.putObject("destination");
      destinationNode.put("address", destinations);

      root.put("travelMode", "DRIVE");
      root.put("computeAlternativeRoutes", false);
      root.put("routingPreference", "TRAFFIC_AWARE");

      String jsonBody = mapper.writeValueAsString(root);

      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create("https://routes.googleapis.com/directions/v2:computeRoutes"))
              .header("Content-Type", "application/json")
              .header("X-Goog-Api-Key", apiKey)
              .header(
                  "X-Goog-FieldMask",
                  "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline")
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .build();

      return client.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.GOOGLE_MAP_API_ERROR_2));
    }
  }
}
