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
package com.axelor.studio.service.excel.importer;

import com.axelor.apps.tool.NammingTool;
import com.axelor.common.ObjectUtils;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.meta.schema.views.MenuItem;
import com.axelor.meta.schema.views.Selection;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.studio.service.CommonService;
import com.axelor.studio.service.builder.FormBuilderService;
import com.axelor.studio.service.builder.GridBuilderService;
import com.axelor.studio.service.builder.ModelBuilderService;
import com.axelor.studio.service.module.ModuleExportService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealImporter {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static final String DOMAIN_DIR = "src/main/resources/domains/";

  public static final String VIEW_DIR = "src/main/resources/views/";

  private List<String> fieldList;

  private ExcelImporterService excelImporterService;

  private StringBuilder fieldBuilder = null;

  @Inject private ModelBuilderService modelBuilderService;

  @Inject private ModuleExportService moduleExportService;

  @Inject private FormBuilderService formBuilderService;

  @Inject private GridBuilderService gridBuilderService;

  public Map<String, ObjectViews> realImporter(
      ExcelImporterService excelImporterService,
      String module,
      DataReaderService reader,
      List<String[]> jsonFieldData,
      List<String> realModels,
      List<String> customModels,
      List<Map<String, String>> menuList,
      ZipOutputStream zipOut)
      throws IOException {

    this.excelImporterService = excelImporterService;

    Map<String, ObjectViews> viewMap = new HashMap<>();

    for (String key : realModels) {

      log.debug("Importing sheet: {}", key);
      key += "(Real)";

      fieldList = new ArrayList<>();
      fieldBuilder = new StringBuilder();
      List<Map<String, String>> fieldValues = new ArrayList<Map<String, String>>();

      int totalLines = reader.getTotalLines(key);
      if (totalLines == 0) {
        continue;
      }

      String model = key.substring(0, key.indexOf("("));

      for (int rowNum = 0; rowNum < totalLines; rowNum++) {
        if (rowNum == 0) {
          continue;
        }

        String[] row = reader.read(key, rowNum);
        if (row == null) {
          continue;
        }

        Map<String, String> valMap = ExcelImporterService.createValMap(row, CommonService.HEADERS);

        if (valMap.get(CommonService.TYPE).equals("panel")
            && valMap.get(CommonService.NAME).endsWith("(end)")) {
          continue;
        }

        if (checkAndAddCustomField(module, model, valMap, jsonFieldData, customModels)) {
          continue;
        }

        fieldValues.add(valMap);

        if (valMap.get(CommonService.TYPE).equals("onnew")
            || valMap.get(CommonService.TYPE).equals("onsave")) {
          continue;
        }

        if (!fieldList.contains(valMap.get(CommonService.NAME))) {
          createField(module, valMap);
          fieldList.add(valMap.get(CommonService.NAME));
        }
      }

      addMetaModel(module, model, zipOut, viewMap, fieldValues);
      addMenu(module, model, menuList, viewMap);
    }

    return viewMap;
  }

  private boolean checkAndAddCustomField(
      String module,
      String model,
      Map<String, String> valMap,
      List<String[]> jsonFieldData,
      List<String> customModels) {

    String type = valMap.get(CommonService.TYPE);
    String wkf = valMap.get(CommonService.WKF);

    if (!Strings.isNullOrEmpty(wkf) && wkf.equals("x")) {
      createJsonField(module, model, valMap, jsonFieldData);
      return true;

    } else if (type.contains("-to-")) {
      String targetModel = type.substring(type.indexOf("(") + 1, type.indexOf(")"));
      Object targetObj = excelImporterService.getModelFromName(targetModel);

      if (customModels.contains(targetModel) && !(targetObj instanceof MetaModel)) {
        createJsonField(module, model, valMap, jsonFieldData);
        return true;
      }
    }
    return false;
  }

  private void createField(String module, Map<String, String> valMap) {

    String type = modelBuilderService.getType(valMap.get(CommonService.TYPE));
    if (type == null) {
      return;
    }
    StringBuilder builder = new StringBuilder();
    builder.append("<");
    if (type.contains("-to-")) {
      builder.append(type.substring(0, type.indexOf("(")));
    } else {
      builder.append(type);
    }
    builder.append(" name=\"" + valMap.get(CommonService.NAME) + "\"");
    if (NammingTool.isKeyword(valMap.get(CommonService.NAME))) {
      builder.append(" column=\"" + valMap.get(CommonService.NAME) + "_val\"");
    }
    if (!Strings.isNullOrEmpty(valMap.get(CommonService.TITLE))) {
      builder.append(" title=\"" + valMap.get(CommonService.TITLE) + "\"");
    }
    if (type.contains("-to-")) {
      addRelationalAttr(module, builder, type);
    }
    if (valMap.get(CommonService.SELECT) != null) {
      builder.append(
          " selection=\""
              + valMap
                  .get(CommonService.SELECT)
                  .substring(0, valMap.get(CommonService.SELECT).indexOf("("))
              + "\"");
    }
    if (valMap.get(CommonService.REQUIRED) != null
        && valMap.get(CommonService.REQUIRED).equals("x")) {
      builder.append(" required=\"" + true + "\"");
    }
    if (valMap.get(CommonService.READONLY) != null
        && valMap.get(CommonService.READONLY).equals("x")) {
      builder.append(" readonly=\"" + true + "\"");
    }
    builder.append(" />");
    fieldBuilder.append("\t\t");
    fieldBuilder.append(builder.toString());
    fieldBuilder.append("\n");
  }

  private void addRelationalAttr(String module, StringBuilder builder, String type) {

    String targetModel = type.substring(type.indexOf("(") + 1, type.indexOf(")"));
    Object targetObj = excelImporterService.getModelFromName(targetModel);

    if (targetObj instanceof MetaModel) {
      MetaModel targetMetaModel = (MetaModel) targetObj;
      builder.append(" ref=\"" + targetMetaModel.getFullName() + "\"");

    } else {
      builder.append(" ref=\"" + modelBuilderService.getModelFullName(module, targetModel) + "\"");
    }
  }

  private void addMetaModel(
      String module,
      String model,
      ZipOutputStream zipOut,
      Map<String, ObjectViews> viewMap,
      List<Map<String, String>> fieldValues)
      throws IOException {

    String fileText = modelBuilderService.buildFromExcel(module, model, fieldBuilder);
    moduleExportService.addZipEntry(DOMAIN_DIR + model + ".xml", fileText, zipOut);

    if (fieldValues
        .stream()
        .filter(
            fieldVal ->
                (!fieldVal.get(CommonService.TYPE).equals("panel")
                        || !fieldVal.get(CommonService.TYPE).equals("onnew")
                        || !fieldVal.get(CommonService.TYPE).equals("onsave"))
                    && !Strings.isNullOrEmpty(fieldVal.get(CommonService.VISIBLE_IN_GRID))
                    && fieldVal.get(CommonService.VISIBLE_IN_GRID).equals("x"))
        .findAny()
        .isPresent()) {

      moduleExportService.updateViewMap(
          model, viewMap, gridBuilderService.buildFromExcel(module, model, fieldValues));
    }
    moduleExportService.updateViewMap(
        model, viewMap, formBuilderService.buildFromExcel(module, model, fieldValues));
    addSelectionFromExcel(fieldValues, viewMap);
  }

  private void addSelectionFromExcel(
      List<Map<String, String>> fieldValues, Map<String, ObjectViews> viewMap) {

    List<Selection> selections = new ArrayList<>();

    for (Map<String, String> valMap : fieldValues) {

      if (valMap.get(CommonService.SELECT) != null) {
        String select =
            valMap
                .get(CommonService.SELECT)
                .substring(0, valMap.get(CommonService.SELECT).indexOf("("));

        List<Option> options = getSelectionList(valMap.get(CommonService.SELECT));
        if (!ObjectUtils.isEmpty(options)) {
          Selection selection = new Selection();
          selection.setName(select);
          selection.setOptions(options);
          selections.add(selection);
        }
      }
    }

    if (!selections.isEmpty()) {
      ObjectViews views = viewMap.get("Selects");
      if (views == null) {
        views = new ObjectViews();
        views.setSelections(new ArrayList<>());
        viewMap.put("Selects", views);
      }
      views.getSelections().addAll(selections);
    }
  }

  private List<Option> getSelectionList(String select) {

    Map<String, Option> optionMap = new LinkedHashMap<>();
    String options = select.substring(select.indexOf("(") + 1, select.indexOf(")"));
    String[] optionArr = options.split(",");

    for (int i = 0; i < optionArr.length; i++) {
      Option option = getSelectionItem(optionArr[i]);
      optionMap.put(option.getValue(), option);
    }

    return new ArrayList<>(optionMap.values());
  }

  private Option getSelectionItem(String item) {
    String[] opt = item.split(":");
    Option option = new Option();
    option.setValue(opt[0]);
    option.setTitle(opt[1]);
    return option;
  }

  private void addMenu(
      String module,
      String model,
      List<Map<String, String>> menuList,
      Map<String, ObjectViews> viewMap) {

    if (!CollectionUtils.isEmpty(menuList)) {
      ObjectViews views = viewMap.get("Menu");
      if (views == null) {
        views = new ObjectViews();
        views.setMenus(new ArrayList<>());
        views.setActions(new ArrayList<>());
        viewMap.put("Menu", views);
      }

      for (Map<String, String> menuValMap : menuList) {
        if (model.equals(menuValMap.get(CommonService.OBJECT))) {
          if (!Strings.isNullOrEmpty(menuValMap.get(CommonService.PARENT))) {
            addParentMenu(module, model, menuValMap, menuList, viewMap);
          }
          addMenu(module, model, menuValMap, viewMap);
          break;
        }
      }
    }
  }

  private void addParentMenu(
      String module,
      String model,
      Map<String, String> menuValMap,
      List<Map<String, String>> menuList,
      Map<String, ObjectViews> viewMap) {

    for (Map<String, String> parentMenuValMap : menuList) {
      if (menuValMap
          .get(CommonService.PARENT)
          .equals(parentMenuValMap.get(CommonService.MENU_NAME))) {

        if (!Strings.isNullOrEmpty(parentMenuValMap.get(CommonService.PARENT))) {
          addParentMenu(module, model, parentMenuValMap, menuList, viewMap);

        } else {

          boolean isMenuPresent =
              viewMap
                  .get("Menu")
                  .getMenus()
                  .stream()
                  .filter(
                      menu -> menu.getName().equals(parentMenuValMap.get(CommonService.MENU_NAME)))
                  .findAny()
                  .isPresent();

          if (!isMenuPresent) {
            addMenu(module, model, parentMenuValMap, viewMap);
          }
        }
      }
    }
  }

  private void addMenu(
      String module,
      String simpleModel,
      Map<String, String> menuValMap,
      Map<String, ObjectViews> viewMap) {

    String model = modelBuilderService.getModelFullName(module, simpleModel);

    MenuItem menu = new MenuItem();
    menu.setName(menuValMap.get(CommonService.MENU_NAME));
    menu.setTitle(menuValMap.get(CommonService.MENU_TITLE));
    if (!Strings.isNullOrEmpty(menuValMap.get(CommonService.ORDER))
        && !menuValMap.get(CommonService.ORDER).equals("0")) {
      menu.setOrder(Integer.parseInt(menuValMap.get(CommonService.ORDER)));
    }
    if (!Strings.isNullOrEmpty(menuValMap.get(CommonService.PARENT))) {
      menu.setParent(menuValMap.get(CommonService.PARENT));
    }
    if (!Strings.isNullOrEmpty(menuValMap.get(CommonService.ICON))) {
      menu.setIcon(menuValMap.get(CommonService.ICON));
    }
    if (!Strings.isNullOrEmpty(menuValMap.get(CommonService.BACKGROUND))) {
      menu.setIconBackground(menuValMap.get(CommonService.BACKGROUND));
    }
    //    menu.setXmlId(module + "-" + menu.getName());

    if (model != null && !Strings.isNullOrEmpty(menuValMap.get(CommonService.ACTION))) {
      String name = menuValMap.get(CommonService.ACTION);
      ActionViewBuilder builder = ActionView.define(menuValMap.get(CommonService.MENU_TITLE));
      builder.model(model);
      builder.domain(null);
      String views = menuValMap.get(CommonService.VIEWS);
      if (!Strings.isNullOrEmpty(views)) {
        String viewNames[] = views.split(",");
        for (int i = 0; i < viewNames.length; i++) {
          if (viewNames[i].contains("grid")) {
            builder.add("grid", viewNames[i]);
          } else if (viewNames[i].contains("form")) {
            builder.add("form", viewNames[i]);
          }
        }
      }
      ActionView actionView = builder.get();
      actionView.setName(name);
      //        actionView.setXmlId(module + "." + name);
      viewMap.get("Menu").getActions().add(actionView);
      menu.setAction(name);
    }

    viewMap.get("Menu").getMenus().add(menu);
  }

  private void createJsonField(
      String moduleName,
      String modelName,
      Map<String, String> valMap,
      List<String[]> jsonFieldData) {

    String name = valMap.get(CommonService.NAME);
    String type = valMap.get(CommonService.TYPE);
    String targetJsonModelName = null;
    if (type.contains("-to-")) {
      targetJsonModelName = type.substring(type.indexOf("(") + 1, type.indexOf(")"));
      type = "json-" + type.substring(0, type.indexOf("("));
    }

    String model = modelBuilderService.getModelFullName(moduleName, modelName);
    String modelField = "attrs";

    jsonFieldData.add(
        new String[] {
          type.equals("panel") ? name.replace("(start)", "") : name,
          valMap.get(CommonService.TITLE),
          type,
          model,
          modelField,
          null,
          null,
          targetJsonModelName,
          null,
          valMap.get(CommonService.CONTEXT_FIELD),
          valMap.get(CommonService.CONTEXT_FIELD_TARGET),
          valMap.get(CommonService.CONTEXT_FIELD_TARGET_NAME),
          valMap.get(CommonService.CONTEXT_FIELD_TITLE),
          valMap.get(CommonService.CONTEXT_FIELD_VALUE),
          valMap.get(CommonService.DOMAIN),
          valMap.get(CommonService.ENUM_TYPE),
          valMap.get(CommonService.FORM_VIEW),
          valMap.get(CommonService.GRID_VIEW),
          valMap.get(CommonService.HELP),
          (valMap.get(CommonService.HIDDEN) != null && valMap.get(CommonService.HIDDEN).equals("x"))
              ? "true"
              : "false",
          (valMap.get(CommonService.HIDDEN) != null
                  && !valMap.get(CommonService.HIDDEN).equals("x"))
              ? valMap.get(CommonService.HIDDEN)
              : null,
          valMap.get(CommonService.INCLUDE_IF),
          null,
          valMap.get(CommonService.MAX_SIZE),
          valMap.get(CommonService.MIN_SIZE),
          (valMap.get(CommonService.NAME_FIELD) != null
                  && valMap.get(CommonService.NAME_FIELD).equals("x"))
              ? "true"
              : null,
          valMap.get(CommonService.ON_CHANGE),
          valMap.get(CommonService.ON_CLICK),
          valMap.get(CommonService.PRECISION),
          (valMap.get(CommonService.READONLY) != null
                  && valMap.get(CommonService.READONLY).equals("x"))
              ? "true"
              : "false",
          (valMap.get(CommonService.READONLY) != null
                  && !valMap.get(CommonService.READONLY).equals("x"))
              ? valMap.get(CommonService.READONLY)
              : null,
          valMap.get(CommonService.REGEX),
          (valMap.get(CommonService.REQUIRED) != null
                  && valMap.get(CommonService.REQUIRED).equals("x"))
              ? "true"
              : "false",
          (valMap.get(CommonService.REQUIRED) != null
                  && !valMap.get(CommonService.REQUIRED).equals("x"))
              ? valMap.get(CommonService.REQUIRED)
              : null,
          valMap.get(CommonService.ROLES),
          valMap.get(CommonService.SCALE),
          valMap.get(CommonService.SELECT) != null
              ? valMap
                  .get(CommonService.SELECT)
                  .substring(0, valMap.get(CommonService.SELECT).indexOf("("))
              : null,
          valMap.get(CommonService.SEQUENCE),
          valMap.get(CommonService.SHOW_IF),
          valMap.get(CommonService.VALUE_EXPR),
          (valMap.get(CommonService.VISIBLE_IN_GRID) != null
                  && valMap.get(CommonService.VISIBLE_IN_GRID).equals("x"))
              ? "true"
              : null,
          valMap.get(CommonService.WIDGET),
          valMap.get(CommonService.WIDGET_ATTRS)
        });
  }
}
