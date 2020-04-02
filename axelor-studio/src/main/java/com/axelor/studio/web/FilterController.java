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
package com.axelor.studio.web;

import com.axelor.common.Inflector;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.Filter;
import com.axelor.studio.service.filter.FilterSqlService;
import com.google.inject.Inject;

public class FilterController {

  @Inject private FilterSqlService filterSqlService;

  public void updateTargetField(ActionRequest request, ActionResponse response) {

    Filter filter = request.getContext().asType(Filter.class);

    MetaField metaField = filter.getMetaField();
    MetaJsonField metaJson = filter.getMetaJsonField();

    Boolean isJson = filter.getIsJson() != null ? filter.getIsJson() : false;

    if (!isJson && metaField != null) {
      String type =
          metaField.getRelationship() != null
              ? metaField.getRelationship()
              : metaField.getTypeName();
      response.setValue("targetType", type);
      response.setValue("targetField", metaField.getName());
    } else if (isJson && metaJson != null) {
      response.setValue("targetType", Inflector.getInstance().camelize(metaJson.getType()));
      response.setValue("targetField", metaJson.getName());
    } else {
      response.setValue("targetField", null);
      response.setValue("targetType", null);
    }

    response.setValue("filterOperator", null);
  }

  public void updateTargetType(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Filter filter = request.getContext().asType(Filter.class);

    if (filter.getTargetField() == null) return;

    StringBuilder parent = new StringBuilder("self");
    String targetType =
        filterSqlService.getTargetType(
            filterSqlService.getTargetField(parent, filter, null, false));

    response.setValue("targetType", targetType);
    response.setValue("filterOperator", null);
  }
}
