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
package com.axelor.studio.service;

import com.axelor.auth.db.AuditableModel;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.rpc.Context;
import com.axelor.rpc.JsonContext;
import com.axelor.studio.db.DataForm;
import com.axelor.studio.db.DataFormLine;
import com.axelor.studio.service.builder.HtmlFormBuilderService;
import com.axelor.studio.variables.DataFormVariables;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

public class DataFormJsonModelServiceImpl implements DataFormJsonModelService {

  protected MetaJsonFieldRepository metaJsonFieldRepository;

  protected DataFormService dataFormService;

  @Inject
  public DataFormJsonModelServiceImpl(
      MetaJsonFieldRepository metaJsonFieldRepository, DataFormService dataFormService) {
    this.metaJsonFieldRepository = metaJsonFieldRepository;
    this.dataFormService = dataFormService;
  }

  @Override
  public void generateHtmlFormForMetaJsonModel(
      HtmlFormBuilderService htmlFormBuilder, List<DataFormLine> dataFormLineList)
      throws ClassNotFoundException, AxelorException, JsonProcessingException {

    for (DataFormLine dataFormLine : dataFormLineList) {

      MetaJsonField metaJsonField = dataFormLine.getMetaJsonField();
      String type = metaJsonField.getType();

      if (metaJsonField.getTargetModel() == null && metaJsonField.getTargetJsonModel() == null) {

        htmlFormBuilder.prepareInputHtmlElementMetaJsonModel(
            metaJsonField, metaJsonField.getName());

      } else if (DataFormVariables.VALID_RELATIONSHIP_TYPES_JSON_MODEL.contains(type)) {

        htmlFormBuilder.prepareSelectHtmlElementMetaJsonModel(
            metaJsonField,
            metaJsonField.getName(),
            dataFormLine.getAttrs(),
            dataFormLine.getCanSelect(),
            dataFormLine.getQueryFilter());
      }
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void createCustomField(MetaJsonField metaJsonField) {
    final String json = "json";

    Boolean startsWithJson = metaJsonField.getType().startsWith(json);
    Query<MetaJsonField> query = metaJsonFieldRepository.all();

    String targetModel = metaJsonField.getTargetModel();
    MetaJsonModel targetJsonModel = metaJsonField.getTargetJsonModel();

    if (startsWithJson) {
      query
          .filter("self.model = :dataFormLine AND self.targetJsonModel = :targetJsonModel")
          .bind(DataFormVariables.DATA_FORM_LINE, DataFormLine.class.getCanonicalName())
          .bind("targetJsonModel", targetJsonModel);
    } else {
      query
          .filter("self.model = :dataFormLine AND self.targetModel = :targetModel")
          .bind(DataFormVariables.DATA_FORM_LINE, DataFormLine.class.getCanonicalName())
          .bind("targetModel", targetModel);
    }

    MetaJsonField customField = query.fetchOne();
    if (ObjectUtils.notEmpty(customField)) {
      return;
    }

    String name;
    String showIf;
    customField = new MetaJsonField();
    if (startsWithJson) {
      name = targetJsonModel.getName();
      showIf = String.format(DataFormVariables.SHOW_IF_JSON, name);
      customField.setType(DataFormVariables.JSON_MANY_TO_MANY);
      customField.setTargetJsonModel(targetJsonModel);
    } else {
      name = targetModel.substring(targetModel.lastIndexOf(".") + 1);
      showIf =
          String.format(
              DataFormVariables.SHOW_IF,
              targetModel.substring(0, targetModel.lastIndexOf(".")),
              name,
              targetModel);
      customField.setType(DataFormVariables.MANY_TO_MANY);
      customField.setTargetModel(targetModel);
    }
    customField.setName(name + DataFormVariables.SUFFIX_SET);
    customField.setModel(DataFormLine.class.getCanonicalName());
    customField.setModelField(DataFormVariables.ATTRS);
    customField.setWidget(DataFormVariables.TAG_SELECT);
    customField.setVisibleInGrid(true);
    customField.setShowIf(showIf);
    metaJsonFieldRepository.save(customField);
  }

  @Override
  public void generateFieldsForMetaJsonModel(DataForm dataForm) {

    if (ObjectUtils.notEmpty(dataForm.getDataFormLineList())) {
      dataForm.getDataFormLineList().clear();
    }

    List<MetaJsonField> metaJsonFieldList = dataForm.getMetaJsonModel().getFields();

    if (ObjectUtils.isEmpty(metaJsonFieldList)) {
      return;
    }

    int sequence = 0;

    for (MetaJsonField metaJsonField : metaJsonFieldList) {

      String targetModel = metaJsonField.getTargetModel();
      MetaJsonModel targetJsonModel = metaJsonField.getTargetJsonModel();
      String fieldType = metaJsonField.getType();

      if ((targetModel == null
              && targetJsonModel == null
              && !DataFormVariables.VALID_TYPES.contains(fieldType))
          || (!DataFormVariables.VALID_RELATIONSHIP_TYPES_JSON_MODEL.contains(fieldType)
              && (targetModel != null || targetJsonModel != null))) {
        continue;
      }

      DataFormLine dataFormLine = new DataFormLine();
      dataFormLine.setMetaJsonField(metaJsonField);
      dataFormLine.setSequence(++sequence);
      dataForm.addDataFormLineListItem(dataFormLine);
      if (isSelectionAllowedForJsonField(metaJsonField)) {
        createCustomField(metaJsonField);
        dataFormLine.setCanSelect(true);
        dataFormLine.setQueryFilter(metaJsonField.getDomain());
      }
    }
  }

  protected boolean isSelectionAllowedForJsonField(MetaJsonField metaJsonField) {

    String fieldType = metaJsonField.getType();
    String targetModel = metaJsonField.getTargetModel();
    MetaJsonModel targetJsonModel = metaJsonField.getTargetJsonModel();

    boolean isManyToMany =
        fieldType.equals(DataFormVariables.JSON_MANY_TO_MANY)
            || fieldType.equals(DataFormVariables.MANY_TO_MANY);
    boolean isValidM2O =
        targetJsonModel != null
            || (targetModel != null && !targetModel.equals(MetaFile.class.getCanonicalName()));
    return isManyToMany || (isValidM2O && checkCanSelect(metaJsonField));
  }

  protected Boolean checkCanSelect(MetaJsonField metaJsonField) {
    JsonContext jsonContext =
        new JsonContext(
            new Context(MetaJsonField.class),
            Mapper.of(MetaJsonField.class).getProperty(DataFormVariables.WIDGET_ATTRS),
            metaJsonField.getWidgetAttrs());
    return !Boolean.FALSE.toString().equals(jsonContext.get(DataFormVariables.CAN_SELECT));
  }

  @Override
  public <T extends AuditableModel> void createRecordMetaJsonModel(
      Map<String, List<InputPart>> formDataMap, String jsonModelName, final Mapper mapper, T bean)
      throws IOException, ClassNotFoundException, IOException, AxelorException, ServletException {

    Map<String, Object> attrsMap = new HashMap<>();
    Map<String, Map<String, List<InputPart>>> referredEntitiesDetails = new HashMap<>();

    setBeanValuesJsonModel(formDataMap, jsonModelName, attrsMap, referredEntitiesDetails);
    if (ObjectUtils.notEmpty(referredEntitiesDetails)) {
      createReferredEntitiesForJsonModel(referredEntitiesDetails, attrsMap);
    }
    mapper.set(
        bean, DataFormVariables.ATTRS, Beans.get(ObjectMapper.class).writeValueAsString(attrsMap));
    mapper.set(bean, DataFormVariables.JSON_MODEL, jsonModelName);
  }

  public <T extends AuditableModel> void createReferredEntitiesForJsonModel(
      Map<String, Map<String, List<InputPart>>> referredEntitiesDetails,
      Map<String, Object> attrsMap)
      throws ClassNotFoundException, IOException, AxelorException, ServletException {
    for (Entry<String, Map<String, List<InputPart>>> referredEntityEntry :
        referredEntitiesDetails.entrySet()) {

      String modelName =
          referredEntityEntry
              .getKey()
              .substring(
                  0,
                  referredEntityEntry
                      .getKey()
                      .indexOf(DataFormVariables.REFERRED_ENTITY_FIELD_IDENTIFER));
      Class<?> klass;
      Boolean custom;
      if (modelName.contains(".")) {
        klass = Class.forName(modelName);
        custom = false;
      } else {
        klass = Class.forName(MetaJsonRecord.class.getCanonicalName());
        custom = true;
      }
      final Mapper mapper = Mapper.of(klass);
      T bean =
          dataFormService.createRecord(referredEntityEntry.getValue(), klass, custom, modelName);
      int startpos =
          referredEntityEntry.getKey().indexOf(DataFormVariables.REFERRED_ENTITY_FIELD_IDENTIFER)
              + 2;
      String mainEntityFieldName = referredEntityEntry.getKey().substring(startpos);
      Map<String, Object> map = new HashMap<>();
      Long id = bean.getId();
      map.put(DataFormVariables.ID, id);
      if (ObjectUtils.notEmpty(mapper.getNameField())) {
        String nameField = mapper.getNameField().getName();
        map.put(
            nameField,
            mapper.get(bean, nameField) == null ? bean.getId() : mapper.get(bean, nameField));
      }
      attrsMap.put(mainEntityFieldName, map);
    }
  }

  @SuppressWarnings("unchecked")
  protected void setBeanValuesJsonModel(
      Map<String, List<InputPart>> formDataMap,
      String jsonModelName,
      Map<String, Object> attrsMap,
      Map<String, Map<String, List<InputPart>>> otherEntityMap)
      throws IOException, ClassNotFoundException, NumberFormatException, ServletException {
    final String bool = "boolean";
    final String type = "type";
    final String string = "string";
    final String datetime = "datetime";
    final String time = "time";
    final String target = "target";

    Map<String, Object> fields = MetaStore.findJsonFields(jsonModelName);

    for (Entry<String, List<InputPart>> entry : formDataMap.entrySet()) {
      String fieldName = entry.getKey();
      List<InputPart> values = entry.getValue();
      InputPart value = values.get(0);
      Object valueToSet = value.getBodyAsString();
      if (ObjectUtils.isEmpty(valueToSet)) {
        continue;
      } else if (fieldName.startsWith(DataFormVariables.REFERRED_ENTITY_IDENTIFIER)) {
        String referredEntityName =
            fieldName.substring(
                1, fieldName.lastIndexOf(DataFormVariables.REFERRED_ENTITY_FIELD_IDENTIFER));
        Map<String, List<InputPart>> map =
            otherEntityMap.computeIfAbsent(referredEntityName, m -> new HashMap<>());
        String referredEntityField =
            fieldName.substring(
                fieldName.lastIndexOf(DataFormVariables.REFERRED_ENTITY_FIELD_IDENTIFER) + 2);
        map.put(referredEntityField, values);
        continue;
      }
      Map<String, Object> fieldMap = (Map<String, Object>) fields.get(entry.getKey());
      String fieldType = (String) fieldMap.get(type);
      if (fieldType.equalsIgnoreCase(string)
          || fieldType.equalsIgnoreCase(datetime)
          || fieldType.equalsIgnoreCase(time)) {
        valueToSet = URLDecoder.decode(value.getBodyAsString(), StandardCharsets.UTF_8.name());
      }
      if (fieldType.equalsIgnoreCase(bool)) {
        valueToSet = true;
      }
      if (fieldType.equalsIgnoreCase(DataFormVariables.MANY_TO_ONE)
          || fieldType.equalsIgnoreCase(DataFormVariables.JSON_MANY_TO_ONE)) {
        valueToSet = processM2OJsonField(target, value, fieldMap);
      }
      if (fieldType.equalsIgnoreCase(DataFormVariables.MANY_TO_MANY)
          || fieldType.equalsIgnoreCase(DataFormVariables.JSON_MANY_TO_MANY)) {
        valueToSet =
            values.stream()
                .map(dataFormService::getIdFromInputPart)
                .filter(Objects::nonNull)
                .map(id -> ImmutableMap.of(DataFormVariables.ID, id))
                .collect(Collectors.toList());
      }
      attrsMap.put(fieldName, valueToSet);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  protected Object processM2OJsonField(
      final String target, InputPart value, Map<String, Object> fieldMap)
      throws ClassNotFoundException, IOException {
    Class targetClass = Class.forName((String) fieldMap.get(target));
    Map<String, Object> valueToSet = new HashMap<>();
    Long id =
        targetClass == MetaFile.class
            ? dataFormService.processFileUpload(value).getId()
            : Long.parseLong(value.getBodyAsString());
    valueToSet.put(DataFormVariables.ID, id);
    Mapper targetMapper = Mapper.of(targetClass);
    if (ObjectUtils.notEmpty(targetMapper.getNameField())) {
      String nameField = targetMapper.getNameField().getName();
      Model item = JpaRepository.of(targetClass).find(id);
      valueToSet.put(
          nameField,
          targetMapper.get(item, nameField) == null
              ? value.getBodyAsString()
              : targetMapper.get(item, nameField));
    }
    return valueToSet;
  }
}
