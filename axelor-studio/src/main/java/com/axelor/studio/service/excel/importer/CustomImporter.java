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

import com.axelor.common.Inflector;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.studio.service.CommonService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomImporter {

  private List<String[]> jsonFieldData = new ArrayList<>();

  private List<String[]> jsonModelData = new ArrayList<>();

  private Inflector inflector = Inflector.getInstance();

  private ExcelImporterService excelImporterService;

  @Inject private MetaFieldRepository metaFieldRepo;

  @Inject private MetaViewRepository metaViewRepo;

  public void customImport(
      ExcelImporterService excelImporterService,
      DataReaderService reader,
      String key,
      List<String[]> jsonModelData,
      List<String[]> jsonFieldData) {

    this.excelImporterService = excelImporterService;
    this.jsonModelData = jsonModelData;
    this.jsonFieldData = jsonFieldData;

    String model = key.substring(0, key.indexOf("("));

    String modelName = "";
    Object obj = excelImporterService.getModelFromName(model);

    if (obj instanceof MetaModel) {
      MetaModel metaModel = (MetaModel) obj;
      modelName = metaModel.getName();

    } else {
      addJsonModels(model, reader, key);
      modelName = model;
    }
    addJsonFields(reader, key, modelName);
  }

  private void addJsonModels(String model, DataReaderService reader, String key) {

    if (Strings.isNullOrEmpty(model)) {
      return;
    }

    String modelName = inflector.camelize(inflector.simplify(model));
    String onnew = null;
    String onsave = null;

    for (int rowNum = 0; rowNum < 3; rowNum++) {
      if (rowNum == 0) {
        continue;
      }

      String[] row = reader.read(key, rowNum);
      if (row == null) {
        continue;
      }

      Map<String, String> valMap = excelImporterService.createValMap(row, CommonService.HEADERS);

      if (valMap.get(CommonService.TYPE).equals("onnew")
          && valMap.get(CommonService.FORMULA) != null) {
        onnew = valMap.get(CommonService.FORMULA);
      } else if (valMap.get(CommonService.TYPE).equals("onsave")
          && valMap.get(CommonService.FORMULA) != null) {
        onsave = valMap.get(CommonService.FORMULA);
      }
    }

    jsonModelData.add(
        new String[] {
          modelName,
          inflector.humanize(modelName),
          onnew,
          onsave,
          null,
          null,
          null,
          "0",
          null,
          "false",
          null
        });
  }

  private void addJsonFields(DataReaderService reader, String key, String modelName) {

    if (modelName == null) {
      return;
    }

    int totalLines = reader.getTotalLines(key);

    for (int rowNum = 0; rowNum < totalLines; rowNum++) {
      if (rowNum == 0) {
        continue;
      }

      String[] row = reader.read(key, rowNum);
      if (row == null) {
        continue;
      }

      Map<String, String> valMap = excelImporterService.createValMap(row, CommonService.HEADERS);

      if ((valMap.get(CommonService.TYPE).equals("panel")
              && valMap.get(CommonService.NAME).contains("end"))
          || valMap.get(CommonService.TYPE).equals("onnew")
          || valMap.get(CommonService.TYPE).equals("onsave")) {
        continue;
      }

      createJsonField(modelName, valMap);
    }
  }

  private void createJsonField(String modelName, Map<String, String> valMap) {

    String name = valMap.get(CommonService.NAME);
    Object obj = excelImporterService.getModelFromName(modelName);

    String type = valMap.get(CommonService.TYPE);
    String targetModelName = null;
    String targetJsonModelName = null;
    if (type.contains("-to-")) {
      String targetModel = type.substring(type.indexOf("(") + 1, type.indexOf(")"));
      Object targetObj = excelImporterService.getModelFromName(targetModel);
      type = type.substring(0, type.indexOf("("));

      if (targetObj instanceof MetaModel) {
        MetaModel targetMetaModel = (MetaModel) targetObj;
        targetModelName = targetMetaModel.getFullName();

      } else if (targetObj instanceof MetaJsonModel) {
        MetaJsonModel targetJsonModel = (MetaJsonModel) targetObj;
        type = "json-" + type;
        targetJsonModelName = targetJsonModel.getName();

      } else {
        targetJsonModelName = targetModel;
      }
    }

    String model = "com.axelor.meta.db.MetaJsonRecord";
    String modelField = "attrs";
    String jsonModelName = null;

    if (obj instanceof MetaModel) {
      MetaModel metaModel = (MetaModel) obj;
      model = metaModel.getFullName();
      String attrsField = findAttrsFromView(metaModel, valMap);
      modelField = attrsField;

    } else if (obj instanceof MetaJsonModel) {
      MetaJsonModel jsonModel = (MetaJsonModel) obj;
      jsonModelName = jsonModel.getName();

    } else {
      jsonModelName = modelName;
    }

    jsonFieldData.add(
        new String[] {
          type.equals("panel") ? name.replace("(start)", "") : name,
          valMap.get(CommonService.TITLE),
          type,
          model,
          modelField,
          jsonModelName,
          targetModelName,
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

  private String findAttrsFromView(MetaModel metaModel, Map<String, String> valMap) {
    String view = valMap.get(CommonService.VIEW);
    String viewName = view.substring(view.indexOf("(") + 1, view.indexOf(")"));

    List<MetaField> metaFields =
        metaFieldRepo
            .all()
            .filter("self.json = true and self.metaModel.id = ?", metaModel.getId())
            .fetch();

    MetaField metaField = null;
    MetaView metaView = metaViewRepo.findByName(viewName);
    if (metaView != null) {
      metaField =
          metaFields
              .stream()
              .filter(field -> metaView.getXml().contains(field.getName()))
              .findFirst()
              .orElse(null);
    }

    if (metaField != null) {
      return metaField.getName();
    } else {
      return "attrs";
    }
  }
}
