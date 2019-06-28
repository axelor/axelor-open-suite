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
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Button;
import com.axelor.meta.schema.views.Extend;
import com.axelor.meta.schema.views.ExtendItemInsert;
import com.axelor.meta.schema.views.Field;
import com.axelor.meta.schema.views.GridView;
import com.axelor.meta.schema.views.Position;
import com.axelor.meta.schema.views.SimpleWidget;
import com.axelor.studio.service.CommonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

public class GridBuilderService {

  private List<Extend> extendList = null;

  private Extend extend = null;

  private ExtendItemInsert insert = null;

  private List<ExtendItemInsert> insertList = null;

  private List<AbstractWidget> insertItemList = null;

  @Inject private ViewBuilderService viewBuilderService;

  @Inject private MetaJsonFieldRepository metaJsonFieldRepo;

  @Inject private ModelBuilderService modelBuilderService;

  @Inject private MetaViewRepository metaViewRepo;

  @Inject private MetaFieldRepository metaFieldRepo;

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

      if (!field.getVisibleInGrid() || field.getIsWkf()) {
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

  public GridView buildFromExcel(
      String module, String model, List<Map<String, String>> fieldValues) {

    String name = Inflector.getInstance().dasherize(model) + "-grid";
    GridView gridView = new GridView();

    try {
      MetaView metaView =
          metaViewRepo
              .all()
              .filter("self.name = ?1 and type = 'grid' and xmlId is null", name)
              .fetchOne();

      if (metaView != null) {
        addItemsFromExcel(model, fieldValues);
        gridView.setModel(metaView.getModel());
        gridView.setName(metaView.getName());
        gridView.setTitle(metaView.getTitle());
        gridView.setXmlId(module + "-" + name);
        gridView.setExtension(true);
        gridView.setExtends(extendList);

      } else {
        gridView.setModel(modelBuilderService.getModelFullName(module, model));
        gridView.setName(name);
        gridView.setTitle(Inflector.getInstance().humanize(model));
        List<AbstractWidget> items = createItemsFromExcel(fieldValues);
        gridView.setItems(items);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return gridView;
  }

  private void addItemsFromExcel(String model, List<Map<String, String>> fieldValues) {

    extendList = new ArrayList<>();
    insertItemList = new ArrayList<>();
    insertList = new ArrayList<>();

    for (Map<String, String> valMap : fieldValues) {

      if (valMap.get(CommonService.TYPE).startsWith("panel")
          || valMap.get(CommonService.TYPE).equals("onnew")
          || valMap.get(CommonService.TYPE).equals("onsave")) {
        continue;
      }

      if (Strings.isNullOrEmpty(valMap.get(CommonService.VISIBLE_IN_GRID))
          || !valMap.get(CommonService.VISIBLE_IN_GRID).equals("x")) {
        continue;
      }

      MetaField metaField =
          metaFieldRepo
              .all()
              .filter(
                  "self.name = ?1 and self.metaModel.name = ?2",
                  valMap.get(CommonService.NAME),
                  model)
              .fetchOne();

      if (metaField != null) {
        continue;
      }

      addItems(valMap);
    }
    if (insert != null) {
      insert.setItems(insertItemList);
      insertList.add(insert);
      extend.setInserts(insertList);
    }
    extendList.add(extend);
  }

  private void addItems(Map<String, String> valMap) {

    HashMap<String, Object> widgetAttrs = getWidgetAttrs(valMap);

    if (extend == null) {
      extend = new Extend();
      insert = new ExtendItemInsert();
      extend.setTarget("/");
      insert.setPosition(Position.AFTER);
    }

    SimpleWidget item = createSimpleItem(valMap);
    if (item != null) {
      processCommon(item, valMap, widgetAttrs);
      insertItemList.add(item);
    }
  }

  private List<AbstractWidget> createItemsFromExcel(List<Map<String, String>> fieldValues) {

    List<AbstractWidget> items = new ArrayList<>();

    for (Map<String, String> valMap : fieldValues) {

      if (valMap.get(CommonService.TYPE).startsWith("panel")
          || valMap.get(CommonService.TYPE).equals("onnew")
          || valMap.get(CommonService.TYPE).equals("onsave")) {
        continue;
      }

      if (Strings.isNullOrEmpty(valMap.get(CommonService.VISIBLE_IN_GRID))
          || !valMap.get(CommonService.VISIBLE_IN_GRID).equals("x")) {
        continue;
      }

      HashMap<String, Object> widgetAttrs = getWidgetAttrs(valMap);

      SimpleWidget item = createSimpleItem(valMap);
      if (item != null) {
        processCommon(item, valMap, widgetAttrs);
        items.add(item);
      }
    }

    return items;
  }

  private SimpleWidget createSimpleItem(Map<String, String> valMap) {

    SimpleWidget item = null;
    switch (valMap.get(CommonService.TYPE)) {
      case "separator":
        break;
      case "panel":
        break;
      case "button":
        item = createButton(valMap);
        break;
      default:
        item = createField(valMap);
    }

    return item;
  }

  private SimpleWidget createButton(Map<String, String> valMap) {

    Button button = new Button();
    if (!Strings.isNullOrEmpty(valMap.get(CommonService.ON_CLICK))) {
      button.setOnClick(valMap.get(CommonService.ON_CLICK));
    }

    return button;
  }

  private SimpleWidget createField(Map<String, String> valMap) {

    Field field = new Field();
    if (!Strings.isNullOrEmpty(valMap.get(CommonService.DOMAIN))) {
      field.setDomain(valMap.get(CommonService.DOMAIN));
    }
    if (!Strings.isNullOrEmpty(valMap.get(CommonService.WIDGET))) {
      field.setWidget(valMap.get(CommonService.WIDGET));
    }
    if (!Strings.isNullOrEmpty(valMap.get(CommonService.ON_CHANGE))) {
      field.setOnChange(valMap.get(CommonService.ON_CHANGE));
    }
    if (!Strings.isNullOrEmpty(valMap.get(CommonService.REQUIRED))
        && valMap.get(CommonService.REQUIRED).equals("x")) {
      field.setRequired(true);
    }
    if (!Strings.isNullOrEmpty(valMap.get(CommonService.REQUIRED))
        && !valMap.get(CommonService.REQUIRED).equals("x")) {
      field.setRequiredIf(valMap.get(CommonService.REQUIRED));
    }
    if (!Strings.isNullOrEmpty(valMap.get(CommonService.MIN_SIZE))
        && !valMap.get(CommonService.MIN_SIZE).equals("0")) {
      field.setMinSize(valMap.get(CommonService.MIN_SIZE));
    }
    if (!Strings.isNullOrEmpty(valMap.get(CommonService.MAX_SIZE))
        && !valMap.get(CommonService.MAX_SIZE).equals("0")) {
      field.setMaxSize(valMap.get(CommonService.MAX_SIZE));
    }

    return field;
  }

  @SuppressWarnings("unchecked")
  private HashMap<String, Object> getWidgetAttrs(Map<String, String> valMap) {

    HashMap<String, Object> widgetAttrs = new HashMap<>();

    if (valMap.get(CommonService.WIDGET_ATTRS) != null) {
      try {
        widgetAttrs =
            new ObjectMapper().readValue(valMap.get(CommonService.WIDGET_ATTRS), HashMap.class);
      } catch (IOException e) {
      }
    }

    return widgetAttrs;
  }

  private void processCommon(
      SimpleWidget item, Map<String, String> valMap, HashMap<String, Object> widgetAttrs) {

    item.setName(valMap.get(CommonService.NAME));
    item.setTitle(valMap.get(CommonService.TITLE));

    if (!Strings.isNullOrEmpty(valMap.get(CommonService.READONLY))
        && valMap.get(CommonService.READONLY).equals("x")) {
      item.setReadonly(true);
    }
    if (!Strings.isNullOrEmpty(valMap.get(CommonService.READONLY))
        && !valMap.get(CommonService.READONLY).equals("x")) {
      item.setReadonlyIf("$get(" + valMap.get(CommonService.READONLY) + ")");
    }
    if (!Strings.isNullOrEmpty(valMap.get(CommonService.HIDDEN))
        && valMap.get(CommonService.HIDDEN).equals("x")) {
      item.setHidden(true);
    }
    if (!Strings.isNullOrEmpty(valMap.get(CommonService.HIDDEN))
        && !valMap.get(CommonService.HIDDEN).equals("x")) {
      item.setHideIf("$get(" + valMap.get(CommonService.HIDDEN) + ")");
    }
    if (!Strings.isNullOrEmpty(valMap.get(CommonService.SHOW_IF))) {
      item.setShowIf("$get(" + valMap.get(CommonService.SHOW_IF) + ")");
    }

    Map<QName, String> otherAttrs = new HashMap<>();
    for (String key : widgetAttrs.keySet()) {
      if (key.equals("col") || key.equals("width") || key.equals("tab")) {
        continue;
      }
      if (key.equals("showTitle") && widgetAttrs.get("showTitle").equals("true")) {
        continue;
      }
      otherAttrs.put(new QName(key), widgetAttrs.get(key).toString());
    }
    item.setOtherAttributes(otherAttrs);
  }
}
