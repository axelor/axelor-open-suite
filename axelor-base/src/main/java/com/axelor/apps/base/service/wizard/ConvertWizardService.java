/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.wizard;

import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.google.common.collect.Lists;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvertWizardService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @SuppressWarnings({"rawtypes", "unchecked"})
  public Object createObject(Map<String, Object> context, Object obj, Mapper mapper)
      throws AxelorException {

    if (context != null) {

      final int random = new Random().nextInt();
      for (final Property p : mapper.getProperties()) {

        if (p.isVirtual() || p.isPrimary() || p.isVersion()) {
          continue;
        }

        LOG.debug("Property name / Context value  : {} / {}", p.getName());

        Object value = context.get(p.getName());

        LOG.debug("Context value : {}", value);

        if (value != null) {

          if (value instanceof String && p.isUnique()) {
            value = ((String) value) + " (" + random + ")";
          }

          if (value instanceof Map) {
            LOG.debug("Map");
            Map map = (Map) value;
            Object id = map.get("id");
            value = JPA.find((Class<Model>) p.getTarget(), Long.parseLong(id.toString()));
          }
          if (value instanceof List) {
            LOG.debug("List");

            List<Object> valueList = (List<Object>) value;
            List<Object> resultList = Lists.newArrayList();

            if (valueList != null) {
              for (Object object : valueList) {
                Map map = (Map) object;
                Object id = map.get("id");
                resultList.add(
                    JPA.find((Class<Model>) p.getTarget(), Long.parseLong(id.toString())));
              }
            }
            value = resultList;
          }

          p.set(obj, value);
        }
      }

      return obj;
    }

    return null;
  }
}
