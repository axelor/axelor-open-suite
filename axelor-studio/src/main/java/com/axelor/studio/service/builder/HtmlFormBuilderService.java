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
package com.axelor.studio.service.builder;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.AppBpm;
import com.axelor.apps.base.db.repo.AppBpmRepository;
import com.axelor.common.Inflector;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.db.mapper.PropertyType;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.rpc.Context;
import com.axelor.rpc.JsonContext;
import com.axelor.studio.db.DataForm;
import com.axelor.studio.db.DataFormLine;
import com.axelor.studio.exception.IExceptionMessage;
import com.axelor.studio.variables.DataFormVariables;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.QueryException;

public class HtmlFormBuilderService {

  private StringBuilder htmlForm = new StringBuilder();
  private String code;
  private ResourceBundle translation;
  private AppBpm appBpm;
  private ObjectMapper objectMapper;

  public HtmlFormBuilderService(String code, String language) {
    this.code = code;
    this.translation = I18n.getBundle(new Locale(language));
    this.appBpm = Beans.get(AppBpmRepository.class).all().fetchOne();
    this.objectMapper = Beans.get(ObjectMapper.class);
  }

  @SuppressWarnings("unused")
  private HtmlFormBuilderService() {}

  public String build(DataForm dataForm) {

    String html =
        new BufferedReader(
                new InputStreamReader(
                    getClass().getResourceAsStream(DataFormVariables.TEMPLATE_LOCATION),
                    StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"));

    String captchaBox =
        appBpm.getGenerateCaptcha() ? DataFormVariables.CAPTCHA_BOX_DIV : StringUtils.EMPTY;

    String recordToCreate =
        dataForm.getCustom()
            ? dataForm.getMetaJsonModel().getName()
            : dataForm.getMetaModel().getName();

    String formName = translation.getString(recordToCreate);

    html = html.replace(DataFormVariables.TEMPLATE_FORM_NAME, formName);
    html =
        html.replace(
            DataFormVariables.TEMPLATE_FORM_ACTION,
            AppSettings.get().getBaseURL() + DataFormVariables.WS_PUBLIC_DATA_FORM);
    html = html.replace(DataFormVariables.TEMPLATE_MODEL_CODE, code);
    html = html.replace(DataFormVariables.TEMPLATE_RECORD_TO_CREATE, recordToCreate);
    html = html.replace(DataFormVariables.TEMPLATE_CAPTCHA_BOX, captchaBox);
    html =
        html.replace(
            DataFormVariables.TEMPLATE_CAPTCHA_ATTEMPT,
            translation.getString(DataFormVariables.ATTEMPT_CAPTCHA_MESSAGE));
    html =
        html.replace(
            DataFormVariables.TEMPLATE_CAPTCHA_WRONG,
            translation.getString(DataFormVariables.WRONG_CAPTCHA_MESSAGE));

    html = html.replace(DataFormVariables.TEMPLATE_FORM_CONTENT, htmlForm);
    return html;
  }

  public void prepareInputHtmlElementMetaField(
      MetaField metaField, String attrs, String elementName) throws ClassNotFoundException {

    Mapper mapper = Mapper.of(Class.forName(metaField.getMetaModel().getFullName()));
    Property property = mapper.getProperty(metaField.getName());

    JsonContext jsonContext =
        new JsonContext(
            new Context(DataFormLine.class),
            Mapper.of(DataFormLine.class).getProperty(DataFormVariables.ATTRS),
            attrs);

    if (ObjectUtils.notEmpty(property.getSelection())) {

      prepareSelectHtmlElementForSelectionFieldMetaModel(
          metaField, property, jsonContext, elementName);

    } else if (property.getType() == PropertyType.TEXT) {

      String attributes =
          String.format(
              " %s %s = '%s' %s = '%s' %s = '%s' %s = '%s' ",
              checkRequired(jsonContext, property),
              DataFormVariables.MAX_LENGTH,
              property.getMaxSize(),
              DataFormVariables.MIN_LENGTH,
              property.getMinSize(),
              DataFormVariables.PATTERN,
              checkPattern(jsonContext),
              DataFormVariables.PLACEHOLDER,
              checkPlaceholder(jsonContext));

      htmlForm.append(
          String.format(
              DataFormVariables.TEXT_AREA, getLabel(metaField), metaField.getName(), attributes));

    } else if (property.getType() == PropertyType.BINARY) {

      htmlForm.append(
          String.format(
              DataFormVariables.FILE_INPUT,
              getLabel(metaField),
              metaField.getName(),
              checkImageWidget(jsonContext),
              checkRequired(jsonContext, property),
              metaField.getName()));
    } else {

      String htmlInputType = getHtmlInputType(metaField.getTypeName());
      String bootstrapClass =
          htmlInputType.equals(DataFormVariables.CHECKBOX)
              ? DataFormVariables.FORM_CHECKBOX
              : DataFormVariables.FORM_CONTROL;
      Boolean isHtmlInputTypeText = htmlInputType.equalsIgnoreCase(DataFormVariables.TEXT);

      String attributes =
          String.format(
              " %s %s = '%s' %s = '%s' %s = '%s' %s = '%s' ",
              checkRequired(jsonContext, property)
                  + checkStepSize(metaField.getTypeName(), property.getScale()),
              isHtmlInputTypeText ? DataFormVariables.MAX_LENGTH : DataFormVariables.MAX,
              property.getMaxSize(),
              isHtmlInputTypeText ? DataFormVariables.MIN_LENGTH : DataFormVariables.MIN,
              property.getMinSize(),
              DataFormVariables.PATTERN,
              checkPattern(jsonContext),
              DataFormVariables.PLACEHOLDER,
              checkPlaceholder(jsonContext));

      htmlForm.append(
          String.format(
              DataFormVariables.INPUT_STRING,
              getLabel(metaField),
              bootstrapClass,
              htmlInputType,
              elementName,
              attributes));
    }
  }

  @SuppressWarnings({"rawtypes"})
  public void prepareSelectHtmlElementMetaField(
      MetaField metaField, String elementName, String attrs, Boolean canSelect, String queryFilter)
      throws ClassNotFoundException, AxelorException, JsonProcessingException {

    String targetClassName = metaField.getPackageName() + "." + metaField.getTypeName();
    Class targetClass = Class.forName(targetClassName);
    Class mainClass = Class.forName(metaField.getMetaModel().getFullName());
    Mapper mapper = Mapper.of(targetClass);
    String nameField =
        mapper.getNameField() != null ? mapper.getNameField().getName() : DataFormVariables.ID;
    Property property = Mapper.of(mainClass).getProperty(metaField.getName());
    JsonContext jsonContext =
        new JsonContext(
            new Context(DataFormLine.class),
            Mapper.of(DataFormLine.class).getProperty(DataFormVariables.ATTRS),
            attrs);

    String attributes =
        String.format(
            " %s %s ",
            metaField.getRelationship().equals(DataFormVariables.MANYTOMANY)
                ? DataFormVariables.MULTIPLE
                : StringUtils.EMPTY,
            checkRequired(jsonContext, property));

    if ((targetClass).equals(MetaFile.class)) {

      htmlForm.append(
          String.format(
              DataFormVariables.FILE_INPUT,
              getLabel(metaField),
              elementName,
              checkImageWidget(jsonContext),
              attributes,
              elementName));
    } else if (!canSelect) {

      allowRecordCreationForMetaModel(metaField, jsonContext, targetClassName, mapper);

    } else {

      htmlForm.append(
          String.format(
              DataFormVariables.SELECT_STRING_START, getLabel(metaField), elementName, attributes));

      List<? extends Model> resultList =
          getResultListMetaModel(metaField, jsonContext, targetClass, queryFilter);

      if (ObjectUtils.notEmpty(resultList)) {
        int limit = Math.min(appBpm.getDataFormSelectRecordLimit(), resultList.size());
        for (int i = 0; i < limit; i++) {
          Map<String, Object> resultMap = Mapper.toMap(resultList.get(i));
          String optionText =
              resultMap.get(nameField) == null
                  ? resultList.get(i).getId().toString()
                  : resultMap.get(nameField).toString();
          Long optionValue = resultList.get(i).getId();
          htmlForm.append(String.format(DataFormVariables.OPTION_STRING, optionValue, optionText));
        }
      }
      htmlForm.append(String.format(DataFormVariables.SELECT_STRING_END));
    }
  }

  protected String checkStepSize(String type, int scale) {
    String bigDecimal = "BigDecimal";
    String decimal = "decimal";

    Double step;
    if (type.equals(bigDecimal) || type.equals(decimal)) {
      if (scale == 0) {
        step = DataFormVariables.DEFAULT_STEP;
      } else {
        step =
            Double.parseDouble(
                String.format(
                    DataFormVariables.STEP_PATTERN,
                    StringUtils.repeat(DataFormVariables.ZERO, scale - 1)));
      }
      return String.format(DataFormVariables.STEP_ATTRIBUTE, step);
    }
    return StringUtils.EMPTY;
  }

  @SuppressWarnings("unchecked")
  protected void allowRecordCreationForMetaModel(
      MetaField metaField, JsonContext jsonContext, String targetClassName, Mapper mapper)
      throws ClassNotFoundException, AxelorException, JsonProcessingException {

    Query<MetaField> query = Beans.get(MetaFieldRepository.class).all();
    Boolean onlyRequired = false;
    List<Map<String, String>> items = new ArrayList<>();

    if (ObjectUtils.notEmpty(jsonContext.get(DataFormVariables.PANEL_EDITOR))) {

      Map<String, List<Map<String, String>>> panelEditor =
          (Map<String, List<Map<String, String>>>) jsonContext.get(DataFormVariables.PANEL_EDITOR);

      items = panelEditor.get(DataFormVariables.ITEMS);
      List<String> editorFieldList =
          panelEditor.get(DataFormVariables.FIELDS).stream()
              .map(field -> field.get(DataFormVariables.NAME))
              .collect(Collectors.toList());
      query
          .filter("self.metaModel.fullName = :modelName AND self.name IN :editorFieldNames")
          .bind("modelName", targetClassName)
          .bind("editorFieldNames", editorFieldList);
    } else {

      query.filter("self.metaModel.fullName = :metaModel").bind("metaModel", targetClassName);
      onlyRequired = true;
    }

    List<MetaField> metaFieldList = query.fetch();

    if (onlyRequired) {
      htmlForm.append(
          String.format(
              DataFormVariables.CREATE_BUTTON,
              getLabel(metaField),
              metaField.getName(),
              metaField.getName()));
    }

    prepareReferredMetaModelForm(metaFieldList, items, mapper, metaField, onlyRequired);
  }

  protected void prepareReferredMetaModelForm(
      List<MetaField> metaFieldList,
      List<Map<String, String>> items,
      Mapper mapper,
      MetaField mainMetaField,
      Boolean onlyRequired)
      throws ClassNotFoundException, AxelorException, JsonProcessingException {

    for (MetaField metaField : metaFieldList) {

      Property property = mapper.getProperty(metaField.getName());

      Optional<Map<String, String>> itemMap =
          items.stream()
              .filter(item -> item.get(DataFormVariables.NAME).equals(metaField.getName()))
              .findAny();

      Map<String, String> attributeMap = new HashMap<>();
      if (itemMap.isPresent()) {
        attributeMap = itemMap.get();
      }

      String attrs = objectMapper.writeValueAsString(attributeMap);

      if (onlyRequired && !property.isRequired()) {
        continue;
      }

      String elementName =
          DataFormVariables.REFERRED_ENTITY_IDENTIFIER
              + mainMetaField.getPackageName()
              + "."
              + mainMetaField.getTypeName()
              + DataFormVariables.REFERRED_ENTITY_FIELD_IDENTIFER
              + mainMetaField.getName()
              + DataFormVariables.REFERRED_ENTITY_FIELD_IDENTIFER
              + metaField.getName();

      if (metaField.getRelationship() == null) {

        prepareInputHtmlElementMetaField(mainMetaField, attrs, elementName);

      } else if (DataFormVariables.VALID_RELATIONSHIP_TYPES_META_MODEL.contains(
          metaField.getRelationship())) {

        prepareSelectHtmlElementMetaField(
            metaField, elementName, StringUtils.EMPTY, true, StringUtils.EMPTY);
      }
    }

    if (onlyRequired) {
      htmlForm.append(DataFormVariables.END_DIV);
    }
  }

  protected void prepareReferredJsonModelForm(
      List<MetaJsonField> metaJsonFieldList, MetaJsonField mainMetaJsonField)
      throws ClassNotFoundException, AxelorException, JsonProcessingException {

    for (MetaJsonField jsonField : metaJsonFieldList) {

      if (jsonField.getRequired()) {

        String elementName =
            DataFormVariables.REFERRED_ENTITY_IDENTIFIER
                + mainMetaJsonField.getTargetJsonModel().getName()
                + DataFormVariables.REFERRED_ENTITY_FIELD_IDENTIFER
                + mainMetaJsonField.getName()
                + DataFormVariables.REFERRED_ENTITY_FIELD_IDENTIFER
                + jsonField.getName();

        if (jsonField.getTargetJsonModel() == null && jsonField.getTargetModel() == null) {

          prepareInputHtmlElementMetaJsonModel(jsonField, elementName);

        } else if (DataFormVariables.VALID_RELATIONSHIP_TYPES_JSON_MODEL.contains(
            jsonField.getType())) {

          prepareSelectHtmlElementMetaJsonModel(
              jsonField, elementName, StringUtils.EMPTY, true, StringUtils.EMPTY);
        }
      }
    }
    htmlForm.append(DataFormVariables.END_DIV);
  }

  protected void prepareReferredMetaModelForm(
      List<MetaField> metaFieldList, Mapper mapper, MetaJsonField mainMetaJsonField)
      throws ClassNotFoundException, AxelorException, JsonProcessingException {

    for (MetaField metaField : metaFieldList) {
      Property property = mapper.getProperty(metaField.getName());

      if (property.isRequired()) {

        String elementName =
            DataFormVariables.REFERRED_ENTITY_IDENTIFIER
                + mainMetaJsonField.getTargetModel()
                + DataFormVariables.REFERRED_ENTITY_FIELD_IDENTIFER
                + mainMetaJsonField.getName()
                + DataFormVariables.REFERRED_ENTITY_FIELD_IDENTIFER
                + metaField.getName();

        if (metaField.getRelationship() == null) {

          prepareInputHtmlElementMetaField(metaField, StringUtils.EMPTY, elementName);

        } else if (DataFormVariables.VALID_RELATIONSHIP_TYPES_META_MODEL.contains(
            metaField.getRelationship())) {

          prepareSelectHtmlElementMetaField(
              metaField, elementName, StringUtils.EMPTY, true, StringUtils.EMPTY);
        }
      }
    }
    htmlForm.append(DataFormVariables.END_DIV);
  }

  @SuppressWarnings("rawtypes")
  public void prepareSelectHtmlElementMetaJsonModel(
      MetaJsonField metaJsonField,
      String elementName,
      String attrs,
      Boolean canSelect,
      String queryFilter)
      throws ClassNotFoundException, AxelorException, JsonProcessingException {

    String targetModelName =
        metaJsonField.getType().startsWith(DataFormVariables.JSON)
            ? MetaJsonRecord.class.getCanonicalName()
            : metaJsonField.getTargetModel();

    JsonContext jsonContext =
        new JsonContext(
            new Context(MetaJsonField.class),
            Mapper.of(MetaJsonField.class).getProperty(DataFormVariables.WIDGET_ATTRS),
            metaJsonField.getWidgetAttrs());

    String attributes =
        String.format(
            " %s %s ",
            metaJsonField.getType().equals(DataFormVariables.MANY_TO_MANY)
                    || metaJsonField.getType().equals(DataFormVariables.JSON_MANY_TO_MANY)
                ? DataFormVariables.MULTIPLE
                : StringUtils.EMPTY,
            checkRequired(jsonContext, metaJsonField));

    Class targetClass = Class.forName(targetModelName);
    Mapper mapper = Mapper.of(targetClass);

    if ((targetClass).equals(MetaFile.class)) {

      htmlForm.append(
          String.format(
              DataFormVariables.FILE_INPUT,
              getLabel(metaJsonField),
              elementName,
              checkImageWidget(metaJsonField),
              attributes,
              elementName));
    } else if (!canSelect) {

      allowRecordCreationForJsonModel(metaJsonField, targetModelName, mapper);
    } else {

      String nameField =
          mapper.getNameField() != null ? mapper.getNameField().getName() : DataFormVariables.ID;

      htmlForm.append(
          String.format(
              DataFormVariables.SELECT_STRING_START,
              getLabel(metaJsonField),
              elementName,
              attributes));
      List<? extends Model> resultList =
          getResultListMetaJsonModel(metaJsonField, attrs, targetClass, queryFilter);

      if (ObjectUtils.notEmpty(resultList)) {
        int limit = Math.min(appBpm.getDataFormSelectRecordLimit(), resultList.size());
        for (int i = 0; i < limit; i++) {
          Map<String, Object> resultMap = Mapper.toMap(resultList.get(i));
          String optionText =
              resultMap.get(nameField) == null
                  ? resultList.get(i).getId().toString()
                  : resultMap.get(nameField).toString();
          Long optionValue = resultList.get(i).getId();
          htmlForm.append(String.format(DataFormVariables.OPTION_STRING, optionValue, optionText));
        }
      }

      htmlForm.append(String.format(DataFormVariables.SELECT_STRING_END));
    }
  }

  protected void allowRecordCreationForJsonModel(
      MetaJsonField metaJsonField, String targetModelName, Mapper mapper)
      throws ClassNotFoundException, AxelorException, JsonProcessingException {

    htmlForm.append(
        String.format(
            DataFormVariables.CREATE_BUTTON,
            getLabel(metaJsonField),
            metaJsonField.getName(),
            metaJsonField.getName()));

    if (metaJsonField.getType().startsWith(DataFormVariables.JSON)) {

      List<MetaJsonField> metaJsonFieldList = metaJsonField.getTargetJsonModel().getFields();
      prepareReferredJsonModelForm(metaJsonFieldList, metaJsonField);

    } else {

      List<MetaField> metaFieldList =
          Beans.get(MetaFieldRepository.class)
              .all()
              .filter("self.metaModel.fullName = ?1", targetModelName)
              .fetch();
      prepareReferredMetaModelForm(metaFieldList, mapper, metaJsonField);
    }
  }

  public void prepareInputHtmlElementMetaJsonModel(
      MetaJsonField metaJsonField, String elementName) {

    JsonContext jsonContext =
        new JsonContext(
            new Context(MetaJsonField.class),
            Mapper.of(MetaJsonField.class).getProperty(DataFormVariables.WIDGET_ATTRS),
            metaJsonField.getWidgetAttrs());

    if (ObjectUtils.notEmpty(metaJsonField.getSelection())) {

      prepareSelectHtmlElementForSelectionFieldMetaJsonModel(
          metaJsonField, jsonContext, elementName);

    } else {

      String htmlInputType = getHtmlInputType(metaJsonField.getType());
      Boolean isHtmlInputTypeText = htmlInputType.equalsIgnoreCase(DataFormVariables.TEXT);

      String attributes =
          String.format(
              " %s %s = '%s' %s = '%s' %s = '%s' %s = '%s' ",
              checkRequired(jsonContext, metaJsonField)
                  + checkStepSize(metaJsonField.getType(), metaJsonField.getScale()),
              isHtmlInputTypeText ? DataFormVariables.MAX_LENGTH : DataFormVariables.MAX,
              metaJsonField.getMaxSize() == 0 ? StringUtils.EMPTY : metaJsonField.getMaxSize(),
              isHtmlInputTypeText ? DataFormVariables.MIN_LENGTH : DataFormVariables.MIN,
              metaJsonField.getMinSize(),
              DataFormVariables.PATTERN,
              checkPattern(jsonContext, metaJsonField),
              DataFormVariables.PLACEHOLDER,
              checkPlaceholder(jsonContext));

      htmlForm.append(
          String.format(
              DataFormVariables.INPUT_STRING,
              getLabel(metaJsonField),
              htmlInputType.equals(DataFormVariables.CHECKBOX)
                  ? DataFormVariables.FORM_CHECKBOX
                  : DataFormVariables.FORM_CONTROL,
              htmlInputType,
              elementName,
              attributes));
    }
  }

  protected void prepareSelectHtmlElementForSelectionFieldMetaJsonModel(
      MetaJsonField metaJsonField, JsonContext jsonContext, String elementName) {

    htmlForm.append(
        String.format(
            DataFormVariables.SELECT_STRING_START,
            getLabel(metaJsonField),
            elementName,
            checkRequired(jsonContext, metaJsonField)));

    if (ObjectUtils.notEmpty(MetaStore.getSelectionList(metaJsonField.getSelection()))) {
      MetaStore.getSelectionList(metaJsonField.getSelection())
          .forEach(
              option ->
                  htmlForm.append(
                      String.format(
                          DataFormVariables.OPTION_STRING,
                          option.getValue(),
                          translation.getString(option.getTitle()))));
    }

    htmlForm.append(String.format(DataFormVariables.SELECT_STRING_END));
  }

  protected void prepareSelectHtmlElementForSelectionFieldMetaModel(
      MetaField metaField, Property property, JsonContext jsonContext, String elementName) {

    htmlForm.append(
        String.format(
            DataFormVariables.SELECT_STRING_START,
            getLabel(metaField),
            elementName,
            checkRequired(jsonContext, property)));

    if (ObjectUtils.notEmpty(MetaStore.getSelectionList(property.getSelection()))) {
      MetaStore.getSelectionList(property.getSelection())
          .forEach(
              option ->
                  htmlForm.append(
                      String.format(
                          DataFormVariables.OPTION_STRING,
                          option.getValue(),
                          translation.getString(option.getTitle()))));
    }

    htmlForm.append(String.format(DataFormVariables.SELECT_STRING_END));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  protected List<? extends Model> getResultListMetaModel(
      MetaField metaField, JsonContext jsonContext, Class klass, String queryFilter)
      throws AxelorException {

    try {

      List<Map<String, Long>> idMap =
          (List<Map<String, Long>>)
              jsonContext.get(metaField.getTypeName() + DataFormVariables.SET);

      Query<? extends Model> query = JPA.all(klass);

      if (ObjectUtils.notEmpty(idMap)) {

        Set<Long> selectedIDs =
            idMap.stream().map(map -> map.get(DataFormVariables.ID)).collect(Collectors.toSet());
        query.filter("self.id in :selectedIDs").bind("selectedIDs", selectedIDs);
      } else if (ObjectUtils.notEmpty(queryFilter)) query.filter(queryFilter);

      return query.fetch();
    } catch (QueryException e) {
      if (e.getMessage().matches(DataFormVariables.QUERY_EXCEPTION_NAMED_PARAMETER_FORMAT)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(IExceptionMessage.NAMED_PARAMETER_EXCEPTION),
            metaField.getName());
      } else if (e.getMessage()
          .matches(DataFormVariables.QUERY_EXCEPTION_NAMED_PARAMETER_NOT_BOUND)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(IExceptionMessage.NAMED_PARAMETER_BOUND_EXCEPTION),
            e.getLocalizedMessage().substring(e.getLocalizedMessage().indexOf(':') + 1),
            metaField.getName());
      }
      throw e;
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  protected List<? extends Model> getResultListMetaJsonModel(
      MetaJsonField metaJsonField, String attrs, Class klass, String queryFilter)
      throws AxelorException {
    try {
      JsonContext jsonContext =
          new JsonContext(
              new Context(DataFormLine.class),
              Mapper.of(DataFormLine.class).getProperty(DataFormVariables.ATTRS),
              attrs);

      Boolean jsonRefer = metaJsonField.getType().startsWith(DataFormVariables.JSON);

      String typeName =
          jsonRefer
              ? metaJsonField.getTargetJsonModel().getName()
              : metaJsonField
                  .getTargetModel()
                  .substring(metaJsonField.getTargetModel().lastIndexOf(".") + 1);

      List<Map<String, Long>> idMap =
          (List<Map<String, Long>>) jsonContext.get(typeName + DataFormVariables.SET);
      Query<? extends Model> query = JPA.all(klass);

      if (ObjectUtils.notEmpty(idMap)) {

        Set<Long> selectedIDs =
            idMap.stream().map(map -> map.get(DataFormVariables.ID)).collect(Collectors.toSet());
        query.filter("self.id in :selectedIDs").bind("selectedIDs", selectedIDs);
      } else if (ObjectUtils.notEmpty(queryFilter) && jsonRefer) {

        query
            .filter(String.format("self.jsonModel = :jsonModel AND %s", queryFilter))
            .bind("jsonModel", metaJsonField.getTargetJsonModel().getName());
      } else if (ObjectUtils.notEmpty(queryFilter)) {

        query.filter(queryFilter);
      } else if (jsonRefer) {

        query
            .filter("self.jsonModel = :jsonModel")
            .bind("jsonModel", metaJsonField.getTargetJsonModel().getName());
      }

      return query.fetch();
    } catch (QueryException e) {
      if (e.getMessage().matches(DataFormVariables.QUERY_EXCEPTION_NAMED_PARAMETER_FORMAT)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(IExceptionMessage.NAMED_PARAMETER_EXCEPTION),
            metaJsonField.getName());
      }
      throw e;
    }
  }

  protected String checkRequired(JsonContext jsonContext, Property property) {
    if (property.isRequired()) {
      return DataFormVariables.REQUIRED;
    }
    Boolean required = (Boolean) jsonContext.get(DataFormVariables.REQUIRED);
    return Boolean.TRUE.equals(required) ? DataFormVariables.REQUIRED : StringUtils.EMPTY;
  }

  protected String checkRequired(JsonContext jsonContext, MetaJsonField metaJsonField) {
    if (metaJsonField.getRequired()) {
      return DataFormVariables.REQUIRED;
    }
    Boolean required = Boolean.valueOf((String) jsonContext.get(DataFormVariables.REQUIRED));
    return required ? DataFormVariables.REQUIRED : StringUtils.EMPTY;
  }

  protected String checkImageWidget(JsonContext jsonContext) {
    if (DataFormVariables.IMAGE.equals(jsonContext.get(DataFormVariables.WIDGET))) {
      return DataFormVariables.ACCEPT_ONLY_IMAGE_FILES;
    }
    return DataFormVariables.ACCEPT_ALL_FILES;
  }

  protected String checkImageWidget(MetaJsonField metaJsonField) {
    if (DataFormVariables.IMAGE.equals(metaJsonField.getWidget())) {
      return DataFormVariables.ACCEPT_ONLY_IMAGE_FILES;
    }
    return DataFormVariables.ACCEPT_ALL_FILES;
  }

  protected String checkPattern(JsonContext jsonContext, MetaJsonField metaJsonField) {
    if (StringUtils.isNotEmpty(metaJsonField.getRegex())) {
      return metaJsonField
          .getRegex()
          .replace(DataFormVariables.SINGLE_QUOTE, DataFormVariables.SINGLE_QUOTE_HTML);
    }
    return checkPattern(jsonContext);
  }

  protected String checkPattern(JsonContext jsonContext) {
    if (ObjectUtils.notEmpty(jsonContext.get(DataFormVariables.PATTERN))) {
      return ((String) jsonContext.get(DataFormVariables.PATTERN))
          .replace(DataFormVariables.SINGLE_QUOTE, DataFormVariables.SINGLE_QUOTE_HTML);
    }
    return DataFormVariables.DEFAULT_PATTERN;
  }

  protected String checkPlaceholder(JsonContext jsonContext) {
    if (ObjectUtils.notEmpty(jsonContext.get(DataFormVariables.PLACEHOLDER))) {
      return ((String) jsonContext.get(DataFormVariables.PLACEHOLDER))
          .replace(DataFormVariables.SINGLE_QUOTE, DataFormVariables.SINGLE_QUOTE_HTML);
    }
    return StringUtils.EMPTY;
  }

  protected String getLabel(MetaJsonField metaJsonField) {
    return translation.getString(
        ObjectUtils.isEmpty(metaJsonField.getTitle())
            ? Inflector.getInstance().titleize(metaJsonField.getName())
            : metaJsonField.getTitle());
  }

  protected String getLabel(MetaField metaField) {
    return translation.getString(
        ObjectUtils.isEmpty(metaField.getLabel())
            ? Inflector.getInstance().titleize(metaField.getName())
            : metaField.getLabel());
  }

  protected String getHtmlInputType(String type) {
    if (DataFormVariables.allowedForNumber.contains(type)) {
      return DataFormVariables.NUMBER;
    } else if (DataFormVariables.allowedForDate.contains(type)) {
      return DataFormVariables.DATE;
    } else if (DataFormVariables.allowedForTime.contains(type)) {
      return DataFormVariables.TIME;
    } else if (DataFormVariables.allowedForDateTime.contains(type)) {
      return DataFormVariables.DATE_TIME;
    } else if (DataFormVariables.allowedForCheckbox.contains(type)) {
      return DataFormVariables.CHECKBOX;
    } else {
      return DataFormVariables.TEXT;
    }
  }
}
