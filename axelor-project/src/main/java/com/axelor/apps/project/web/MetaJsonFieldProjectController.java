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
package com.axelor.apps.project.web;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.MetaJsonFieldProjectService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.ModelHelper;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class MetaJsonFieldProjectController {

  public void onNew(ActionRequest request, ActionResponse response) {

    String action = request.getAction();
    String modelName = action.split(",")[0].substring(action.lastIndexOf('-') + 1);

    switch (modelName) {
      case "task":
        modelName = ProjectTask.class.getName();
        break;
      case "project":
        modelName = Project.class.getName();
        break;
      default:
        modelName = null;
    }

    if (modelName == null) {
      response.setError(I18n.get(ProjectExceptionMessage.JSON_FIELD_MODEL_INVALID));
      return;
    }

    Map<String, Object> contextValues = new HashMap<>();

    contextValues.put("modelField", "attrs");
    contextValues.put("model", modelName);
    contextValues.put("widgetAttrs", "{\"colSpan\":\"6\"}");

    final Context context = request.getContext();
    final Context parentContext = context.getParent();
    response.setValues(
        Beans.get(MetaJsonFieldProjectService.class)
            .computeContextValues(contextValues, parentContext));
  }

  public void setSelection(ActionRequest request, ActionResponse response) {
    MetaJsonField jsonField = request.getContext().getParent().asType(MetaJsonField.class);

    String typeSelect = (String) request.getContext().get("typeSelect");
    if (!("select".equals(typeSelect) || "multiselect".equals(typeSelect))) {
      return;
    }

    response.setValue("select", jsonField.getSelectionRef());
  }

  public void computeFields(ActionRequest request, ActionResponse response) {

    MetaJsonField jsonField = request.getContext().asType(MetaJsonField.class);

    String title = jsonField.getTitle();
    if (StringUtils.isEmpty(title)) {
      return;
    }

    String typeSelect = (String) request.getContext().get("typeSelect");
    String name = ModelHelper.normalizeKeyword(title, true);

    if (Project.class.equals(request.getContext().getParent().getContextClass())) {
      Long projectId = request.getContext().getParent().asType(Project.class).getId();
      name += projectId;
    }

    String widget = null;
    if ("multiselect".equals(typeSelect)) {
      widget = "MultiSelect";
    }

    String selection =
        Beans.get(MetaJsonFieldProjectService.class).computeSelectName(jsonField, typeSelect);

    if (selection != null && jsonField.getSelectionRef() == null) {
      MetaSelectRepository selectRepo = Beans.get(MetaSelectRepository.class);
      MetaSelect select = selectRepo.findByName(selection);

      if (select == null) {
        select = new MetaSelect(selection);
        select.setModule("axelor-project");
      }
      jsonField.setSelectionRef(select);
    }

    response.setValue("widget", widget);
    response.setValue("name", name);
    response.setValue("selection", selection);
    response.setValue("selectionRef", jsonField.getSelectionRef());
  }
}
