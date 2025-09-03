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
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.translation.ITranslation;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.studio.db.AppBase;
import com.axelor.studio.db.repo.AppBaseRepository;
import com.google.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class KilometricService {

  private AppBaseService appBaseService;
  private MapService mapService;

  @Inject
  public KilometricService(AppBaseService appBaseService, MapService mapService) {
    this.appBaseService = appBaseService;
    this.mapService = mapService;
  }

  public BigDecimal computeDistance(ExpenseLine expenseLine) throws AxelorException {
    if (expenseLine.getKilometricTypeSelect() == ExpenseLineRepository.KILOMETRIC_TYPE_ROUND_TRIP) {
      return computeDistance(expenseLine.getFromCity(), expenseLine.getToCity())
          .multiply(BigDecimal.valueOf(2));
    }
    return computeDistance(expenseLine.getFromCity(), expenseLine.getToCity());
  }

  /**
   * Compute the distance between two cities.
   *
   * @param fromCity
   * @param toCity
   * @return
   * @throws AxelorException
   */
  public BigDecimal computeDistance(String fromCity, String toCity) throws AxelorException {

    BigDecimal distance = BigDecimal.ZERO;
    if (StringUtils.isEmpty(fromCity)
        || StringUtils.isEmpty(toCity)
        || fromCity.equalsIgnoreCase(toCity)) return distance;

    AppBase appBase = appBaseService.getAppBase();
    try {
      switch (appBase.getMapApiSelect()) {
        case AppBaseRepository.MAP_API_GOOGLE:
          distance = this.getDistanceUsingGoogle(fromCity, toCity);
          break;

        case AppBaseRepository.MAP_API_OPEN_STREET_MAP:
          distance = this.getDistanceUsingOSRMApi(fromCity, toCity);
          break;
      }
      return distance;
    } catch (URISyntaxException | IOException | JSONException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  protected BigDecimal getDistanceUsingGoogle(String fromCity, String toCity)
      throws JSONException, AxelorException, URISyntaxException, IOException {
    User user = AuthUtils.getUser();
    JSONObject json = getGoogleMapsDistanceMatrixResponse(fromCity, toCity, user.getLanguage());
    String status = json.getString("status");

    if (status.equals("OK")) {
      JSONObject response =
          json.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0);
      status = response.getString("status");
      if (status.equals("OK")) {
        return BigDecimal.valueOf(response.getJSONObject("distance").getDouble("value") / 1000);
      }
    }

    String msg =
        json.has("error_message")
            ? String.format("%s / %s", status, json.getString("error_message"))
            : status;

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        HumanResourceExceptionMessage.KILOMETRIC_ALLOWANCE_GOOGLE_MAPS_ERROR,
        msg);
  }

  protected BigDecimal getDistanceUsingOSRMApi(String fromCity, String toCity)
      throws AxelorException, JSONException, URISyntaxException, IOException {
    JSONObject json = getOSRMApiResponse(fromCity, toCity);
    String status = json.getString("code");

    if (status.equals("Ok")) {
      return BigDecimal.valueOf(
          json.getJSONArray("routes").getJSONObject(0).getDouble("distance") / 1000);
    }

    String msg = json.has("message") ? String.format("%s", json.getString("message")) : status;

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        HumanResourceExceptionMessage.KILOMETRIC_ALLOWANCE_OSM_ERROR,
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
   * @throws JSONException
   * @throws AxelorException
   */
  protected JSONObject getGoogleMapsDistanceMatrixResponse(
      String origins, String destinations, String language)
      throws URISyntaxException, IOException, JSONException, AxelorException {

    URIBuilder ub = new URIBuilder("https://maps.googleapis.com/maps/api/distancematrix/json");
    ub.addParameter("origins", origins);
    ub.addParameter("destinations", destinations);
    ub.addParameter("language", language);
    ub.addParameter("key", mapService.getGoogleMapsApiKey());

    return this.getApiResponse(
        ub.toString(), HumanResourceExceptionMessage.KILOMETRIC_ALLOWANCE_GOOGLE_MAPS_ERROR);
  }

  /**
   * Get JSON response from Open Street Route Machine API.
   *
   * @param origins
   * @param destinations
   * @return
   * @throws AxelorException
   * @throws JSONException
   * @throws URISyntaxException
   * @throws IOException
   */
  protected JSONObject getOSRMApiResponse(String origins, String destinations)
      throws AxelorException, JSONException, URISyntaxException, IOException {

    Map<String, Object> originMap = this.getLocationMap(origins);
    Map<String, Object> destinationMap = this.getLocationMap(destinations);

    String originCoordinates = originMap.get("longitude") + "," + originMap.get("latitude");
    String destinationCoordinates =
        destinationMap.get("longitude") + "," + destinationMap.get("latitude");
    String uri =
        String.format(
            "https://router.project-osrm.org/route/v1/driving/%s;%s",
            originCoordinates, destinationCoordinates);

    return this.getApiResponse(uri, HumanResourceExceptionMessage.KILOMETRIC_ALLOWANCE_OSM_ERROR);
  }

  protected Map<String, Object> getLocationMap(String location) throws AxelorException {
    Map<String, Object> locationMap;
    try {
      locationMap = mapService.getMap(location);
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          HumanResourceExceptionMessage.KILOMETRIC_ALLOWANCE_OSM_ERROR,
          e.getMessage());
    }

    if (locationMap == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          HumanResourceExceptionMessage.KILOMETRIC_ALLOWANCE_OSM_ERROR,
          ITranslation.NO_SUCH_PLACE);
    }
    return locationMap;
  }

  protected JSONObject getApiResponse(String urlString, String exceptionMessage)
      throws IOException, JSONException, AxelorException {

    URL url = new URL(urlString);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    int responseCode = connection.getResponseCode();

    this.checkResponseStatus(responseCode, exceptionMessage);

    StringBuilder sb = new StringBuilder();
    try (BufferedReader in =
        new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        sb.append(inputLine + "\n");
      }
    }

    String response = sb.toString();
    JSONObject json;
    // throw exception if response is not json
    try {
      json = new JSONObject(response);
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, exceptionMessage, response);
    }

    return json;
  }

  protected void checkResponseStatus(int responseCode, String exceptionMessage)
      throws AxelorException {
    if (responseCode == 200) {
      return;
    } else if (responseCode == 429) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          exceptionMessage,
          ITranslation.REQUEST_OVERFLOW);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          exceptionMessage,
          "Server returned status code " + responseCode);
    }
  }
}
