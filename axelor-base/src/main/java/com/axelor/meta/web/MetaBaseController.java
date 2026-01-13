/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.meta.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.service.MetaBaseService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.web.ITranslation;
import com.google.common.base.Strings;

public class MetaBaseController {

  public void checkMetaFields(ActionRequest request, ActionResponse response) {
    try {
      String fieldNotExists = Beans.get(MetaBaseService.class).checkMetaFields();
      if (!Strings.isNullOrEmpty(fieldNotExists)) {
        response.setView(
            ActionView.define(I18n.get("Fields"))
                .model(MetaField.class.getName())
                .add("grid", "meta-field-grid")
                .add("form", "meta-field-form")
                .domain("self.id in (" + fieldNotExists + ")")
                .map());
      } else {
        response.setInfo(I18n.get(ITranslation.ALL_META_FIELD_EXIST));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkMetaModels(ActionRequest request, ActionResponse response) {
    try {
      String modelNotExists = Beans.get(MetaBaseService.class).checkMetaModels();

      if (!Strings.isNullOrEmpty(modelNotExists)) {
        response.setView(
            ActionView.define(I18n.get("Models"))
                .model(MetaModel.class.getName())
                .add("grid", "meta-model-grid")
                .add("form", "meta-model-form")
                .domain("self.id in (" + modelNotExists + ")")
                .map());
      } else {
        response.setInfo(I18n.get(ITranslation.ALL_META_MODEL_EXIST));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
