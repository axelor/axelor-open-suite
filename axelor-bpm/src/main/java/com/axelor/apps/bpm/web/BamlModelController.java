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
package com.axelor.apps.bpm.web;

import com.axelor.apps.baml.service.BamlService;
import com.axelor.apps.bpm.context.WkfContextHelper;
import com.axelor.apps.bpm.db.BamlModel;
import com.axelor.apps.bpm.db.repo.BamlModelRepository;
import com.axelor.common.Inflector;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.Map;

public class BamlModelController {

  @Inject protected BamlService bamlService;

  public void generateCode(ActionRequest request, ActionResponse response) throws AxelorException {

    BamlModel model = request.getContext().asType(BamlModel.class);

    String xml = bamlService.extractBamlXml(model.getBamlXml());
    if (xml != null) {
      String resultScript = Beans.get(BamlService.class).generateGroovyCode(xml);
      response.setValue("resultScript", resultScript);
    }
  }

  public void execute(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    String model = (String) context.get("modelName");
    Model entity = null;
    if (context.get("recordId") != null && model != null) {
      Long recordId = Long.parseLong(context.get("recordId").toString());
      entity = WkfContextHelper.getRepository(model).find(recordId);
    }

    Map<String, Object> bamlModelMap = (Map<String, Object>) context.get("bamlModel");
    BamlModel bamlModel =
        Beans.get(BamlModelRepository.class)
            .find(Long.parseLong(bamlModelMap.get("id").toString()));

    Model object = Beans.get(BamlService.class).execute(bamlModel, entity);

    String modelName = object.getClass().getSimpleName();
    String dasherizeModel = Inflector.getInstance().dasherize(modelName);

    String title = object.getClass().getSimpleName();
    String formView = dasherizeModel + "-form";
    String gridView = dasherizeModel + "-grid";

    String jsonModel = null;
    if (object instanceof MetaJsonRecord) {
      jsonModel = ((MetaJsonRecord) object).getJsonModel();
      title = Beans.get(MetaJsonModelRepository.class).findByName(jsonModel).getTitle();
      if (Strings.isNullOrEmpty(title)) {
        title = jsonModel;
      }
      formView = "custom-model-" + jsonModel + "-form";
      gridView = "custom-model-" + jsonModel + "-grid";
    }

    response.setCanClose(true);

    ActionViewBuilder builder =
        ActionView.define(I18n.get(title))
            .model(object.getClass().getName())
            .add("form", formView)
            .add("grid", gridView)
            .context("_showRecord", object.getId());

    if (jsonModel != null) {
      builder.context("jsonModel", jsonModel);
      builder.domain("self.jsonModel = :jsonModel");
    }

    response.setView(builder.map());
  }
}
