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
