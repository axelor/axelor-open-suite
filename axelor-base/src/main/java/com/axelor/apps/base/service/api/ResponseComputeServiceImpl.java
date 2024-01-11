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
package com.axelor.apps.base.service.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.i18n.I18n;

public class ResponseComputeServiceImpl implements ResponseComputeService {
  public String compute(Model model) throws AxelorException {
    Mapper mapper = Mapper.of(model.getClass());
    StringBuilder result =
        new StringBuilder(I18n.get("The object") + " " + model.getClass().getSimpleName());
    try {
      for (Property property : mapper.getProperties()) {
        if (property.isNameColumn()) {
          String namecolumn = (String) mapper.get(model, property.getName());
          if (!namecolumn.isEmpty()) {
            computeWithNameColumn(result, namecolumn);
            return result.toString();
          }
        }
      }
      computeWithId(model, result, mapper);
      return result.toString();
    } catch (Exception e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, e.getMessage());
    }
  }

  protected void computeWithNameColumn(StringBuilder result, String namecolumn) {
    result.append(" ");
    result.append(namecolumn);
    result.append(" ");
    result.append(I18n.get("has been created"));
  }

  protected void computeWithId(Model model, StringBuilder result, Mapper mapper) {
    result.append(" ");
    result.append(I18n.get("has been correctly created with the id :"));
    result.append(" ");
    result.append(mapper.get(model, "id"));
  }
}
