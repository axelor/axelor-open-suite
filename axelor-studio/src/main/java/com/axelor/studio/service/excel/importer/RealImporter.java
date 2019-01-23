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

import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.views.FormView;
import com.axelor.meta.schema.views.Selection;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.studio.service.CommonService;
import com.axelor.studio.service.builder.FormBuilderService;
import com.axelor.studio.service.builder.ModelBuilderService;
import com.axelor.studio.service.module.ModuleExportService;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.JAXBException;

public class RealImporter {

  public static final String DOMAIN_DIR = "src/main/resources/domains/";

  public static final String VIEW_DIR = "src/main/resources/views/";

  private ExcelImporterService excelImporterService;

  private StringBuilder fieldBuilder = null;

  @Inject private ModelBuilderService modelBuilderService;

  @Inject private ModuleExportService moduleExportService;

  @Inject private FormBuilderService formBuilderService;

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private MetaJsonModelRepository metaJsonModelRepo;

  public void realImporter(
      ExcelImporterService excelImporterService,
      String module,
      DataReaderService reader,
      String key,
      Map<String, ObjectViews> viewMap,
      ZipOutputStream zipOut)
      throws IOException, JAXBException, AxelorException {

    this.excelImporterService = excelImporterService;

    String model = key.substring(0, key.indexOf("("));
    int totalLines = reader.getTotalLines(key);

    fieldBuilder = new StringBuilder();
    List<Map<String, String>> fieldValues = new ArrayList<Map<String, String>>();

    for (int rowNum = 0; rowNum < totalLines; rowNum++) {
      if (rowNum == 0) {
        continue;
      }

      String[] row = reader.read(key, rowNum);
      if (row == null) {
        continue;
      }

      Map<String, String> valMap = excelImporterService.createValMap(row, CommonService.HEADERS);

      if (valMap.get(CommonService.TYPE).equals("panel")
          && valMap.get(CommonService.NAME).endsWith("(end)")) {
        continue;
      }

      fieldValues.add(valMap);

      if (valMap.get(CommonService.TYPE).equals("onnew")
          || valMap.get(CommonService.TYPE).equals("onsave")) {
        continue;
      }

      createField(module, valMap);
    }

    addMetaModel(module, model, zipOut, viewMap, fieldValues);
    addMenu(module, model, viewMap);
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
    builder.append(" title=\"" + valMap.get(CommonService.TITLE) + "\"");
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
    } else if (targetObj instanceof MetaJsonModel) {
      builder.append(" ref=\"" + targetModel + "\"");
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
      throws IOException, AxelorException, JAXBException {

    String fileText = modelBuilderService.buildFromExcel(module, model, fieldBuilder);
    moduleExportService.addZipEntry(DOMAIN_DIR + model + ".xml", fileText, zipOut);
    FormView formView = formBuilderService.buildFromExcel(module, model, fieldValues);
    moduleExportService.updateViewMap(model, viewMap, formView);
    addSelectionFromExcel(fieldValues, viewMap);
  }

  private void addSelectionFromExcel(
      List<Map<String, String>> fieldValues, Map<String, ObjectViews> viewMap)
      throws AxelorException, JAXBException {

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

  private void addMenu(String module, String model, Map<String, ObjectViews> viewMap)
      throws JAXBException {

    MetaModel metaModel = metaModelRepo.findByName(model);
    if (metaModel != null) {
      return;
    }

    MetaJsonModel jsonModel = metaJsonModelRepo.findByName(model);
    if (jsonModel == null) {
      return;
    }

    MetaMenu menu = jsonModel.getMenu();
    if (menu == null) {
      return;
    }

    ObjectViews views = viewMap.get("Menu");
    if (views == null) {
      views = new ObjectViews();
      views.setMenus(new ArrayList<>());
      views.setActions(new ArrayList<>());
      viewMap.put("Menu", views);
    }

    if (menu.getParent() != null) {
      moduleExportService.addMenu(model, null, menu.getParent(), viewMap);
    }
    moduleExportService.addMenu(module, model, menu, viewMap);
  }
}
