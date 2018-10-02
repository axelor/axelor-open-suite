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

import com.axelor.common.Inflector;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Button;
import com.axelor.meta.schema.views.Field;
import com.axelor.meta.schema.views.GridView;
import com.axelor.meta.schema.views.SimpleWidget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class GridBuilderService {

  @Inject private ViewBuilderService viewBuilderService;

  @Inject private MetaJsonFieldRepository metaJsonFieldRepo;
  
  @Inject private ModelBuilderService modelBuilderService;

  public GridView build(MetaJsonModel jsonModel, String module) throws AxelorException {

    String model = modelBuilderService.getModelFullName(module, jsonModel.getName());
    String name = Inflector.getInstance().dasherize(jsonModel.getName()) + "-grid";

    GridView view = new GridView();
    view.setModel(model);
    view.setName(name);
    view.setXmlId(module + "-" + name);
    view.setTitle(jsonModel.getTitle());
    List<AbstractWidget> items = createItems(jsonModel.getFields());
    view.setItems(items);

    return view;
  }

  public GridView build(MetaModel metaModel, String module) throws AxelorException {

    if (metaModel == null) {
      return null;
    }

    List<MetaJsonField> fields =
        metaJsonFieldRepo.all().filter("self.model = ?1", metaModel.getFullName()).fetch();

    if (fields.isEmpty()) {
      return null;
    }

    String name = Inflector.getInstance().dasherize(metaModel.getName()) + "-grid";
    GridView view = new GridView();
    view.setModel(metaModel.getFullName());
    view.setName(name);
    view.setTitle(viewBuilderService.getViewTitle(name));
    view.setExtension(true);
    view.setXmlId(module + "-" + name);
    List<AbstractWidget> items = createItems(fields);
    view.setItems(items);

    return view;
  }

  private List<AbstractWidget> createItems(List<MetaJsonField> fields) {

    List<AbstractWidget> items = new ArrayList<>();

    modelBuilderService.sortJsonFields(fields);

    for (MetaJsonField field : fields) {

      if (!field.getVisibleInGrid()) {
        continue;
      }

      SimpleWidget item = createSimpleItem(field);
      if (item != null) {
        processCommon(item, field);
        items.add(item);
      }
    }

    return items;
  }

  private SimpleWidget createSimpleItem(MetaJsonField field) {

    SimpleWidget item = null;
    switch (field.getType()) {
      case "separator":
        break;
      case "panel":
        break;
      case "button":
        item = createButton(field);
        break;
      default:
        item = createField(field);
    }

    return item;
  }

  private void processCommon(SimpleWidget item, MetaJsonField field) {

    item.setName(field.getName());
    if (!(item instanceof Field)) {
    	item.setTitle(field.getTitle());
    }
    if (field.getReadonly()) {
      item.setReadonly(field.getReadonly());
    }
    if (field.getReadonlyIf() != null) {
      item.setReadonlyIf(field.getReadonlyIf());
    }
    if (field.getHidden()) {
      item.setHidden(field.getHidden());
    }
    item.setHideIf(field.getHideIf());
  }

  private SimpleWidget createButton(MetaJsonField field) {

    Button button = new Button();
    button.setOnClick(field.getOnClick());

    return button;
  }

  private SimpleWidget createField(MetaJsonField jsonField) {

    Field field = new Field();
    field.setWidget(jsonField.getWidget());
    field.setOnChange(jsonField.getOnChange());
    if (jsonField.getRequired()) {
      field.setRequired(jsonField.getRequired());
    }
    field.setRequiredIf(jsonField.getRequiredIf());

    return field;
  }
}
