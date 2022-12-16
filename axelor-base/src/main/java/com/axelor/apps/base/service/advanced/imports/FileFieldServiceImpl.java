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
import com.axelor.apps.base.service.advancedExport.AdvancedExportService;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class FileFieldServiceImpl implements FileFieldService {

  @Inject private MetaModelRepository metaModelRepo;

  @Inject MetaFieldRepository metaFieldRepo;

  @Inject private CustomAdvancedImportService customAdvancedImportService;

  @Inject ValidatorService validatorService;

  @Override
  public FileField fillType(FileField fileField) {
    String targetType = null;
    String relationship = null;
    String[] types = new String[2];

    if (fileField.getImportField() != null) {
      targetType = fileField.getImportField().getTypeName();
      relationship = fileField.getImportField().getRelationship();

      if (!Strings.isNullOrEmpty(fileField.getSubImportField())) {
        String[] subFields = fileField.getSubImportField().split("\\.");

        if (AdvancedExportService.FIELD_ATTRS.equals(fileField.getImportField().getName())) {
          types =
              this.getAttrsSubFieldType(
                  subFields, 0, fileField.getImportField().getMetaModel().getFullName(), types);
        } else {
          types = this.getSubFieldType(subFields, 0, fileField.getImportField());
        }
        targetType = types[0];
        relationship = types[1];
      }
    } else if (fileField.getJsonField() != null) {
      targetType = getDataType(fileField.getJsonField().getType());
      relationship = null;

      if (!Strings.isNullOrEmpty(fileField.getSubImportField())) {
        String[] subFields = fileField.getSubImportField().split("\\.");
        types = this.getSubJsonFieldtype(subFields, 0, fileField.getJsonField());
        targetType = types[0];
        relationship = types[1];
      }
    }
    fileField.setTargetType(targetType);
    fileField.setRelationship(relationship);
    return fileField;
  }

  private String[] getSubFieldType(String[] subFields, int index, MetaField parentField) {
    String[] types = new String[2];

    if (index < subFields.length) {
      types[0] = parentField.getTypeName();
      types[1] = parentField.getRelationship();
      MetaModel childModel = metaModelRepo.findByName(parentField.getTypeName());
      if (childModel != null) {
        MetaField childField = metaFieldRepo.findByModel(subFields[index], childModel);

        if (childField == null) {
          return types;
        }

        index += 1;
        if (childField.getRelationship() != null) {
          if (!types[0].equals("MetaFile")) {
            types[0] = childField.getTypeName();
          }
          types[1] = childField.getRelationship();
          types = this.getSubFieldType(subFields, index, childField);

        } else if (AdvancedExportService.FIELD_ATTRS.equals(childField.getName())
            && subFields.length > index) {
          types = this.getAttrsSubFieldType(subFields, index, childModel.getFullName(), types);
        } else {
          if (!types[0].equals("MetaFile")) {
            types[0] = childField.getTypeName();
          }
        }
      }
    } else {
      if ((!Strings.isNullOrEmpty(types[0]) && !types[0].equals("MetaFile"))
          || Strings.isNullOrEmpty(types[0])) {
        types[0] = parentField.getTypeName();
      }
      types[1] = parentField.getRelationship();
    }
    return types;
  }

  private String[] getSubJsonFieldtype(String[] subFields, int index, MetaJsonField jsonField) {
    String[] types = new String[2];

    if (index < subFields.length) {
      types[0] = getDataType(jsonField.getType());
      types[1] = jsonField.getType();

      if (jsonField.getTargetJsonModel() != null) {
        jsonField =
            customAdvancedImportService.getJsonField(
                subFields[index],
                null,
                AdvancedExportService.META_JSON_RECORD_FULL_NAME.concat(
                    " " + jsonField.getTargetJsonModel().getName()),
                null);

        if (jsonField != null) {
          if (CustomAdvancedImportService.relationTypeList.contains(jsonField.getType())) {
            types[1] = jsonField.getType();
            types = getSubJsonFieldtype(subFields, index + 1, jsonField);
          } else {
            types[0] = getDataType(jsonField.getType());
          }
        }
      } else if (!Strings.isNullOrEmpty(jsonField.getTargetModel())) {
        MetaField childField =
            metaFieldRepo
                .all()
                .filter(
                    "self.name = ?1 AND self.metaModel.fullName = ?2",
                    subFields[index],
                    jsonField.getTargetModel())
                .fetchOne();

        if (childField == null) {
          return types;
        }

        if (childField.getRelationship() != null) {
          if (!types[0].equals("MetaFile")) {
            types[0] = childField.getTypeName();
          }
          types[1] = childField.getRelationship();
          types = this.getSubFieldType(subFields, index + 1, childField);

        } else if (AdvancedExportService.FIELD_ATTRS.equals(childField.getName())
            && subFields.length > index + 1) {
          types =
              this.getAttrsSubFieldType(subFields, index + 1, jsonField.getTargetModel(), types);
        } else {
          if (!types[0].equals("MetaFile")) {
            types[0] = childField.getTypeName();
          }
        }
      }
    } else {
      types[0] = getDataType(jsonField.getType());
    }

    return types;
  }

  private String[] getAttrsSubFieldType(
      String[] subFields, int index, String modelFullName, String[] types) {
    MetaJsonField jsonField =
        customAdvancedImportService.getJsonField(subFields[index], modelFullName, null, null);

    if (jsonField != null) {
      if (CustomAdvancedImportService.relationTypeList.contains(jsonField.getType())) {
        types[1] = jsonField.getType();
        types = getSubJsonFieldtype(subFields, index + 1, jsonField);
      } else {
        types[0] = getDataType(jsonField.getType());
      }
    }
    return types;
  }

  private String getDataType(String type) {
    switch (type) {
      case "date":
        return ValidatorService.LOCAL_DATE;
      case "time":
        return ValidatorService.LOCAL_TIME;
      case "datetime":
        return ValidatorService.LOCAL_DATE_TIME;
      case "integer":
        return ValidatorService.INTEGER;
      case "long":
        return ValidatorService.LONG;
      case "boolean":
        return ValidatorService.BOOLEAN;
      case "decimal":
        return ValidatorService.BIG_DECIMAL;
      case "string":
        return ValidatorService.STRING;
      default:
        return "";
    }
  }

  @Override
  public String computeFullName(FileField fileField) {
    String fullName = fileField.getSequence().toString();

    if (!Strings.isNullOrEmpty(fileField.getColumnTitle())) {
      fullName += " - " + fileField.getColumnTitle();
    }
    if (fileField.getImportField() != null) {
      fullName += " - " + fileField.getImportField().getName();
    }
    if (fileField.getJsonField() != null) {
      fullName += " - " + fileField.getJsonField().getName();
    }
    if (!Strings.isNullOrEmpty(fileField.getSubImportField())) {
      fullName += "." + fileField.getSubImportField();
    }
    return fullName;
  }
}
