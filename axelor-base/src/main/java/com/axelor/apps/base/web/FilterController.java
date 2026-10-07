/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Filter;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.filter.FilterTargetService;
import com.axelor.common.Inflector;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import jakarta.inject.Singleton;
import java.util.Map;

@Singleton
public class FilterController {

  public void updateTargetField(ActionRequest request, ActionResponse response) {
    try {
      Filter filter = request.getContext().asType(Filter.class);
      MetaField metaField = filter.getMetaField();
      MetaJsonField metaJsonField = filter.getMetaJsonField();

      if (!filter.getIsJson() && metaField != null) {
        response.setValue("targetType", getType(metaField));
        response.setValue("targetField", metaField.getName());
        response.setValue("targetTitle", getTitle(metaField));
      } else if (metaJsonField != null) {
        response.setValue("targetType", Inflector.getInstance().camelize(metaJsonField.getType()));
        response.setValue("targetField", metaJsonField.getName());
      } else {
        response.setValue("targetField", null);
        response.setValue("targetType", null);
      }
      response.setValue("operator", null);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateTargetType(ActionRequest request, ActionResponse response) {
    try {
      Filter filter = request.getContext().asType(Filter.class);
      if (filter.getTargetField() == null) {
        return;
      }
      response.setValue("targetType", Beans.get(FilterTargetService.class).getTargetType(filter));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void updateTargetMetaField(ActionRequest request, ActionResponse response) {
    try {
      Filter filter = request.getContext().asType(Filter.class);
      Map<String, Object> targetMetaFieldMap =
          (Map<String, Object>) request.getContext().get("targetMetaField");

      if (targetMetaFieldMap != null) {
        MetaField targetMetaField =
            Beans.get(MetaFieldRepository.class)
                .find(Long.valueOf(targetMetaFieldMap.get("id").toString()));

        response.setValue("targetField", filter.getTargetField() + "." + targetMetaField.getName());
        response.setValue("targetTitle", filter.getTargetTitle() + "." + getTitle(targetMetaField));
        response.setValue("targetType", getType(targetMetaField));

        if (targetMetaField.getRelationship() != null) {
          response.setValue("metaTargetFieldDomain", targetMetaField.getTypeName());
          response.setValue("targetMetaField", null);
        }
      }
      response.setValue("operator", null);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void clearSelection(ActionRequest request, ActionResponse response) {
    try {
      Filter filter = request.getContext().asType(Filter.class);
      MetaField metaField = filter.getMetaField();

      if (metaField != null) {
        response.setValue("targetField", metaField.getName());
        response.setValue("targetTitle", getTitle(metaField));
        response.setValue("targetType", metaField.getRelationship());
      }
      response.setValue("metaTargetFieldDomain", null);
      response.setValue("targetMetaField", null);
      response.setValue("operator", null);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected String getType(MetaField metaField) {
    return metaField.getRelationship() != null
        ? metaField.getRelationship()
        : metaField.getTypeName();
  }

  protected String getTitle(MetaField metaField) {
    return StringUtils.notEmpty(metaField.getLabel()) ? metaField.getLabel() : metaField.getName();
  }
}
