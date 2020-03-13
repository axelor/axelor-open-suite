/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.util.Map;

public class ImportTrackingNumberConfig {

  @Transactional
  public Object computeFullName(Object bean, Map<String, Object> values) throws AxelorException {

    assert bean instanceof TrackingNumberConfiguration;

    TrackingNumberConfiguration trackingNumberConfiguration = (TrackingNumberConfiguration) bean;
    Sequence sequence = trackingNumberConfiguration.getSequence();
    String name = trackingNumberConfiguration.getName();
    trackingNumberConfiguration.setFullName(name);
    if (sequence != null) {
      trackingNumberConfiguration.setFullName(name + " / " + sequence.getFullName());
    }

    return trackingNumberConfiguration;
  }
}
