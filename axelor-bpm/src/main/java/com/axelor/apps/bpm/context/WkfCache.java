/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bpm.context;

import com.axelor.apps.baml.tools.BpmTools;
import com.axelor.apps.bpm.db.WkfProcessConfig;
import com.axelor.apps.bpm.db.WkfTaskConfig;
import com.axelor.db.JPA;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;

public class WkfCache {

  public static Map<String, Map<Long, String>> WKF_MODEL_CACHE =
      new ConcurrentHashMap<String, Map<Long, String>>();
  public static Map<String, MultiMap> WKF_BUTTON_CACHE = new ConcurrentHashMap<String, MultiMap>();

  public static void initWkfModelCache() {

    List<WkfProcessConfig> wkfProcessConfigs = JPA.all(WkfProcessConfig.class).fetch();

    Map<Long, String> modelMap = new HashMap<Long, String>();
    modelMap.put(0L, "");
    for (WkfProcessConfig config : wkfProcessConfigs) {
      modelMap.put(config.getId(), config.getModel());
    }
    WKF_MODEL_CACHE.put(BpmTools.getCurentTenant(), modelMap);
  }

  public static void initWkfButttonCache() {

    List<WkfTaskConfig> wkfTaskConfigs = JPA.all(WkfTaskConfig.class).fetch();

    MultiMap multiMap = new MultiValueMap();
    multiMap.put(0L, null);
    for (WkfTaskConfig config : wkfTaskConfigs) {
      if (config.getButton() != null) {
        for (String btnName : config.getButton().split(",")) {
          multiMap.put(config.getId(), btnName);
        }
      }
    }

    WKF_BUTTON_CACHE.put(BpmTools.getCurentTenant(), multiMap);
  }
}
