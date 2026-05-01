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
package com.axelor.apps.purchase.web;

import com.axelor.apps.purchase.service.CallTenderAttrConfigService;
import com.axelor.common.Inflector;
import com.axelor.common.StringUtils;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class MetaJsonFieldCallTenderController {

  public void onNew(ActionRequest request, ActionResponse response) {
    response.setValue("model", CallTenderAttrConfigService.MODEL_SOURCE);
    response.setValue("modelField", CallTenderAttrConfigService.MODEL_FIELD);
    response.setValue("widgetAttrs", "{\"colSpan\":\"6\"}");

    Context parent = request.getContext().getParent();
    if (parent == null) {
      return;
    }
    Object parentId = parent.get("id");
    if (parentId == null) {
      return;
    }
    Map<String, Object> configRef = new HashMap<>();
    configRef.put("id", Long.valueOf(parentId.toString()));
    response.setValue("callTenderAttrConfig", configRef);
  }

  public void computeFields(ActionRequest request, ActionResponse response) {
    MetaJsonField jsonField = request.getContext().asType(MetaJsonField.class);

    String title = jsonField.getTitle();
    if (StringUtils.isEmpty(title)) {
      return;
    }

    response.setValue("name", Inflector.getInstance().camelize(title, true));

    String typeSelect = (String) request.getContext().get("typeSelect");
    if (StringUtils.notEmpty(typeSelect)) {
      response.setValue("type", typeSelect);
    }
  }
}
