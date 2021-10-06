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
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.db.mapper.PropertyType;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Field;
import com.axelor.meta.schema.views.FormView;
import com.axelor.meta.schema.views.Panel;
import com.axelor.meta.schema.views.PanelField;
import com.axelor.meta.schema.views.PanelInclude;
import com.axelor.meta.schema.views.PanelRelated;
import com.axelor.meta.schema.views.PanelStack;
import com.axelor.meta.schema.views.PanelTabs;
import com.axelor.studio.db.DataForm;
import com.axelor.studio.db.DataFormLine;
import com.axelor.studio.exception.IExceptionMessage;
import com.axelor.studio.service.builder.HtmlFormBuilderService;
import com.axelor.studio.variables.DataFormVariables;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

public class DataFormMetaModelServiceImpl implements DataFormMetaModelService {

  protected MetaFieldRepository metaFieldRepository;

  protected MetaJsonFieldRepository metaJsonFieldRepository;

  protected DataFormService dataFormService;

  @Inject
  public DataFormMetaModelServiceImpl(
      MetaFieldRepository metaFieldRepository,
      MetaJsonFieldRepository metaJsonFieldRepository,
      DataFormService dataFormService) {
    this.metaFieldRepository = metaFieldRepository;
    this.metaJsonFieldRepository = metaJsonFieldRepository;
    this.dataFormService = dataFormService;
  }

  @Override
  public void generateHtmlFormForMetaModel(
      HtmlFormBuilderService htmlFormBuilder, List<DataFormLine> dataFormLineList)
      throws ClassNotFoundException, AxelorException, JsonProcessingException {

    for (DataFormLine dataFormLine : dataFormLineList) {

      MetaField metaField = dataFormLine.getMetaField();
      String relationship = metaField.getRelationship();

      if (relationship == null) {

        htmlFormBuilder.prepareInputHtmlElementMetaField(
            metaField, dataFormLine.getAttrs(), metaField.getName());

      } else if (DataFormVariables.VALID_RELATIONSHIP_TYPES_META_MODEL.contains(relationship)) {

        htmlFormBuilder.prepareSelectHtmlElementMetaField(
            metaField,
            metaField.getName(),
            dataFormLine.getAttrs(),
            dataFormLine.getCanSelect(),
            dataFormLine.getQueryFilter());
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public String getDomainFilter(DataForm dataForm) {

    String query =
        "WITH self AS\n"
            + "(   SELECT *, ROW_NUMBER() OVER (PARTITION BY name ORDER BY priority DESC) AS rn\n"
            + "    FROM meta_view\n"
            + ")\n"
            + "SELECT id\n"
            + "FROM self\n"
            + "WHERE rn = 1 AND type = :viewType AND model = :model AND (extension IS null OR extension = false)";

    List<BigInteger> idList =
        JPA.em()
            .createNativeQuery(query)
            .setParameter("viewType", "form")
            .setParameter("model", dataForm.getMetaModel().getFullName())
            .getResultList();

    String idListString =
        idList.stream().map(String::valueOf).collect(Collectors.joining("','", "('", "')"));
    return String.format("self.id in %s", idListString);
  }

  @Override
  public void generateFieldsFormView(DataForm dataForm)
      throws JAXBException, JsonProcessingException {

    if (ObjectUtils.notEmpty(dataForm.getDataFormLineList())) {
      dataForm.getDataFormLineList().clear();
    }
    ObjectViews objectView = XMLViews.unmarshal(dataForm.getModelFormView().getXml());
    Map<String, Map<String, Object>> fieldMap = new LinkedHashMap<>();
    for (AbstractView abstractView : objectView.getViews()) {
      if (abstractView instanceof FormView) {
        FormView formView = (FormView) abstractView;
        for (AbstractWidget abstractWidget : formView.getItems()) {
          getFieldName(abstractWidget, fieldMap);
        }
      }
    }

    if (fieldMap.isEmpty()) {
      return;
    }

    List<MetaField> metaFieldList =
        metaFieldRepository
            .all()
            .filter(
                "self.metaModel = :model AND self.name IN :names AND (self.relationship IN :validRelationships OR self.relationship IS null)")
            .bind("model", dataForm.getMetaModel())
            .bind("names", fieldMap.keySet())
            .bind("validRelationships", DataFormVariables.VALID_RELATIONSHIP_TYPES_META_MODEL)
            .fetch();

    if (ObjectUtils.isEmpty(metaFieldList)) {
      return;
    }
    int sequence = 0;
    for (Entry<String, Map<String, Object>> fieldEntry : fieldMap.entrySet()) {
      MetaField metaField =
          metaFieldList.stream()
              .filter(field -> fieldEntry.getKey().equals(field.getName()))
              .findFirst()
              .orElse(null);

      if (metaField == null) {
        continue;
      }
      DataFormLine dataFormLine = new DataFormLine();
      dataFormLine.setMetaField(metaField);
      dataFormLine.setSequence(sequence++);
      dataForm.addDataFormLineListItem(dataFormLine);
      Map<String, Object> attributesMap = fieldEntry.getValue();
      if (isSelectionAllowedForMetaField(metaField, dataFormLine, attributesMap)) {
        createCustomField(metaField);
        dataFormLine.setCanSelect(true);
        if (ObjectUtils.notEmpty(attributesMap.get(DataFormVariables.DOMAIN))) {
          dataFormLine.setQueryFilter(String.valueOf(attributesMap.get(DataFormVariables.DOMAIN)));
        }
      }

      createAdditionalAttributes(dataFormLine, attributesMap);
    }
  }

  protected boolean isSelectionAllowedForMetaField(
      MetaField metaField, DataFormLine dataFormLine, Map<String, Object> attributesMap) {

    boolean isNotMetaFile =
        !dataFormLine.getMetaField().getTypeName().equals(MetaFile.class.getSimpleName());
    boolean canSelect =
        !Boolean.FALSE.toString().equals(attributesMap.get(DataFormVariables.CAN_SELECT));
    return metaField.getRelationship() != null
        && (metaField.getRelationship().equals("ManyToMany") || (isNotMetaFile && canSelect));
  }

  protected void getFieldName(
      AbstractWidget abstractWidget, Map<String, Map<String, Object>> fieldMap) {
    if (abstractWidget instanceof Field) {
      Field field = (Field) abstractWidget;
      Map<String, Object> attributes = new HashMap<>();
      attributes.put(DataFormVariables.REQUIRED, field.getRequired());
      attributes.put(DataFormVariables.DOMAIN, field.getDomain());
      attributes.put(DataFormVariables.CAN_SELECT, field.getCanSelect());
      attributes.put(DataFormVariables.WIDGET, field.getWidget());
      attributes.put(DataFormVariables.PATTERN, field.getPattern());
      attributes.put(DataFormVariables.PLACEHOLDER, field.getPlaceholder());
      if (field.getName().contains(".")) {
        fieldMap.put(StringUtils.substringBefore(field.getName(), "."), attributes);
        attributes.put(DataFormVariables.NAME, StringUtils.substringAfter(field.getName(), "."));
        Map<String, List<Map<String, String>>> panelEditor = new HashMap<>();
        List<Map<String, String>> fields =
            ImmutableList.of(
                ImmutableMap.of(
                    DataFormVariables.NAME, StringUtils.substringAfter(field.getName(), ".")));
        List<Map<String, String>> items = new ArrayList<>();
        Map<String, String> itemMap = new HashMap<>();
        for (Entry<String, Object> entry : attributes.entrySet()) {
          itemMap.put(entry.getKey(), (String) entry.getValue());
        }
        items.add(itemMap);
        panelEditor.put(DataFormVariables.ITEMS, items);
        panelEditor.put(DataFormVariables.FIELDS, fields);
        attributes.put(DataFormVariables.PANEL_EDITOR, panelEditor);
      } else {
        fieldMap.put(field.getName(), attributes);
      }
      if (abstractWidget instanceof PanelField) {
        PanelField panelField = (PanelField) abstractWidget;
        attributes.put(DataFormVariables.CAN_SELECT, panelField.getCanSelect());
        if (panelField.getEditor() != null && panelField.getEditor().getItems() != null) {
          attributes.put(DataFormVariables.PANEL_EDITOR, panelField.getEditor());
          panelField.getEditor().getItems().forEach(item -> getFieldName(item, fieldMap));
        }
      }
    } else if (abstractWidget instanceof PanelRelated) {
      PanelRelated panelRelated = (PanelRelated) abstractWidget;
      Map<String, Object> attributes = new HashMap<>();
      attributes.put(DataFormVariables.REQUIRED, panelRelated.getRequired());
      fieldMap.put(panelRelated.getName(), attributes);
    } else if (abstractWidget instanceof Panel) {
      Panel panel = (Panel) abstractWidget;
      if (panel.getItems() != null) {
        panel.getItems().forEach(item -> getFieldName(item, fieldMap));
      }
    } else if (abstractWidget instanceof PanelStack) {
      PanelStack panelStack = (PanelStack) abstractWidget;
      if (panelStack.getItems() != null) {
        panelStack.getItems().forEach(item -> getFieldName(item, fieldMap));
      }
    } else if (abstractWidget instanceof PanelTabs) {
      PanelTabs panelTabs = (PanelTabs) abstractWidget;
      if (panelTabs.getItems() != null) {
        panelTabs.getItems().forEach(item -> getFieldName(item, fieldMap));
      }
    } else if (abstractWidget instanceof PanelInclude) {
      PanelInclude panelInclude = (PanelInclude) abstractWidget;
      if (panelInclude.getView() instanceof FormView) {
        FormView formView = (FormView) panelInclude.getView();
        for (AbstractWidget absWidget : formView.getItems()) {
          getFieldName(absWidget, fieldMap);
        }
      }
    }
  }

  @Override
  public void generateFieldsMetaModel(DataForm dataForm) {

    if (ObjectUtils.notEmpty(dataForm.getDataFormLineList())) {
      dataForm.getDataFormLineList().clear();
    }

    List<MetaField> metaFieldList =
        metaFieldRepository
            .all()
            .filter(
                "self.metaModel = :model AND self.name NOT IN :names AND (self.relationship IN :validRelationships OR self.relationship IS null)")
            .bind("model", dataForm.getMetaModel())
            .bind("names", getIgnoreFieldList())
            .bind("validRelationships", DataFormVariables.VALID_RELATIONSHIP_TYPES_META_MODEL)
            .fetch();

    int sequence = 0;
    for (MetaField metaField : metaFieldList) {
      DataFormLine dataFormLine = new DataFormLine();
      dataFormLine.setMetaField(metaField);
      dataFormLine.setSequence(sequence++);
      dataForm.addDataFormLineListItem(dataFormLine);
      if (isSelectionAllowedForMetaField(metaField, dataFormLine, ImmutableMap.of())) {
        createCustomField(metaField);
        dataFormLine.setCanSelect(true);
      }
    }
  }

  private List<String> getIgnoreFieldList() {
    List<String> ignoreFieldList = new ArrayList<>();
    for (java.lang.reflect.Field field : AuditableModel.class.getDeclaredFields()) {
      ignoreFieldList.add(field.getName());
    }
    for (java.lang.reflect.Field field : Model.class.getDeclaredFields()) {
      ignoreFieldList.add(field.getName());
    }
    ignoreFieldList.add(DataFormVariables.ID);
    return ignoreFieldList;
  }

  protected void createAdditionalAttributes(
      DataFormLine dataFormLine, Map<String, Object> attributesMap) throws JsonProcessingException {
    if (!Boolean.TRUE.equals(attributesMap.get(DataFormVariables.REQUIRED))) {
      attributesMap.put(DataFormVariables.REQUIRED, false);
    }
    ObjectMapper objectMapper = Beans.get(ObjectMapper.class);
    String attrs = objectMapper.writeValueAsString(attributesMap);
    dataFormLine.setAttrs(attrs);
  }

  @Transactional(rollbackOn = Exception.class)
  protected void createCustomField(MetaField metaField) {

    String targetModel = metaField.getPackageName() + "." + metaField.getTypeName();
    MetaJsonField customField =
        metaJsonFieldRepository
            .all()
            .filter("self.model = :dataFormLine AND self.targetModel = :targetModel")
            .bind(DataFormVariables.DATA_FORM_LINE, DataFormLine.class.getCanonicalName())
            .bind("targetModel", targetModel)
            .fetchOne();
    if (ObjectUtils.notEmpty(customField)) {
      return;
    }

    customField = new MetaJsonField();
    String name = metaField.getTypeName();
    String showIf =
        String.format(
            DataFormVariables.SHOW_IF,
            metaField.getPackageName(),
            metaField.getTypeName(),
            metaField.getPackageName() + "." + name);
    customField.setType(DataFormVariables.MANY_TO_MANY);
    customField.setTargetModel(targetModel);

    customField.setName(name + DataFormVariables.SUFFIX_SET);
    customField.setModel(DataFormLine.class.getCanonicalName());
    customField.setModelField(DataFormVariables.ATTRS);
    customField.setWidget(DataFormVariables.TAG_SELECT);
    customField.setVisibleInGrid(true);
    customField.setShowIf(showIf);
    metaJsonFieldRepository.save(customField);
  }

  @Override
  public <T extends AuditableModel> void createRecordMetaModel(
      Map<String, List<InputPart>> formDataMap, Class<?> klass, final Mapper mapper, T bean)
      throws IOException, ClassNotFoundException, AxelorException, ServletException {

    Map<String, Map<String, List<InputPart>>> referredEntitiesDetails = new HashMap<>();
    setBeanValuesMetaModel(formDataMap, klass, mapper, bean, referredEntitiesDetails);
    if (ObjectUtils.notEmpty(referredEntitiesDetails)) {
      createReferredEntitiesForMetaModel(referredEntitiesDetails, mapper, bean);
    }
  }

  protected <T extends AuditableModel> void createReferredEntitiesForMetaModel(
      Map<String, Map<String, List<InputPart>>> referredEntitiesDetails,
      Mapper mainEntityMapper,
      T mainEntityBean)
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
      Class<?> klass = Class.forName(modelName);
      T bean =
          dataFormService.createRecord(
              referredEntityEntry.getValue(), klass, false, StringUtils.EMPTY);

      int startpos =
          referredEntityEntry.getKey().indexOf(DataFormVariables.REFERRED_ENTITY_FIELD_IDENTIFER)
              + 2;
      String mainEntityFieldName = referredEntityEntry.getKey().substring(startpos);

      mainEntityMapper.set(mainEntityBean, mainEntityFieldName, bean);
    }
  }

  @SuppressWarnings("unchecked")
  protected <T extends AuditableModel> void setBeanValuesMetaModel(
      Map<String, List<InputPart>> formDataMap,
      Class<?> klass,
      final Mapper mapper,
      T bean,
      Map<String, Map<String, List<InputPart>>> referredEntitiesDetails)
      throws IOException, AxelorException {
    for (Entry<String, List<InputPart>> entry : formDataMap.entrySet()) {
      String name = entry.getKey();
      List<InputPart> values = entry.getValue();
      Property property = mapper.getProperty(name);
      InputPart value = values.get(0);
      Object valueToSet = value.getBodyAsString();
      if (ObjectUtils.isEmpty(valueToSet)) {
        continue;
      } else if (name.startsWith(DataFormVariables.REFERRED_ENTITY_IDENTIFIER)) {
        String referredEntityName =
            name.substring(1, name.lastIndexOf(DataFormVariables.REFERRED_ENTITY_FIELD_IDENTIFER));
        Map<String, List<InputPart>> referredEntityMap =
            referredEntitiesDetails.computeIfAbsent(referredEntityName, m -> new HashMap<>());
        String fieldName =
            name.substring(name.lastIndexOf(DataFormVariables.REFERRED_ENTITY_FIELD_IDENTIFER) + 2);
        referredEntityMap.put(fieldName, values);
        continue;
      } else if (property.getType() == PropertyType.BINARY) {
        valueToSet = value.getBody(byte[].class, null);
      } else if (property.getType() == PropertyType.STRING
          || property.getType() == PropertyType.DATETIME
          || property.getType() == PropertyType.TIME) {
        valueToSet = URLDecoder.decode(value.getBodyAsString(), StandardCharsets.UTF_8.name());
      } else if (property.getType() == PropertyType.ONE_TO_ONE) {
        valueToSet = processO2OField(property, value, klass);
      } else if (property.getType() == PropertyType.MANY_TO_ONE) {
        valueToSet = processM2OField(property, value);
      } else if (property.getType() == PropertyType.BOOLEAN) {
        valueToSet = true;
      } else if (property.getType() == PropertyType.MANY_TO_MANY) {
        Class<T> target = (Class<T>) property.getTarget();
        valueToSet =
            values.stream()
                .map(dataFormService::getIdFromInputPart)
                .filter(Objects::nonNull)
                .map(id -> JPA.find(target, id))
                .collect(Collectors.toSet());
      }
      mapper.set(bean, name, valueToSet);
    }
  }

  @SuppressWarnings("unchecked")
  protected <T extends AuditableModel> Object processM2OField(Property property, InputPart value)
      throws IOException {
    Object valueToSet;
    if (property.getTarget() == MetaFile.class) {
      valueToSet = dataFormService.processFileUpload(value);
    } else {
      valueToSet =
          JPA.find((Class<T>) property.getTarget(), Long.parseLong(value.getBodyAsString()));
    }
    return valueToSet;
  }

  @SuppressWarnings("unchecked")
  protected <T extends AuditableModel> Object processO2OField(
      Property property, InputPart value, Class<?> klass) throws IOException, AxelorException {
    Long passedId = Long.parseLong(value.getBodyAsString());
    T model =
        JPA.all((Class<T>) klass)
            .filter(String.format("self.%s.id = :id", property.getName()))
            .bind("id", passedId)
            .fetchOne();
    if (ObjectUtils.notEmpty(model)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_UNIQUE_KEY,
          I18n.get(IExceptionMessage.O2O_ASSOCIATION_EXCEED));
    }
    return JPA.find((Class<T>) property.getTarget(), passedId);
  }
}
