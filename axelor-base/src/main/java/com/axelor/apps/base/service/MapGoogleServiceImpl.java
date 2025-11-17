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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapGoogleServiceImpl implements MapGoogleService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private BigDecimal lat;
  private BigDecimal lon;

  protected final AppBaseService appBaseService;
  protected final MapToolService mapToolService;

  @Inject
  public MapGoogleServiceImpl(AppBaseService appBaseService, MapToolService mapToolService) {
    this.appBaseService = appBaseService;
    this.mapToolService = mapToolService;
  }

  @Override
  public Map<String, Object> getMapGoogle(String qString) throws AxelorException {
    LOG.debug("Query string: {}", qString);
    /*    JSONObject googleResponse = geocodeGoogle(qString);
    LOG.debug("Google response: {}", googleResponse);
    if (googleResponse != null) {
      Map<String, Object> result = new HashMap<>();
      BigDecimal latitude = new BigDecimal(googleResponse.get("lat").toString());
      BigDecimal longitude = new BigDecimal(googleResponse.get("lng").toString());
      LOG.debug("URL:" + "map/gmaps.html?x=" + latitude + "&y=" + longitude + "&z=18");
      result.put(
          "url",
          "map/gmaps.html?key="
              + getGoogleMapsApiKey()
              + "&x="
              + latitude
              + "&y="
              + longitude
              + "&z=18");
      result.put("latitude", latitude);
      result.put("longitude", longitude);
      return result;
    }*/

    return null;
  }

  @Override
  public void testGMapService() throws AxelorException {
    /*    HttpResponse<String> response = getGoogleApiResponse("google");
    getJSON(response.body());*/
    return;
  }

  protected Map<String, Object> geocodeGoogle(String qString) throws AxelorException {
    if (StringUtils.isBlank(qString)) {
      return null;
    }
    // http://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&sensor=true_or_false

    // TODO inject the rest client, or better, run it in the browser
    /*    HttpResponse<String> response = getGoogleApiResponse(qString);

    System.out.println(response.body());

    LOG.debug("Gmap response: {}", restResponse);

    if (restResponse.containsKey("results")) {
      JSONArray results = (JSONArray) restResponse.get("results");

      if (CollectionUtils.isNotEmpty(results)) {
        JSONObject result = (JSONObject) results.iterator().next();

        if (result != null && result.containsKey("geometry")) {
          return (JSONObject) ((JSONObject) result.get("geometry")).get("location");
        }
      }
    }

    throw new AxelorException(
            appBaseService.getAppBase(),
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(BaseExceptionMessage.MAP_RESPONSE_ERROR),
            restResponse);*/
    return null;
  }

  protected HttpResponse<String> getGoogleApiResponse(String qString)
      throws IOException, InterruptedException {
    Map<String, Object> responseQuery = new HashMap<>();
    responseQuery.put("address", qString.trim());
    responseQuery.put("sensor", "false");
    responseQuery.put("key", getGoogleMapsApiKey());

    String queryString =
        responseQuery.entrySet().stream()
            .map(
                entry ->
                    URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                        + "="
                        + URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));

    String url = "https://maps.googleapis.com/maps/api/geocode/json?" + queryString;

    HttpClient client =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(5000))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .timeout(Duration.ofMillis(10000))
            .GET()
            .build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  protected Map<String, Object> getJSON(HttpResponse<String> response) throws AxelorException {
    /*    LOG.debug(
        "Gmap connection status code: {}, message: {}",
        response.statusCode(),
        response.getStatusMessage());

    AppBase appBase = appBaseService.getAppBase();

    if (response.statusCode() != HttpStatus.SC_OK) {
      String msg = String.format("%d: %s", response.getStatusCode(), response.getStatusMessage());
      throw new AxelorException(appBase, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, msg);
    }

    JSONObject json = new JSONObject(response.getContentAsString());
    String status = json.getString("status");

    if (!"OK".equalsIgnoreCase(status)) {
      String msg =
          json.has("error_message")
              ? String.format("%s: %s", status, json.getString("error_message"))
              : status;
      throw new AxelorException(appBase, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, msg);
    }*/

    return null;
  }

  @Override
  public Map<String, Object> getDirectionMapGoogle(
      String dString,
      BigDecimal dLat,
      BigDecimal dLon,
      String aString,
      BigDecimal aLat,
      BigDecimal aLon) {
    LOG.debug("departureString = {}", dString);
    LOG.debug("arrivalString = {}", aString);
    Map<String, Object> result = new HashMap<>();
    try {
      if (!checkNotNullNotZero(dLat) || !checkNotNullNotZero(dLon)) {
        getGoogleResponse(dString);
        dLat = lat;
        dLon = lon;
      }
      LOG.debug("departureLat = {}, departureLng={}", dLat, dLon);
      if (!checkNotNullNotZero(aLat) || !checkNotNullNotZero(aLon)) {
        getGoogleResponse(aString);
        aLat = lat;
        aLon = lon;
      }
      LOG.debug("arrivalLat = {}, arrivalLng={}", aLat, aLon);

      if (checkNotNullNotZero(dLat)
          && checkNotNullNotZero(dLon)
          && checkNotNullNotZero(aLon)
          && checkNotNullNotZero(aLat)) {
        result.put(
            "url",
            "map/directions.html?key="
                + getGoogleMapsApiKey()
                + "&dx="
                + dLat
                + "&dy="
                + dLon
                + "&ax="
                + aLat
                + "&ay="
                + aLon);
        result.put("aLat", aLat);
        result.put("dLat", dLat);
        return result;
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return null;
  }

  @Override
  public String getMapURI(String name, Long id) {
    final String uri = "map/gmap-objs.html";

    try {
      UriBuilder ub = UriBuilder.fromUri(uri);
      ub.queryParam("key", getGoogleMapsApiKey());
      ub.queryParam("object", name);

      if (id != null) {
        ub.queryParam("id", id);
      }
      return ub.build().toString();
    } catch (Exception e) {
      TraceBackService.trace(e);
      return mapToolService.getErrorURI(e.getMessage());
    }
  }

  @Override
  public String getGoogleMapsApiKey() {
    Preconditions.checkArgument(
        StringUtils.notBlank(appBaseService.getAppBase().getGoogleMapsApiKey()),
        I18n.get(BaseExceptionMessage.MAP_GOOGLE_MAPS_API_KEY_MISSING));
    return appBaseService.getAppBase().getGoogleMapsApiKey();
  }

  protected void getGoogleResponse(String key) throws AxelorException {
    @SuppressWarnings("unchecked")
    Map<String, Object> googleResponse = geocodeGoogle(key);

    lat = lon = BigDecimal.ZERO;
    if (googleResponse != null) {
      lat = new BigDecimal(googleResponse.get("lat").toString());
      lon = new BigDecimal(googleResponse.get("lng").toString());
    }
  }

  protected Boolean checkNotNullNotZero(BigDecimal value) {
    Boolean flag = false;
    if (value != null && BigDecimal.ZERO.compareTo(value) != 0) {
      flag = true;
    }
    return flag;
  }
}
