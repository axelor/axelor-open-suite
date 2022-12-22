/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.AdvancedImportFileTab;
import com.axelor.apps.base.db.repo.AdvancedImportFileTabRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.advanced.imports.ActionService;
import com.axelor.apps.base.service.advanced.imports.AdvancedImportFileTabService;
import com.axelor.apps.base.service.advanced.imports.SearchCallService;
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

public class AdvancedImportFileTabController {

  public void updateFields(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Map<String, Object> map = context.getParent();
      if (map == null || (boolean) map.get("isConfigInFile") == true) {
        return;
      }

      AdvancedImportFileTab advancedImportFileTab = context.asType(AdvancedImportFileTab.class);
      Beans.get(AdvancedImportFileTabService.class).updateFields(advancedImportFileTab);
      response.setValue(
          "advancedImportFileFieldList", advancedImportFileTab.getAdvancedImportFileFieldList());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void compute(ActionRequest request, ActionResponse response) {
    try {
      AdvancedImportFileTab advancedImportFileTab =
          request.getContext().asType(AdvancedImportFileTab.class);
      advancedImportFileTab =
          Beans.get(AdvancedImportFileTabService.class).compute(advancedImportFileTab);
      response.setValues(advancedImportFileTab);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showRecord(ActionRequest request, ActionResponse response) {
    try {
      AdvancedImportFileTab advancedImportFileTab =
          request.getContext().asType(AdvancedImportFileTab.class);
      advancedImportFileTab =
          Beans.get(AdvancedImportFileTabRepository.class).find(advancedImportFileTab.getId());

      String btnName = request.getContext().get("_signal").toString();
      String fieldName = StringUtils.substringBetween(btnName, "show", "Btn");

      MetaJsonField jsonField =
          Beans.get(MetaJsonFieldRepository.class)
              .all()
              .filter(
                  "self.name = ?1 AND self.type = 'many-to-many' AND self.model = ?2 AND self.modelField = 'attrs'",
                  fieldName,
                  advancedImportFileTab.getClass().getName())
              .fetchOne();

      if (jsonField == null) {
        return;
      }

      String ids =
          Beans.get(AdvancedImportFileTabService.class)
              .getShowRecordIds(advancedImportFileTab, jsonField.getName());

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
    AdvancedImportFileTab advancedImportFileTab =
        request.getContext().asType(AdvancedImportFileTab.class);

    if (StringUtils.isEmpty(advancedImportFileTab.getActions())) {
      return;
    }

    ActionService actionService = Beans.get(ActionService.class);
    if (!actionService.validate(advancedImportFileTab.getActions())) {
      response.setError(
          String.format(
              BaseExceptionMessage.ADVANCED_IMPORT_LOG_10,
              advancedImportFileTab.getMetaModel().getName()));
    }
  }

  public void validateSearchCall(ActionRequest request, ActionResponse response) {
    AdvancedImportFileTab advancedImportFileTab =
        request.getContext().asType(AdvancedImportFileTab.class);

    SearchCallService searchCallService = Beans.get(SearchCallService.class);

    if (!searchCallService.validate(advancedImportFileTab.getSearchCall())) {
      response.setError(
          String.format(
              BaseExceptionMessage.ADVANCED_IMPORT_LOG_11,
              advancedImportFileTab.getMetaModel().getName()));
    }
  }
}
