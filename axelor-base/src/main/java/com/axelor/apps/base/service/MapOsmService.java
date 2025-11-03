package com.axelor.apps.base.service;

import java.math.BigDecimal;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

public interface MapOsmService {
  Map<String, Object> getMapOsm(String qString);

  Map<String, Object> getDirectionMapOsm(
      String dString,
      BigDecimal dLat,
      BigDecimal dLon,
      String aString,
      BigDecimal aLat,
      BigDecimal aLon);

  String getOsmMapURI(String name, Long id);

  String getDirectionUrl(
      String key,
      Pair<BigDecimal, BigDecimal> departureLatLong,
      Pair<BigDecimal, BigDecimal> arrivalLatLong);
}
