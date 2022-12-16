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
package com.axelor.csv.script;

import com.axelor.apps.base.db.FileTab;
import com.axelor.apps.base.db.repo.FileTabRepository;
import com.axelor.apps.base.service.advanced.imports.ActionService;
import com.axelor.apps.base.service.advanced.imports.CustomAdvancedImportService;
import com.axelor.apps.base.service.advanced.imports.CustomValidatorService;
import com.axelor.apps.base.service.advanced.imports.ValidatorService;
import com.axelor.common.Inflector;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaJsonRecordRepository;
import com.axelor.rpc.Context;
import com.axelor.rpc.JsonContext;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptBindings;
import com.axelor.script.ScriptHelper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class ImportAdvancedImport {

  @Inject protected MetaFiles metaFiles;

  @Inject private FileTabRepository fileTabRepo;

  @Inject private MetaJsonRecordRepository metaJsonRecordRepo;

  @Inject private ValidatorService validatorService;

  @Inject private CustomValidatorService customValidatorService;

  @Inject protected ActionService actionService;

  @Inject private MetaJsonFieldRepository metaJsonFieldRepo;

  @SuppressWarnings("unchecked")
  public Object importGeneral(Object bean, Map<String, Object> values)
      throws ClassNotFoundException, JSONException {
    if (bean == null) {
      return bean;
    }

    FileTab fileTab = fileTabRepo.find(Long.valueOf(values.get("fileTabId").toString()));

    ScriptHelper scriptHelper = new GroovyScriptHelper(new ScriptBindings(values));

    List<String> exprs = (List<String>) values.get("ifConditions" + fileTab.getId());
    if (!CollectionUtils.isEmpty(exprs)) {
      if ((boolean) scriptHelper.eval(String.join(" || ", exprs))) {
        return null;
      }
    }

    if (((Model) bean).getId() == null) {
      prepareRealObject(bean, values, fileTab);
    }

    if (((Model) bean).getClass().getSimpleName().equals("MetaJsonRecord")
        && ((Model) bean).getVersion() == 0) {

      prepareCustomObject(bean, values, fileTab);
    }

    final String ACTIONS_TO_APPLY = "actionsToApply" + fileTab.getId();
    if (!ObjectUtils.isEmpty(values.get(ACTIONS_TO_APPLY))) {
      bean = actionService.apply(values.get(ACTIONS_TO_APPLY).toString(), bean);
    }
    return bean;
  }

  private void prepareRealObject(Object bean, Map<String, Object> values, FileTab fileTab)
      throws JSONException {
    List<Property> propList = this.getProperties(bean);
    Mapper mapper = Mapper.of(bean.getClass());
    Property property = mapper.getProperty("attrs");
    String jsonString = (String) property.get(bean);
    JSONObject jsonObject = new JSONObject(jsonString);
    List<MetaJsonField> jsonFields = new ArrayList<>();
    if (!jsonObject.isEmpty()) {
      jsonFields = this.getRealModelJsonFields(bean.getClass().getCanonicalName(), jsonObject);
    }
    JPA.save((Model) bean);
    this.addJsonObjectRecord(bean, fileTab, fileTab.getMetaModel().getName(), values);

    int fieldSeq = 2;
    int btnSeq = 3;
    for (Property prop : propList) {
      validatorService.createCustomObjectSet(
          fileTab.getClass().getName(), prop.getTarget().getName(), fieldSeq);
      validatorService.createCustomButton(
          fileTab.getClass().getName(), prop.getTarget().getName(), btnSeq);

      this.addJsonObjectRecord(
          prop.get(bean),
          fileTab,
          StringUtils.substringAfterLast(prop.getTarget().getName(), "."),
          values);
      fieldSeq++;
      btnSeq++;
    }

    for (MetaJsonField field : jsonFields) {
      jsonObject = (JSONObject) jsonObject.get(field.getName());
      long id = jsonObject.getLong("id");
      MetaJsonRecord record = new MetaJsonRecord();
      record.setId(id);

      customValidatorService.createCustomObjectSetForJson(
          fileTab.getClass().getName(), field.getTargetJsonModel(), fieldSeq);
      customValidatorService.createCustomButtonForJson(
          fileTab.getClass().getName(), field.getTargetJsonModel().getName(), btnSeq);
      this.addJsonObjectRecord(
          (Object) record, fileTab, field.getTargetJsonModel().getName(), values);

      fieldSeq++;
      btnSeq++;
    }
  }

  private void prepareCustomObject(Object bean, Map<String, Object> values, FileTab fileTab)
      throws JSONException {
    List<MetaJsonField> jsonFields =
        this.getJsonFields((MetaJsonRecord) bean, fileTab.getJsonModel());
    this.addJsonObjectRecord(bean, fileTab, ((MetaJsonRecord) bean).getJsonModel(), values);

    int fieldSeq = 2;
    int btnSeq = 3;
    for (MetaJsonField field : jsonFields) {
      MetaJsonRecord record = (MetaJsonRecord) bean;
      JSONObject jsonObject = new JSONObject(record.getAttrs());
      jsonObject = (JSONObject) jsonObject.get(field.getName());
      record = new MetaJsonRecord();
      record.setId(jsonObject.getLong("id"));

      if (Strings.isNullOrEmpty(field.getTargetModel())) {
        customValidatorService.createCustomObjectSetForJson(
            fileTab.getClass().getName(), field.getTargetJsonModel(), fieldSeq);
        customValidatorService.createCustomButtonForJson(
            fileTab.getClass().getName(), field.getTargetJsonModel().getName(), btnSeq);
        this.addJsonObjectRecord(
            (Object) record, fileTab, field.getTargetJsonModel().getName(), values);
      } else {
        validatorService.createCustomObjectSet(
            fileTab.getClass().getName(), field.getTargetModel(), fieldSeq);
        validatorService.createCustomButton(
            fileTab.getClass().getName(), field.getTargetModel(), btnSeq);
        this.addJsonObjectRecord(
            (Object) record,
            fileTab,
            StringUtils.substringAfterLast(field.getTargetModel(), "."),
            values);
      }

      fieldSeq++;
      btnSeq++;
    }
  }

  private List<Property> getProperties(Object bean) {

    List<Property> propList = new ArrayList<Property>();

    for (Property prop : Mapper.of(bean.getClass()).getProperties()) {
      if (prop.getTarget() != null
          && !prop.isCollection()
          && ((Model) prop.get(bean) != null)
          && ((Model) prop.get(bean)).getId() == null) {
        propList.add(prop);
      }
    }
    return propList;
  }

  private List<MetaJsonField> getJsonFields(MetaJsonRecord record, MetaJsonModel jsonModel)
      throws JSONException {
    List<MetaJsonField> jsonFields = new ArrayList<>();
    for (MetaJsonField field : jsonModel.getFields()) {
      if (record != null && !field.getType().endsWith("-many")) {
        if (field.getTargetJsonModel() != null && !isAlreadyStored(record, field.getName())) {
          jsonFields.add(field);
        }
      }
    }
    return jsonFields;
  }

  private boolean isAlreadyStored(MetaJsonRecord record, String field) throws JSONException {
    JSONObject jsonObject = new JSONObject(record.getAttrs());
    if (!jsonObject.isEmpty() && !jsonObject.isNull(field)) {
      jsonObject = (JSONObject) jsonObject.get(field);
      record = metaJsonRecordRepo.find(jsonObject.getLong("id"));
      if (record != null && record.getVersion() == 0) {
        return false;
      }
    }
    return true;
  }

  private List<MetaJsonField> getRealModelJsonFields(String model, JSONObject jsonObject)
      throws JSONException {
    List<MetaJsonField> jsonFields = new ArrayList<>();
    if (!jsonObject.isEmpty()) {
      for (MetaJsonField jsonField :
          metaJsonFieldRepo.all().filter("self.model = ?1", model).fetch().stream()
              .filter(
                  field ->
                      CustomAdvancedImportService.relationTypeList.contains(field.getType())
                          && !field.getType().endsWith("-many"))
              .collect(Collectors.toList())) {
        if (jsonField.getTargetJsonModel() != null && !jsonObject.isNull(jsonField.getName())) {
          jsonObject = (JSONObject) jsonObject.get(jsonField.getName());
          MetaJsonRecord record = metaJsonRecordRepo.find(jsonObject.getLong("id"));
          if (record != null
              && record.getVersion() == 0
              && record.getCreatedOn().isAfter(LocalDateTime.now().minusSeconds(5))) {
            jsonFields.add(jsonField);
          }
        }
      }
    }
    return jsonFields;
  }

  @SuppressWarnings("unchecked")
  private void addJsonObjectRecord(
      Object bean, FileTab fileTab, String fieldName, Map<String, Object> values) {

    String field = Inflector.getInstance().camelize(fieldName, true) + "Set";
    List<Object> recordList;

    Map<String, Object> recordMap = new HashMap<String, Object>();
    recordMap.put("id", ((Model) bean).getId());

    Map<String, Object> jsonContextValues =
        (Map<String, Object>) values.get("jsonContextValues" + fileTab.getId());

    JsonContext jsonContext = (JsonContext) jsonContextValues.get("jsonContext");
    Context context = (Context) jsonContextValues.get("context");

    if (!jsonContext.containsKey(field)) {
      recordList = new ArrayList<Object>();
    } else {
      recordList =
          ((List<Object>) jsonContext.get(field))
              .stream()
                  .map(
                      obj -> {
                        if (Mapper.toMap(EntityHelper.getEntity(obj)).get("id") != null) {
                          Map<String, Object> idMap = new HashMap<String, Object>();
                          idMap.put("id", Mapper.toMap(EntityHelper.getEntity(obj)).get("id"));
                          return idMap;
                        }
                        return obj;
                      })
                  .collect(Collectors.toList());
    }
    recordList.add(recordMap);
    jsonContext.put(field, recordList);

    fileTab.setAttrs(context.get("attrs").toString());
  }

  public Object importPicture(String value, String pathVal) throws IOException {
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }

    Path path = Paths.get(pathVal);
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }

    File image = path.resolve(value).toFile();
    if (!image.exists() || image.isDirectory()) {
      return null;
    }

    MetaFile metaFile = metaFiles.upload(image);
    return metaFile;
  }
}
