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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.FileTab;
import com.axelor.apps.base.db.repo.FileTabRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.advanced.imports.ActionService;
import com.axelor.apps.base.service.advanced.imports.FileTabService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class FileTabController {

  public void updateFields(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Map<String, Object> map = context.getParent();
      if (map == null || (boolean) map.get("isConfigInFile") == true) {
        return;
      }

      FileTab fileTab = context.asType(FileTab.class);
      Beans.get(FileTabService.class).updateFields(fileTab);
      response.setValue("fileFieldList", fileTab.getFileFieldList());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void compute(ActionRequest request, ActionResponse response) {
    try {
      FileTab fileTab = request.getContext().asType(FileTab.class);
      fileTab = Beans.get(FileTabService.class).compute(fileTab);
      response.setValues(fileTab);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showRecord(ActionRequest request, ActionResponse response) {
    try {
      FileTab fileTab = request.getContext().asType(FileTab.class);
      fileTab = Beans.get(FileTabRepository.class).find(fileTab.getId());

      String btnName = request.getContext().get("_signal").toString();
      String fieldName = StringUtils.substringBetween(btnName, "show", "Btn");

      MetaJsonField jsonField =
          Beans.get(MetaJsonFieldRepository.class)
              .all()
              .filter(
                  "self.name = ?1 AND self.type = 'many-to-many' AND self.model = ?2 AND self.modelField = 'attrs'",
                  fieldName,
                  fileTab.getClass().getName())
              .fetchOne();

      if (jsonField == null) {
        return;
      }

      String ids = Beans.get(FileTabService.class).getShowRecordIds(fileTab, jsonField.getName());

      response.setView(
          ActionView.define(I18n.get(jsonField.getTitle()))
              .model(jsonField.getTargetModel())
              .add("grid", jsonField.getGridView())
              .add("form", jsonField.getFormView())
              .domain("self.id IN (" + ids + ")")
              .map());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateActions(ActionRequest request, ActionResponse response) {
    FileTab fileTab = request.getContext().asType(FileTab.class);

    if (StringUtils.isEmpty(fileTab.getActions())) {
      return;
    }

    ActionService actionService = Beans.get(ActionService.class);
    if (!actionService.validate(fileTab.getActions())) {
      response.setError(
          String.format(
              IExceptionMessage.ADVANCED_IMPORT_LOG_10, fileTab.getMetaModel().getName()));
    }
  }
}
