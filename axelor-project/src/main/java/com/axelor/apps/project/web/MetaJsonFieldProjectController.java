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
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.ModelTool;
import com.google.inject.Singleton;

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

    response.setValue("modelField", "attrs");
    response.setValue("model", modelName);
    response.setValue("widgetAttrs", "{\"colSpan\":\"6\"}");

    final Context context = request.getContext();
    final Context parentContext = context.getParent();
    if (parentContext == null || !Project.class.getName().equals(parentContext.get("_model"))) {
      return;
    }

    // per project custom field
    final String contextField = "project";
    final Mapper mapper = Mapper.of(ProjectTask.class);
    final Property property = mapper.getProperty(contextField);
    final String target = property == null ? null : property.getTarget().getName();
    final String targetName = property == null ? null : property.getTargetName();

    response.setValue("contextField", contextField);
    response.setValue("contextFieldTarget", target);
    response.setValue("contextFieldTargetName", targetName);
    response.setValue("contextFieldValue", parentContext.get("id").toString());
    response.setValue("contextFieldTitle", parentContext.get(targetName).toString());
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
    String name = ModelTool.normalizeKeyword(title, true);

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
