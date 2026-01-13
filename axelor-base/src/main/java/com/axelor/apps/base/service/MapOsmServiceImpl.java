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
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.studio.db.repo.AppBaseRepository;
import groovy.xml.XmlSlurper;
import groovy.xml.slurpersupport.GPathResult;
import groovy.xml.slurpersupport.Node;
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
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapOsmServiceImpl implements MapOsmService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private BigDecimal lat;
  private BigDecimal lon;

  protected final AppBaseService appBaseService;
  protected final MapToolService mapToolService;

  @Inject
  public MapOsmServiceImpl(AppBaseService appBaseService, MapToolService mapToolService) {
    this.appBaseService = appBaseService;
    this.mapToolService = mapToolService;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public Map<String, Object> getMapOsm(String qString) {
    Map<String, Object> result = new HashMap<>();
    try {
      BigDecimal latitude = BigDecimal.ZERO;
      BigDecimal longitude = BigDecimal.ZERO;
      HttpResponse<String> response = getOsmApiResponse(qString);
      GPathResult searchresults = new XmlSlurper().parseText(response.body());
      Iterator<Node> iterator = searchresults.childNodes();
      if (iterator.hasNext()) {
        Node node = iterator.next();
        Map attributes = node.attributes();
        if (attributes.containsKey("lat") && attributes.containsKey("lon")) {
          if (BigDecimal.ZERO.compareTo(latitude) == 0)
            latitude = new BigDecimal(node.attributes().get("lat").toString());
          if (BigDecimal.ZERO.compareTo(longitude) == 0)
            longitude = new BigDecimal(node.attributes().get("lon").toString());
        }
      }

      LOG.debug("OSMap qString: {}, latitude: {}, longitude: {}", qString, latitude, longitude);

      if (BigDecimal.ZERO.compareTo(latitude) != 0 && BigDecimal.ZERO.compareTo(longitude) != 0) {
        result.put("url", "map/oneMarker.html?x=" + latitude + "&y=" + longitude + "&z=18");
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        return result;
      }

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return null;
  }

  protected HttpResponse<String> getOsmApiResponse(String qString)
      throws IOException, InterruptedException {
    Map<String, Object> mapQuery = new HashMap<>();
    mapQuery.put("q", qString);
    mapQuery.put("format", "xml");
    mapQuery.put("polygon", 1);
    mapQuery.put("addressdetails", 1);

    String query =
        mapQuery.entrySet().stream()
            .map(
                e ->
                    URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                        + "="
                        + URLEncoder.encode(e.getValue().toString(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));

    String url = "https://nominatim.openstreetmap.org/search?" + query;

    // Create HttpClient with custom options
    HttpClient client =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(10000))
            .followRedirects(HttpClient.Redirect.NEVER) // Do not follow redirects
            .build();

    // Build request
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json") // Accept JSON
            .timeout(Duration.ofMillis(10000))
            .GET()
            .build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  @Override
  public Map<String, Object> getDirectionMapOsm(
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
        getOsmResponse(dString);
        dLat = lat;
        dLon = lon;
      }
      LOG.debug("departureLat = {}, departureLng={}", dLat, dLon);
      if (!checkNotNullNotZero(aLat) || !checkNotNullNotZero(aLon)) {
        getOsmResponse(aString);
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
            "map/osm-directions.html?"
                + "dx="
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
  public String getOsmMapURI(String name, Long id) {
    final String uri = "osm-objs/index.html";

    try {
      UriBuilder ub = UriBuilder.fromUri(uri);
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
  public String getDirectionUrl(
      String key,
      Pair<BigDecimal, BigDecimal> departureLatLong,
      Pair<BigDecimal, BigDecimal> arrivalLatLong) {
    return getDirectionUrl(
        key,
        departureLatLong.getLeft(),
        departureLatLong.getRight(),
        arrivalLatLong.getLeft(),
        arrivalLatLong.getRight());
  }

  protected String getDirectionUrl(
      String key, BigDecimal dLat, BigDecimal dLon, BigDecimal aLat, BigDecimal aLon) {
    String queryParam = "dx=" + dLat + "&dy=" + dLon + "&ax=" + aLat + "&ay=" + aLon;
    if (appBaseService.getAppBase().getMapApiSelect()
        == AppBaseRepository.MAP_API_OPEN_STREET_MAP) {
      return "map/osm-directions.html?" + queryParam;
    }
    return "map/directions.html?" + queryParam + "&key=" + key;
  }

  protected void getOsmResponse(String qString) throws AxelorException {
    Map<String, Object> osmResponse = getMapOsm(qString);

    lat = lon = BigDecimal.ZERO;
    if (osmResponse != null) {
      lat = new BigDecimal(osmResponse.get("latitude").toString());
      lon = new BigDecimal(osmResponse.get("longitude").toString());
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
