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
package com.axelor.apps.base.utils;

import java.util.HashMap;
import java.util.Map;

public class MapTools {

  private MapTools() {}

  public static void addMap(
      Map<String, Map<String, Object>> map, Map<String, Map<String, Object>> toAddMap) {
    for (Map.Entry<String, Map<String, Object>> entry : toAddMap.entrySet()) {
      if (map.containsKey(entry.getKey())) {
        Map<String, Object> newMap = new HashMap<>();
        newMap.putAll(toAddMap.get(entry.getKey()));
        newMap.putAll(map.get(entry.getKey()));
        map.put(entry.getKey(), newMap);
      } else {
        map.put(entry.getKey(), entry.getValue());
      }
    }
  }
}
