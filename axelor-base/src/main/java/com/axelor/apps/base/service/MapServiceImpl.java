/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.StringUtils;
import com.axelor.studio.db.repo.AppBaseRepository;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapServiceImpl implements MapService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final Cache<String, Optional<Map<String, Object>>> GEOCODE_CACHE =
      CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(1000).build();

  protected final AppBaseService appBaseService;
  protected final MapOsmService mapOsmService;
  protected final MapGoogleService mapGoogleService;
  protected final MapToolService mapToolService;

  @Inject
  public MapServiceImpl(
      AppBaseService appBaseService,
      MapOsmService mapOsmService,
      MapGoogleService mapGoogleService,
      MapToolService mapToolService) {
    this.appBaseService = appBaseService;
    this.mapOsmService = mapOsmService;
    this.mapGoogleService = mapGoogleService;
    this.mapToolService = mapToolService;
  }

  @Override
  public Map<String, Object> getMap(String qString) throws AxelorException {
    LOG.debug("qString = {}", qString);

    int mapApiSelect = appBaseService.getAppBase().getMapApiSelect();
    String cacheKey = mapApiSelect + "|" + qString.trim().toLowerCase(Locale.ROOT);

    Optional<Map<String, Object>> cached = GEOCODE_CACHE.getIfPresent(cacheKey);
    if (cached != null) {
      return cached.orElse(null);
    }

    Map<String, Object> result;
    switch (mapApiSelect) {
      case AppBaseRepository.MAP_API_GOOGLE:
        result = mapGoogleService.getMapGoogle(qString);
        break;

      case AppBaseRepository.MAP_API_OPEN_STREET_MAP:
        result = mapOsmService.getMapOsm(qString);
        break;

      default:
        result = null;
    }

    GEOCODE_CACHE.put(cacheKey, Optional.ofNullable(result));
    return result;
  }

  @Override
  public String getMapUrl(Pair<BigDecimal, BigDecimal> latLong, String title) {
    return getMapUrl(latLong.getLeft(), latLong.getRight(), title);
  }

  protected String getMapUrl(BigDecimal latitude, BigDecimal longitude, String title) {
    try {
      switch (appBaseService.getAppBase().getMapApiSelect()) {
        case AppBaseRepository.MAP_API_GOOGLE:
          final String uri = "map/gmaps.html";
          UriBuilder ub = UriBuilder.fromUri(uri);
          ub.queryParam("key", mapGoogleService.getGoogleMapsApiKey());
          ub.queryParam("x", String.valueOf(latitude));
          ub.queryParam("y", String.valueOf(longitude));
          ub.queryParam("z", String.valueOf(18));
          ub.queryParam("title", title);
          return ub.build().toString();

        case AppBaseRepository.MAP_API_OPEN_STREET_MAP:
          return "map/oneMarker.html?x=" + latitude + "&y=" + longitude + "&z=18";

        default:
          return null;
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      return mapToolService.getErrorURI(e.getMessage());
    }
  }

  @Override
  public String getMapURI(String name) {
    return appBaseService.getAppBase().getMapApiSelect() == AppBaseRepository.MAP_API_GOOGLE
        ? mapGoogleService.getMapURI(name, null)
        : mapOsmService.getOsmMapURI(name, null);
  }

  @Override
  public boolean isConfigured() {
    switch (appBaseService.getAppBase().getMapApiSelect()) {
      case AppBaseRepository.MAP_API_GOOGLE:
        return StringUtils.notBlank(appBaseService.getAppBase().getGoogleMapsApiKey());

      case AppBaseRepository.MAP_API_OPEN_STREET_MAP:
        return true;

      default:
        return false;
    }
  }

  @Override
  public String getAddressString(Address address) {
    if (address == null) {
      return "";
    }
    if (appBaseService.getAppBase().getMapApiSelect()
        == AppBaseRepository.MAP_API_OPEN_STREET_MAP) {

      String streetName = address.getStreetName();
      String city = address.getCity() != null ? address.getCity().getName() : "";
      String zip = address.getZip();
      String countrySubDivision = address.getCountrySubDivision();
      String country = address.getCountry() != null ? address.getCountry().getName() : "";

      return (!Strings.isNullOrEmpty(streetName) ? streetName : "")
          + (!Strings.isNullOrEmpty(city) ? " " + city : "")
          + (!Strings.isNullOrEmpty(countrySubDivision) ? " " + countrySubDivision : "")
          + (!Strings.isNullOrEmpty(zip) ? " " + zip : "")
          + (!Strings.isNullOrEmpty(country) ? " " + country : "");
    }
    return address.getFullName();
  }
}
