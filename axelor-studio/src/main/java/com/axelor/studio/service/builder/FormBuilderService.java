/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.builder;

import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Button;
import com.axelor.meta.schema.views.FormView;
import com.axelor.meta.schema.views.SimpleWidget;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;

import org.hsqldb.lib.StringInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormBuilderService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private ViewBuilderService viewBuilderService;

  public String build(MetaJsonModel jsonModel, String moduleName) throws AxelorException {

    String model = viewBuilderService.getJsonModelName(moduleName, jsonModel.getName());

    FormView view = new FormView();
    view.setModel(model);
    view.setTitle(jsonModel.getTitle());
    view.setOnNew(jsonModel.getOnNew());
    view.setOnSave(jsonModel.getOnSave());
    List<AbstractWidget> items = getItems(jsonModel);
    view.setItems(items);

    return viewBuilderService.createXml(view);
  }

  private List<AbstractWidget> getItems(MetaJsonModel jsonModel) {

    List<AbstractWidget> items = new ArrayList<>();

    for (MetaJsonField field : jsonModel.getFields()) {
      SimpleWidget item = null;
      switch (field.getType()) {
        case "button":
          item = createButton(field);
          break;
      }

      if (item != null) {
        processCommon(item, field);
        items.add(item);
      }
    }

    return items;
  }

  private void processCommon(SimpleWidget item, MetaJsonField field) {

    item.setName(field.getName());
    item.setTitle(field.getTitle());
    item.setReadonly(field.getReadonly());
    item.setReadonlyIf(field.getReadonlyIf());
    item.setHidden(field.getHidden());
    item.setHideIf(field.getHideIf());
    
//    if (field.getWidgetAttrs() != null) {
//    	Json.createParser(new StringInputStream(field.getWidgetAttrs()));
//    }
  }

  private SimpleWidget createButton(MetaJsonField field) {

    Button button = new Button();
    button.setOnClick(field.getOnClick());

    return button;
  }
}
