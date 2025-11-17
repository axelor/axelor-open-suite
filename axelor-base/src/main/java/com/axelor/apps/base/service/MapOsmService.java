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
