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
package com.axelor.studio.service.module;

import com.axelor.data.csv.CSVBind;
import com.axelor.data.csv.CSVConfig;
import com.axelor.data.csv.CSVInput;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.studio.service.builder.ModelBuilderService;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

public class ModuleExportJsonModelService {

  @Inject private MetaJsonModelRepository metaJsonModelRepo;

  @Inject private MetaJsonFieldRepository metaJsonFieldRepo;

  @Inject private ModuleExportDataInitService moduleExportDataInitService;

  @Inject private ModelBuilderService modelBuilderService;

  private static final String[] JSON_HEADER =
      new String[] {
        "name",
        "title",
        "onNew",
        "onSave",
        "nameField",
        "menuParent.name",
        "menuIcon,menuBackGround",
        "menuOrder",
        "menuTitle",
        "menuTop",
        "appBuilder.code"
      };

  private static final String[] JSON_FIELD_HEADER =
      new String[] {
        "name",
        "title",
        "type",
        "model",
        "modelField",
        "jsonModelName",
        "targetModel",
        "targetJsonModel.name",
        "appBuilder.code",
        "contextField",
        "contextFieldTarget",
        "contextFieldTargetName",
        "contextFieldTitle",
        "contextFieldValue",
        "domain",
        "enumType",
        "formView",
        "gridView",
        "help",
        "hidden",
        "hideIf",
        "includeIf",
        "isWkf",
        "maxSize",
        "minSize",
        "nameField",
        "onChange",
        "onClick",
        "precision",
        "readonly",
        "readonlyIf",
        "regex",
        "required",
        "requiredIf",
        "roleNames",
        "scale",
        "selection",
        "sequence",
        "showIf",
        "valueExpr",
        "visibleInGrid",
        "widget",
        "widgetAttrs"
      };

  public void addJsonModels(String modulePrefix, ZipOutputStream zipOut, CSVConfig csvConfig)
      throws IOException {

    List<MetaJsonModel> jsonModels = metaJsonModelRepo.all().filter("self.isReal = false").fetch();

    if (jsonModels.isEmpty()) {
      return;
    }

    List<String[]> data = new ArrayList<>();
    for (MetaJsonModel jsonModel : jsonModels) {
      data.add(
          new String[] {
            jsonModel.getName(),
            jsonModel.getTitle(),
            jsonModel.getOnNew(),
            jsonModel.getOnSave(),
            jsonModel.getNameField(),
            jsonModel.getMenuParent() != null ? jsonModel.getMenuParent().getName() : null,
            jsonModel.getMenuIcon(),
            jsonModel.getMenuOrder().toString(),
            jsonModel.getMenuTitle(),
            jsonModel.getMenuTop().toString(),
            jsonModel.getAppBuilder() != null ? jsonModel.getAppBuilder().getCode() : null
          });
    }

    String fileName = modulePrefix + MetaJsonModel.class.getSimpleName() + ".csv";
    CSVInput input =
        moduleExportDataInitService.createCSVInput(
            fileName, MetaJsonModel.class.getName(), null, "self.name = :name");
    csvConfig.getInputs().add(input);

    moduleExportDataInitService.addCsv(zipOut, fileName, JSON_HEADER, data);
  }

  public void addJsonFields(String module, ZipOutputStream zipOut, CSVConfig csvConfig)
      throws IOException {

    List<MetaJsonField> jsonFields =
        metaJsonFieldRepo
            .all()
            .filter(
                "self.isWkf = true OR self.jsonModel.isReal = false OR self.model IN (SELECT fullName FROM MetaModel WHERE isReal = false AND name != 'MetaJsonRecord')")
            .fetch();

    if (jsonFields.isEmpty()) {
      return;
    }

    List<String[]> data = new ArrayList<>();
    for (MetaJsonField jsonField : jsonFields) {

      String roles = null;
      if (!jsonField.getRoles().isEmpty()) {
        List<String> roleNames =
            jsonField.getRoles().stream().map(it -> it.getName()).collect(Collectors.toList());
        roles = Joiner.on("|").join(roleNames);
      }

      MetaJsonModel jsonModel = jsonField.getJsonModel();
      String model = jsonField.getModel();
      if (jsonModel != null && jsonModel.getIsReal() && jsonField.getIsWkf()) {
        model = modelBuilderService.getModelFullName(module, jsonModel.getName());
      }

      data.add(
          new String[] {
            jsonField.getName(),
            jsonField.getTitle(),
            jsonField.getType(),
            model,
            jsonField.getModelField(),
            jsonModel != null ? jsonModel.getName() : null,
            jsonField.getTargetModel(),
            jsonField.getTargetJsonModel() != null
                ? jsonField.getTargetJsonModel().getName()
                : null,
            jsonField.getAppBuilder() != null ? jsonField.getAppBuilder().getCode() : null,
            jsonField.getContextField(),
            jsonField.getContextFieldTarget(),
            jsonField.getContextFieldTargetName(),
            jsonField.getContextFieldTitle(),
            jsonField.getContextFieldValue(),
            jsonField.getDomain(),
            jsonField.getEnumType(),
            jsonField.getFormView(),
            jsonField.getGridView(),
            jsonField.getHelp(),
            jsonField.getHidden().toString(),
            jsonField.getHideIf(),
            jsonField.getIncludeIf(),
            jsonField.getIsWkf().toString(),
            jsonField.getMaxSize().toString(),
            jsonField.getMinSize().toString(),
            jsonField.getNameField().toString(),
            jsonField.getOnChange(),
            jsonField.getOnClick(),
            jsonField.getPrecision().toString(),
            jsonField.getReadonly().toString(),
            jsonField.getReadonlyIf(),
            jsonField.getRegex(),
            jsonField.getRequired().toString(),
            jsonField.getRequiredIf(),
            roles,
            jsonField.getScale().toString(),
            jsonField.getSelection(),
            jsonField.getSequence().toString(),
            jsonField.getShowIf(),
            jsonField.getValueExpr(),
            jsonField.getVisibleInGrid().toString(),
            jsonField.getWidget(),
            jsonField.getWidgetAttrs()
          });
    }

    String fileName =
        moduleExportDataInitService.getModulePrefix(module)
            + MetaJsonField.class.getSimpleName()
            + ".csv";

    CSVInput input =
        moduleExportDataInitService.createCSVInput(
            fileName,
            MetaJsonField.class.getName(),
            null,
            "self.name = :name AND (self.jsonModel.name = :jsonModelName OR self.model = :model AND self.modelField = :modelField)");
    CSVBind bind =
        moduleExportDataInitService.createCSVBind(
            "roleNames", "roles", "self.name in :roleNames", "roleNames.split('|') as List", true);
    input.getBindings().add(bind);
    bind =
        moduleExportDataInitService.createCSVBind(
            "jsonModelName", "jsonModel", "self.name = :jsonModelName", null, true);
    input.getBindings().add(bind);
    csvConfig.getInputs().add(input);

    moduleExportDataInitService.addCsv(zipOut, fileName, JSON_FIELD_HEADER, data);
  }

  public void addJsonModelsFromExcel(
      String modulePrefix,
      List<String[]> jsonModelData,
      ZipOutputStream zipOut,
      CSVConfig csvConfig)
      throws IOException {

    if (!jsonModelData.isEmpty()) {
      String fileName = modulePrefix + MetaJsonModel.class.getSimpleName() + ".csv";
      CSVInput input =
          moduleExportDataInitService.createCSVInput(
              fileName, MetaJsonModel.class.getName(), null, "self.name = :name");
      csvConfig.getInputs().add(input);

      moduleExportDataInitService.addCsv(zipOut, fileName, JSON_HEADER, jsonModelData);
    }
  }

  public void addJsonFieldsFromExcel(
      String module, List<String[]> jsonFieldData, ZipOutputStream zipOut, CSVConfig csvConfig)
      throws IOException {

    String fileName =
        moduleExportDataInitService.getModulePrefix(module)
            + MetaJsonField.class.getSimpleName()
            + ".csv";

    CSVInput input =
        moduleExportDataInitService.createCSVInput(
            fileName,
            MetaJsonField.class.getName(),
            null,
            "self.name = :name AND (self.jsonModel.name = :jsonModelName OR self.model = :model AND self.modelField = :modelField)");
    CSVBind bind =
        moduleExportDataInitService.createCSVBind(
            "roleNames", "roles", "self.name in :roleNames", "roleNames.split('|') as List", true);
    input.getBindings().add(bind);
    bind =
        moduleExportDataInitService.createCSVBind(
            "jsonModelName", "jsonModel", "self.name = :jsonModelName", null, true);
    input.getBindings().add(bind);
    csvConfig.getInputs().add(input);

    moduleExportDataInitService.addCsv(zipOut, fileName, JSON_FIELD_HEADER, jsonFieldData);
  }
}
