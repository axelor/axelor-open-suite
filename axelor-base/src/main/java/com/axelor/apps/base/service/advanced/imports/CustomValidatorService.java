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
package com.axelor.apps.base.service.advanced.imports;

import com.axelor.apps.base.db.FileField;
import com.axelor.apps.base.db.FileTab;
import com.axelor.apps.base.db.repo.FileFieldRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.advancedExport.AdvancedExportService;
import com.axelor.common.Inflector;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class CustomValidatorService {

  @Inject private ValidatorService validatorService;

  @Inject private AdvancedImportService advancedImportService;

  @Inject private CustomAdvancedImportService customAdvancedImportService;

  @Inject private MetaJsonFieldRepository metaJsonFieldRepo;

  public void validateObjectRequiredJsonFields(
      List<MetaJsonField> jsonFields, List<String> fieldList, LogService logService) {
    for (MetaJsonField field : jsonFields) {
      if (field.getRequired() && !fieldList.contains(field.getName())) {
        logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_3, field.getName(), null);
      }
    }
  }

  public void validateJsonFields(int line, boolean isConfig, FileTab fileTab, LogService logService)
      throws ClassNotFoundException, IOException {

    List<String> relationalFieldList =
        fileTab.getFileFieldList().stream()
            .filter(field -> !Strings.isNullOrEmpty(field.getSubImportField()))
            .map(field -> field.getJsonField().getName() + "." + field.getSubImportField())
            .collect(Collectors.toList());

    for (FileField fileField : fileTab.getFileFieldList()) {
      MetaJsonField importField = fileField.getJsonField();

      if (importField == null) {
        logService.addLog(
            BaseExceptionMessage.ADVANCED_IMPORT_LOG_2, fileField.getColumnTitle(), line);
        continue;
      }

      if (Strings.isNullOrEmpty(fileField.getSubImportField())) {
        if (importField.getTargetJsonModel() != null
            || !Strings.isNullOrEmpty(importField.getTargetModel())) {
          logService.addLog(
              BaseExceptionMessage.ADVANCED_IMPORT_LOG_4, importField.getName(), line);
          continue;
        }
        validateJsonImportRequiredFields(
            line,
            importField.getJsonModel(),
            importField.getName(),
            fileField,
            relationalFieldList,
            logService);
        validatorService.validateDateField(line, fileField);
      } else if (!Strings.isNullOrEmpty(fileField.getSubImportField())) {
        Integer rowNum = fileField.getIsMatchWithFile() ? line : null;
        Object object =
            this.validateJsonSubField(
                importField,
                fileField.getSubImportField().split("\\."),
                this.getJsonFieldName(fileField),
                0,
                rowNum,
                false,
                logService);

        checkAndValidateObjectRequiredFields(
            object, fileField, relationalFieldList, line, logService);
      }
    }
  }

  public Object validateJsonSubField(
      MetaJsonField jsonField,
      String[] subFields,
      String field,
      int index,
      Integer line,
      boolean isLog,
      LogService logService)
      throws ClassNotFoundException, IOException {
    Object object = null;

    if (jsonField == null) {
      if (!isLog) {
        logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_7, field, line);
      }
      return object;
    }

    object = jsonField;
    if (subFields.length >= index
        && jsonField.getTargetJsonModel() == null
        && Strings.isNullOrEmpty(jsonField.getTargetModel())
        && !isLog) {
      logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_7, field, line);
    }

    if (jsonField.getTargetJsonModel() != null) {
      object =
          this.getValidatedSubCustomField(
              jsonField.getTargetJsonModel(), subFields, field, index, line, isLog, logService);

    } else if (!Strings.isNullOrEmpty(jsonField.getTargetModel())) {
      object =
          this.getValidatedSubRealField(
              jsonField.getTargetModel(), subFields, field, index, line, isLog, logService);
    }
    return object;
  }

  private Object getValidatedSubCustomField(
      MetaJsonModel model,
      String[] subFields,
      String field,
      int index,
      Integer line,
      boolean isLog,
      LogService logService)
      throws ClassNotFoundException, IOException {

    MetaJsonField jsonField =
        customAdvancedImportService.getJsonField(subFields[index], null, null, model);

    index += 1;
    if (jsonField == null) {
      if (!isLog) {
        logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_7, field, line);
      }

    } else if (CustomAdvancedImportServiceImpl.relationTypeList.contains(jsonField.getType())) {
      if (subFields.length <= index) {
        if (!isLog) {
          logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_4, field, line);
        }
      } else {
        return this.validateJsonSubField(
            jsonField, subFields, field, index, line, isLog, logService);
      }
    } else if (subFields.length > index) {
      if (!isLog) {
        logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_7, field, line);
      }
    }
    return jsonField;
  }

  private Object getValidatedSubRealField(
      String fullModelName,
      String[] subFields,
      String field,
      int index,
      Integer line,
      boolean isLog,
      LogService logService)
      throws ClassNotFoundException, IOException {
    Mapper mapper = advancedImportService.getMapper(fullModelName);
    Property parentProp = mapper.getProperty(subFields[index]);

    index += 1;
    if (parentProp == null) {
      if (!isLog) {
        logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_7, field, line);
      }
      return null;
    }

    if (AdvancedExportService.FIELD_ATTRS.equals(parentProp.getName())
        || parentProp.getTarget() != null) {

      if (subFields.length <= index) {
        if (!isLog) {
          logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_4, field, line);
        }

      } else if (parentProp.getTarget() != null) {
        return validatorService.getAndValidateSubField(
            subFields, index, line, parentProp, field, false);

      } else {
        return this.getValidatedAttrsSubField(
            subFields, fullModelName, field, index, line, isLog, logService);
      }
    } else if (index < subFields.length) {
      if (!isLog) {
        logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_7, field, line);
      }
      return null;
    }
    return parentProp;
  }

  public Object getValidatedAttrsSubField(
      String[] subFields,
      String fullModelName,
      String field,
      int index,
      Integer line,
      boolean isLog,
      LogService logService)
      throws ClassNotFoundException, IOException {
    if (subFields.length <= index) {
      if (!isLog) {
        logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_4, field, line);
      }
      return null;
    }
    MetaJsonField jsonField =
        customAdvancedImportService.getJsonField(subFields[index], fullModelName, null, null);

    index += 1;
    if (CustomAdvancedImportService.relationTypeList.contains(jsonField.getType())) {

      if (subFields.length <= index) {
        if (!isLog) {
          logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_4, field, line);
        }
      } else {
        return this.validateJsonSubField(
            jsonField, subFields, field, index, line, isLog, logService);
      }
    } else if (index < subFields.length) {
      if (!isLog) {
        logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_7, field, line);
      }
      return null;
    }
    return jsonField;
  }

  public void validateJsonImportRequiredFields(
      int line,
      MetaJsonModel jsonModel,
      String fieldName,
      FileField fileField,
      List<String> relationalFieldList,
      LogService logService)
      throws ClassNotFoundException {

    if (jsonModel == null) {
      return;
    }

    String field =
        fileField.getFileTab().getIsJson()
            ? getJsonFieldName(fileField)
            : validatorService.getField(fileField);

    Integer rowNum = fileField.getIsMatchWithFile() ? line : null;
    int importType = fileField.getImportType();

    for (MetaJsonField jsonField :
        jsonModel.getFields().stream()
            .filter(jsonField -> jsonField.getRequired())
            .collect(Collectors.toList())) {
      if (jsonField.getName().equals(fieldName)
          && importType == FileFieldRepository.IMPORT_TYPE_IGNORE_EMPTY) {
        logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_5, field, rowNum);
      } else if ((importType == FileFieldRepository.IMPORT_TYPE_FIND_NEW
              || importType == FileFieldRepository.IMPORT_TYPE_NEW)
          && field.contains(".")) {

        String newField = StringUtils.substringBeforeLast(field, ".");
        newField = newField + "." + jsonField.getName();

        if (!relationalFieldList.contains(newField)) {
          logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_3, newField, null);
        }
      }
    }
  }

  public void validateJsonData(
      String[] dataRow,
      int line,
      boolean isConfig,
      FileTab fileTab,
      LogService logService,
      Map<String, Object> map)
      throws IOException, ClassNotFoundException {

    for (int fieldIndex = 0; fieldIndex < fileTab.getFileFieldList().size(); fieldIndex++) {
      FileField fileField = fileTab.getFileFieldList().get(fieldIndex);

      if (!fileField.getIsMatchWithFile() || !Strings.isNullOrEmpty(fileField.getExpression())) {
        continue;
      }

      String key = null;
      if (isConfig) {
        key = this.getJsonFieldName(fileField);
      } else {
        key = fileField.getColumnTitle();
      }

      int cellIndex = 0;
      if (map.containsKey(key)) {
        cellIndex = (int) map.get(key);
      }

      cellIndex =
          (!isConfig && !fileTab.getAdvancedImport().getIsHeader()) ? fieldIndex : cellIndex;

      MetaJsonField jsonField = fileField.getJsonField();
      if (Strings.isNullOrEmpty(fileField.getSubImportField())) {
        if (this.validateJsonDataRequiredField(
            dataRow, cellIndex, line, fileField, jsonField, logService)) {
          continue;
        }

        if (!Strings.isNullOrEmpty(jsonField.getSelection())
            && fileField.getForSelectUse() != FileFieldRepository.SELECT_USE_VALUES) {
          continue;
        }
        validatorService.validateDataType(
            dataRow, cellIndex, line, fileField.getTargetType(), fileField);
      } else {
        Object object =
            this.validateJsonSubField(
                jsonField,
                fileField.getSubImportField().split("\\."),
                key,
                0,
                line,
                true,
                logService);

        checkAndValidateObjectData(object, dataRow, fileField, cellIndex, line, logService);
      }
    }
  }

  private boolean validateJsonDataRequiredField(
      String row[],
      int cell,
      int line,
      FileField fileField,
      MetaJsonField jsonField,
      LogService logService)
      throws ClassNotFoundException, IOException {
    boolean flag = false;
    int importType = fileField.getImportType();
    String field =
        fileField.getIsJson() ? getJsonFieldName(fileField) : validatorService.getField(fileField);

    if (jsonField != null) {
      if (jsonField.getRequired()
          && importType != FileFieldRepository.IMPORT_TYPE_FIND
          && Strings.isNullOrEmpty(row[cell])) {
        logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_8, field, line);
      } else if (importType == FileFieldRepository.IMPORT_TYPE_IGNORE_EMPTY) {
        flag = true;
      }
    }
    return flag;
  }

  public void checkAndValidateObjectRequiredFields(
      Object object,
      FileField fileField,
      List<String> relationalFieldList,
      int line,
      LogService logService)
      throws ClassNotFoundException, IOException {
    if (object != null) {
      if (object instanceof MetaJsonField) {
        MetaJsonField jsonField = (MetaJsonField) object;
        validateJsonImportRequiredFields(
            line,
            jsonField.getJsonModel(),
            jsonField.getName(),
            fileField,
            relationalFieldList,
            logService);

      } else if (object instanceof Property) {
        Property prop = (Property) object;
        validatorService.validateImportRequiredField(
            line, prop.getEntity(), prop.getName(), fileField, relationalFieldList);
      }
    }
    validatorService.validateDateField(line, fileField);
  }

  public void checkAndValidateObjectData(
      Object object,
      String[] dataRow,
      FileField fileField,
      int cellIndex,
      int line,
      LogService logService)
      throws IOException, ClassNotFoundException {
    if (object != null) {
      String selection = null;
      String fieldType = null;
      if (object instanceof MetaJsonField) {
        MetaJsonField jsonField = (MetaJsonField) object;
        validateJsonDataRequiredField(dataRow, cellIndex, line, fileField, jsonField, logService);

        selection = jsonField.getSelection();
        fieldType = fileField.getTargetType();
      } else if (object instanceof Property) {
        Property subProperty = (Property) object;

        if (validatorService.validateDataRequiredField(
            dataRow, cellIndex, line, subProperty.getEntity(), subProperty.getName(), fileField)) {

          return;
        }

        selection = subProperty.getSelection();
        fieldType = subProperty.getJavaType().getSimpleName();
      }

      if (!Strings.isNullOrEmpty(selection)
          && fileField.getForSelectUse() != FileFieldRepository.SELECT_USE_VALUES) {
        return;
      }

      if (!Strings.isNullOrEmpty(fieldType)) {
        validatorService.validateDataType(dataRow, cellIndex, line, fieldType, fileField);
      }
    }
  }

  public String getJsonFieldName(FileField fileField) {
    String field =
        Strings.isNullOrEmpty(fileField.getSubImportField())
            ? fileField.getJsonField().getName()
            : fileField.getJsonField().getName() + "." + fileField.getSubImportField().trim();

    return field;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void createCustomObjectSetForJson(String modelName, MetaJsonModel model, int sequence) {

    String fieldName = Inflector.getInstance().camelize(model.getName(), true) + "Set";
    String viewName = model.getName();

    if (metaJsonFieldRepo
            .all()
            .filter(
                "self.type = ?1 AND self.model = ?2 AND self.targetJsonModel = ?3",
                "json-many-to-many",
                modelName,
                model)
            .count()
        > 0) {
      return;
    }

    MetaJsonField jsonField = new MetaJsonField();
    jsonField.setName(fieldName);
    jsonField.setType("json-many-to-many");
    jsonField.setTitle(Inflector.getInstance().titleize(viewName));
    jsonField.setSequence(sequence);
    jsonField.setModel(modelName);
    jsonField.setModelField("attrs");
    jsonField.setTargetJsonModel(model);
    jsonField.setWidgetAttrs("{\"colSpan\": \"12\"}");
    jsonField.setShowIf(fieldName + " != null && $record.advancedImport.statusSelect > 0");

    metaJsonFieldRepo.save(jsonField);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void createCustomButtonForJson(String modelName, String targetModelName, int sequence) {

    String fieldName = Inflector.getInstance().camelize(targetModelName, true) + "Set";
    String buttonName = "show" + fieldName + "Btn";

    if (metaJsonFieldRepo
            .all()
            .filter(
                "self.name = ?1 AND self.type = ?2 AND self.model = ?3",
                buttonName,
                "button",
                modelName)
            .count()
        > 0) {
      return;
    }

    MetaJsonField jsonField = new MetaJsonField();
    jsonField.setName(buttonName);
    jsonField.setType("button");
    jsonField.setTitle("Show " + Inflector.getInstance().titleize(targetModelName));
    jsonField.setSequence(sequence);
    jsonField.setModel(modelName);
    jsonField.setModelField("attrs");
    jsonField.setOnClick("action-file-tab-method-show-record-json,close");
    jsonField.setWidgetAttrs("{\"colSpan\": \"4\"}");
    jsonField.setShowIf(fieldName + " != null && $record.advancedImport.statusSelect > 0");

    metaJsonFieldRepo.save(jsonField);
  }
}
