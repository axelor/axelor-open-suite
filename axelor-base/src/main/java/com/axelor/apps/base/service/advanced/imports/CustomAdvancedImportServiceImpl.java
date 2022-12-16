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
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.advancedExport.AdvancedExportService;
import com.axelor.common.Inflector;
import com.axelor.db.EntityHelper;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.rpc.JsonContext;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomAdvancedImportServiceImpl implements CustomAdvancedImportService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private MetaJsonFieldRepository metaJsonFieldRepo;

  @Inject private AdvancedImportService advancedImportService;
  @Inject private FileFieldService fileFieldService;
  @Inject private DataImportService dataImportService;

  private Inflector inflector = Inflector.getInstance();

  @Override
  public MetaJsonField getJsonField(
      String fieldName, String model, String uniqueModel, MetaJsonModel jsonModel) {
    MetaJsonField jsonField =
        metaJsonFieldRepo
            .all()
            .filter(
                "self.name = ?1 AND (self.model = ?2 OR self.uniqueModel = ?3 OR self.jsonModel = ?4)",
                fieldName,
                model,
                uniqueModel,
                jsonModel)
            .fetchOne();

    return jsonField;
  }

  @Override
  public boolean checkJsonField(FileTab fileTab, String importField, String subImportField)
      throws AxelorException, ClassNotFoundException {
    MetaJsonField metaJsonField =
        this.getJsonField(importField, null, null, fileTab.getJsonModel());

    if (metaJsonField == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_1),
              importField,
              fileTab.getJsonModel().getName()));
    }

    String[] subFields =
        Strings.isNullOrEmpty(subImportField) ? new String[0] : subImportField.split("\\.");
    if (relationTypeList.contains(metaJsonField.getType())) {
      if (metaJsonField.getType().equals("one-to-many")
          || metaJsonField.getType().equals("json-one-to-many")) {
        return false;
      }

      if (0 < subFields.length) {
        return this.chekckJsonSubField(
            subFields, 0, metaJsonField, importField, fileTab.getJsonModel().getName());
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_5),
                importField,
                fileTab.getJsonModel().getName()));
      }
    } else if (0 < subFields.length) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_2),
              subFields[0],
              importField,
              fileTab.getJsonModel().getName()));
    }

    return true;
  }

  private boolean chekckJsonSubField(
      String[] subFields, int index, MetaJsonField metaJsonField, String importField, String model)
      throws AxelorException, ClassNotFoundException {

    if (metaJsonField.getTargetJsonModel() != null) {
      return this.checkSubCustomField(
          subFields, index, metaJsonField.getTargetJsonModel(), importField, model);

    } else if (!Strings.isNullOrEmpty(metaJsonField.getTargetModel())) {
      return this.checkSubRealField(
          subFields, index, metaJsonField.getTargetModel(), importField, model);
    }

    return true;
  }

  private boolean checkSubCustomField(
      String[] subFields, int index, MetaJsonModel jsonModel, String importField, String model)
      throws AxelorException, ClassNotFoundException {
    String fieldName = subFields[index];
    MetaJsonField metaJsonField = this.getJsonField(fieldName, null, null, jsonModel);

    if (metaJsonField == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_2), fieldName, importField, model));
    }

    model = metaJsonField.getJsonModel().getName();
    importField = metaJsonField.getName();
    index += 1;
    if (relationTypeList.contains(metaJsonField.getType())) {
      if (metaJsonField.getType().equals("one-to-many")
          || metaJsonField.getType().equals("json-one-to-many")) {
        return false;
      }

      if (index < subFields.length) {
        return this.chekckJsonSubField(subFields, index, metaJsonField, importField, model);
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_5), fieldName, model));
      }
    } else if (index < subFields.length) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_2),
              subFields[index],
              fieldName,
              model));
    }
    return true;
  }

  private boolean checkSubRealField(
      String[] subFields, int index, String targetModel, String importField, String model)
      throws ClassNotFoundException, AxelorException {
    Mapper mapper = advancedImportService.getMapper(targetModel);
    Property childProp = mapper.getProperty(subFields[index]);

    if (childProp == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_2),
              subFields[index],
              importField,
              model));
    }

    model = childProp.getEntity().getName();
    index += 1;
    if (childProp.getTarget() != null) {
      if (childProp.getType().name().equals("ONE_TO_MANY")) {
        return false;
      }

      if (index < subFields.length) {
        return advancedImportService.checkSubFields(subFields, index, childProp, model);
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_5), subFields[index], model));
      }
    } else {
      if (AdvancedExportService.FIELD_ATTRS.equals(childProp.getName())) {
        checkAttrsSubField(
            subFields,
            index,
            mapper.getBeanClass().getCanonicalName(),
            childProp.getName(),
            mapper.getBeanClass().getSimpleName());
      } else if (index < subFields.length) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_2),
                subFields[index],
                childProp.getName(),
                model));
      }
    }
    return true;
  }

  @Override
  public boolean checkAttrsSubField(
      String[] subFields, int index, String uniqueModel, String importField, String model)
      throws AxelorException, ClassNotFoundException {

    if (subFields.length <= index) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_2),
              subFields[index - 1],
              importField,
              model));
    }

    MetaJsonField jsonField = this.getJsonField(subFields[index], null, uniqueModel, null);
    if (jsonField == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_2),
              subFields[index],
              importField,
              model));
    }

    importField = jsonField.getName();
    index += 1;
    if (relationTypeList.contains(jsonField.getType())) {
      if (jsonField.getType().equals("one-to-many")
          || jsonField.getType().equals("json-one-to-many")) {
        return false;
      }

      if (index < subFields.length) {
        return this.chekckJsonSubField(subFields, index, jsonField, importField, model);
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_5), importField, model));
      }
    } else if (index < subFields.length) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_2),
              subFields[index],
              importField,
              model));
    }

    return true;
  }

  @Override
  public void setJsonFields(
      FileTab fileTab, FileField fileField, String importField, String subImportField) {
    MetaJsonField field = this.getJsonField(importField, null, null, fileTab.getJsonModel());

    fileField.setJsonField(field);
    fileField.setIsMatchWithFile(true);

    if (!Strings.isNullOrEmpty(subImportField)) {
      fileField.setSubImportField(subImportField);
    }

    fileField.setFullName(fileFieldService.computeFullName(fileField));
    fileField = fileFieldService.fillType(fileField);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void removeJsonRecords(List<FileTab> fileTabList) throws ClassNotFoundException {

    for (FileTab fileTab : fileTabList) {

      Map<String, Object> jsonContextMap = dataImportService.createJsonContext(fileTab);
      JsonContext jsonContext = (JsonContext) jsonContextMap.get("jsonContext");
      String fieldName = inflector.camelize(fileTab.getJsonModel().getName(), true) + "Set";

      List<Object> recordList = (List<Object>) jsonContext.get(fieldName);
      if (CollectionUtils.isEmpty(recordList)) {
        continue;
      }

      Class<? extends Model> modelKlass =
          (Class<? extends Model>) Class.forName(AdvancedExportService.META_JSON_RECORD_FULL_NAME);
      removeJsonRecord(fileTab, modelKlass, recordList, fileTabList);
      removeJsonSubRecords(modelKlass, fileTab.getJsonModel().getFields(), jsonContext);
    }
  }

  @SuppressWarnings("unchecked")
  @Transactional
  public void removeJsonRecord(
      FileTab fileTab,
      Class<? extends Model> modelKlass,
      List<Object> recordList,
      List<FileTab> fileTabList)
      throws ClassNotFoundException {

    JpaRepository<? extends Model> modelRepo = JpaRepository.of(modelKlass);

    for (FileTab tab : fileTabList) {

      Map<String, Object> jsonContextMap = dataImportService.createJsonContext(tab);
      JsonContext jsonContext = (JsonContext) jsonContextMap.get("jsonContext");
      MetaJsonModel jsonModel = tab.getJsonModel();
      String fieldName = inflector.camelize(jsonModel.getName(), true) + "Set";

      List<Object> recList = (List<Object>) jsonContext.get(fieldName);
      if (CollectionUtils.isEmpty(recList)) {
        continue;
      }

      for (MetaJsonField jsonField : tab.getJsonModel().getFields()) {
        if (jsonField.getTargetJsonModel() != null
            && jsonField.getTargetJsonModel().equals(jsonModel)
            && jsonField.getRequired()) {
          removeJsonRecord(tab, modelKlass, recList, fileTabList);
        }
      }
    }

    String ids =
        recordList.stream()
            .map(
                obj -> {
                  Map<String, Object> recordMap = Mapper.toMap(EntityHelper.getEntity(obj));
                  return recordMap.get("id").toString();
                })
            .collect(Collectors.joining(","));

    modelRepo.all().filter("self.id IN (" + ids + ")").delete();
    fileTab.setAttrs(null);

    LOG.debug("Reset imported data : {}", modelKlass.getSimpleName());
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional
  public void removeJsonSubRecords(
      Class<? extends Model> klass, List<MetaJsonField> jsonFields, JsonContext jsonContext)
      throws ClassNotFoundException {

    for (MetaJsonField jsonField : jsonFields) {
      if ((jsonField.getTargetJsonModel() == null
              && Strings.isNullOrEmpty(jsonField.getTargetModel()))
          || jsonField.getType().endsWith("-many")) {
        continue;
      }

      String simpleModelName =
          Strings.isNullOrEmpty(jsonField.getTargetModel())
              ? jsonField.getTargetJsonModel().getName()
              : StringUtils.substringAfterLast(jsonField.getTargetModel(), ".");
      String field = inflector.camelize(simpleModelName, true) + "Set";

      if (!jsonContext.containsKey(field)) {
        continue;
      }

      List<Object> recList = (List<Object>) jsonContext.get(field);

      String ids =
          recList.stream()
              .map(
                  obj -> {
                    Map<String, Object> recordMap = (Map<String, Object>) obj;
                    return recordMap.get("id").toString();
                  })
              .collect(Collectors.joining(","));

      JpaRepository<? extends Model> modelRepo =
          JpaRepository.of(
              (Class<? extends Model>)
                  Class.forName(
                      jsonField.getTargetJsonModel() == null
                          ? jsonField.getTargetModel()
                          : klass.getName()));

      modelRepo.all().filter("self.id IN (" + ids + ")").delete();
    }
  }
}
