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
package com.axelor.apps.project.service;

import com.axelor.common.Inflector;
import com.axelor.common.StringUtils;
import com.axelor.meta.db.MetaJsonField;

public class MetaJsonFieldProjectServiceImpl implements MetaJsonFieldProjectService {

  @Override
  public String computeSelectName(MetaJsonField jsonField, String typeSelect) {
    String selection = jsonField.getSelection();
    if (StringUtils.isEmpty(jsonField.getName())
        || !("select".equals(typeSelect) || "multiselect".equals(typeSelect))
        || StringUtils.notEmpty(selection)) {
      return selection;
    }

    Inflector inflector = Inflector.getInstance();
    String model =
        inflector
            .dasherize(jsonField.getModel().substring(jsonField.getModel().lastIndexOf('.') + 1))
            .replace("-", ".");
    String fieldName = inflector.dasherize(jsonField.getName()).replace("-", ".");

    return String.format("project.%s.json.field.%s.type.select", model, fieldName);
  }
}
