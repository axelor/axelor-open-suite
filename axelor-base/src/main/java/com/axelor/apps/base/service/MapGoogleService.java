package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.util.Map;

public interface MapGoogleService {
  Map<String, Object> getMapGoogle(String qString) throws AxelorException;

  void testGMapService() throws AxelorException;

  Map<String, Object> getDirectionMapGoogle(
      String dString,
      BigDecimal dLat,
      BigDecimal dLon,
      String aString,
      BigDecimal aLat,
      BigDecimal aLon);

  String getMapURI(String name, Long id);

  String getGoogleMapsApiKey();
}
