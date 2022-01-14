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
package com.axelor.apps.bpm.listener;

import com.axelor.apps.baml.tools.BpmTools;
import com.axelor.apps.bpm.context.WkfCache;
import com.axelor.apps.bpm.db.WkfProcessConfig;
import java.util.HashMap;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

public class WkfProcessConfigListener {

  @PostPersist
  @PostUpdate
  public void onSave(WkfProcessConfig config) {

    String tenantId = BpmTools.getCurentTenant();

    if (!WkfCache.WKF_MODEL_CACHE.containsKey(tenantId)) {
      WkfCache.WKF_MODEL_CACHE.put(tenantId, new HashMap<Long, String>());
    }

    WkfCache.WKF_MODEL_CACHE.get(tenantId).put(config.getId(), config.getModel());
  }

  @PostRemove
  public void onRemove(WkfProcessConfig config) {

    String tenantId = BpmTools.getCurentTenant();

    if (WkfCache.WKF_MODEL_CACHE.containsKey(tenantId)) {
      WkfCache.WKF_MODEL_CACHE.get(tenantId).remove(config.getId());
    }
  }
}
