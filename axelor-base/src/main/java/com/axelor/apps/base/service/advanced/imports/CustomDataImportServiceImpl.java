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
import com.axelor.apps.base.service.advancedExport.AdvancedExportService;
import com.axelor.data.XStreamUtils;
import com.axelor.data.csv.CSVBind;
import com.axelor.data.csv.CSVInput;
import com.axelor.data.csv.CSVInputJson;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.db.MetaJsonField;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class CustomDataImportServiceImpl implements CustomDataImportService {

  @Inject private CustomAdvancedImportService customAdvancedImportService;

  @Inject private AdvancedImportService advancedImportService;

  @Inject private DataImportService dataImportService;

  private Map<String, String> nameFieldMap = new HashMap<>();

  @Override
  public CSVInputJson createCSVInputJson(
      FileTab fileTab, String fileName, String inputCallable, char csvSepartor) {
    boolean update = false;
    String searchCall = fileTab.getSearchCall();

    if (fileTab.getImportType() != FileFieldRepository.IMPORT_TYPE_FIND_NEW
        && (CollectionUtils.isNotEmpty(fileTab.getSearchFieldSet())
            || StringUtils.isNotBlank(searchCall))) {
      update = true;
    }

    XStream stream = XStreamUtils.createXStream();
    stream.processAnnotations(CSVInput.class);
    CSVInputJson input = new CSVInputJson();
    input.setUpdate(update);
    input.setFileName(fileName);
    input.setJsonModel(fileTab.getJsonModel().getName());
    input.setCallable(inputCallable);
    input.setSeparator(csvSepartor);
    input.setSearch(null);
    input.setBindings(new ArrayList<>());
    input.setSearchCall(searchCall);

    return input;
  }

  @Override
  public void checkAndWriteJsonData(String dataCell, FileField fileField, List<String> dataList)
      throws ClassNotFoundException {

    if (Strings.isNullOrEmpty(fileField.getSubImportField())) {
      if (fileField.getJsonField() != null
          && !Strings.isNullOrEmpty(fileField.getJsonField().getSelection())) {
        dataImportService.writeSelectionData(
            fileField.getJsonField().getSelection(),
            dataCell,
            fileField.getForSelectUse(),
            dataList);

      } else {
        dataList.add(dataCell);
      }
    } else {
      String[] subFields = fileField.getSubImportField().split("\\.");
      this.checkJsonSubFieldAndWriteData(
          subFields, 0, fileField.getJsonField(), dataCell, fileField.getForSelectUse(), dataList);
    }
  }

  @Override
  public void checkJsonSubFieldAndWriteData(
      String[] subFields,
      int index,
      MetaJsonField parentField,
      String dataCell,
      int forSelectUse,
      List<String> dataList)
      throws ClassNotFoundException {

    if (index < subFields.length) {
      if (parentField.getTargetJsonModel() != null) {
        MetaJsonField childField =
            customAdvancedImportService.getJsonField(
                subFields[index], null, null, parentField.getTargetJsonModel());

        if (childField.getTargetJsonModel() != null
            || !Strings.isNullOrEmpty(childField.getTargetModel())) {
          this.checkJsonSubFieldAndWriteData(
              subFields, index + 1, childField, dataCell, forSelectUse, dataList);

        } else {
          if (!Strings.isNullOrEmpty(childField.getSelection())) {
            dataImportService.writeSelectionData(
                childField.getSelection(), dataCell, forSelectUse, dataList);
          } else {
            dataList.add(dataCell);
          }
        }
      } else if (!Strings.isNullOrEmpty(parentField.getTargetModel())) {
        Mapper mapper = advancedImportService.getMapper(parentField.getTargetModel());
        Property childProp = mapper.getProperty(subFields[index]);
        if (childProp.getTarget() != null) {
          dataImportService.checkSubFieldAndWriteData(
              subFields, index + 1, childProp, dataCell, forSelectUse, dataList);

        } else {
          if (!Strings.isNullOrEmpty(childProp.getSelection())) {
            dataImportService.writeSelectionData(
                childProp.getSelection(), dataCell, forSelectUse, dataList);
          } else {
            dataList.add(dataCell);
          }
        }
      }
    }
  }

  @Override
  public List<CSVBind> createNameFieldBinding(String fieldname, List<CSVBind> bindList) {

    if (nameFieldMap.containsKey(fieldname)) {
      CSVBind nameBind =
          dataImportService.createCSVBind(
              nameFieldMap.get(fieldname), "name", null, null, null, null);
      bindList.add(nameBind);
    }
    return bindList;
  }

  @Override
  public CSVBind createAttrsCSVBinding(
      String[] subFields,
      int index,
      String column,
      String modelName,
      FileField fileField,
      CSVBind parentBind,
      CSVBind dummyBind,
      boolean isSameParentExist,
      List<CSVBind> allBindings,
      Map<String, CSVBind> parentBindMap,
      String fullFieldName)
      throws ClassNotFoundException {

    if (index > 1) {
      return createSubAttrsCSVBinding(
          subFields,
          index,
          column,
          modelName,
          fileField,
          parentBind,
          dummyBind,
          isSameParentExist,
          parentBindMap,
          fullFieldName);
    }

    MetaJsonField jsonField =
        customAdvancedImportService.getJsonField(subFields[index], modelName, null, null);

    this.addJsonNameFields(jsonField, subFields, column, fileField.getSubImportField(), index + 1);

    String fieldName = "$attrs." + jsonField.getName();

    if (jsonField.getTargetJsonModel() != null
        || !Strings.isNullOrEmpty(jsonField.getTargetModel())) {
      if (parentBindMap.containsKey(fieldName)) {
        parentBind = parentBindMap.get(fieldName);
        isSameParentExist = true;

      } else {
        parentBind = dataImportService.createCSVBind(null, fieldName, null, null, null, true);
        parentBind.setBindings(new ArrayList<>());
        allBindings.add(parentBind);
        parentBindMap.put(fieldName, parentBind);
      }
      fullFieldName = fieldName;

      this.createCustomCSVSubBinding(
          subFields,
          index + 1,
          column,
          jsonField,
          fileField,
          parentBind,
          dummyBind,
          isSameParentExist,
          parentBindMap,
          fullFieldName);

    } else {

      CSVBind bind =
          createCustomChildCSVSubBinding(fileField, jsonField, dummyBind, fieldName, column);

      if (parentBind == null) {
        parentBind = bind;
        allBindings.add(parentBind);
      } else {
        parentBind.getBindings().add(bind);
      }
    }

    return parentBind;
  }

  private CSVBind createSubAttrsCSVBinding(
      String[] subFields,
      int index,
      String column,
      String modelName,
      FileField fileField,
      CSVBind parentBind,
      CSVBind dummyBind,
      boolean isSameParentExist,
      Map<String, CSVBind> subBindMap,
      String fullFieldName)
      throws ClassNotFoundException {

    MetaJsonField jsonField =
        customAdvancedImportService.getJsonField(subFields[index], modelName, null, null);

    this.addJsonNameFields(jsonField, subFields, column, fileField.getSubImportField(), index + 1);

    fullFieldName += "." + subFields[index];
    String fieldName = "$attrs." + jsonField.getName();
    CSVBind subBind = null;

    if (jsonField.getTargetJsonModel() != null
        || !Strings.isNullOrEmpty(jsonField.getTargetModel())) {
      subBind =
          checkAvailableSubBinding(
              subBindMap, parentBind, subBind, fieldName, fullFieldName, index);

      if (fileField.getImportType() == FileFieldRepository.IMPORT_TYPE_FIND_NEW) {
        this.setSearch(
            column, fieldName, fileField, jsonField, parentBind, index, isSameParentExist);
      }

      this.createCustomCSVSubBinding(
          subFields,
          index + 1,
          column,
          jsonField,
          fileField,
          fileField.getImportType() == FileFieldRepository.IMPORT_TYPE_FIND ? parentBind : subBind,
          dummyBind,
          isSameParentExist,
          subBindMap,
          fullFieldName);

    } else if (fileField.getImportType() != FileFieldRepository.IMPORT_TYPE_FIND) {
      CSVBind bind =
          createCustomChildCSVSubBinding(fileField, jsonField, dummyBind, fieldName, column);

      parentBind.getBindings().add(bind);
    }

    return parentBind;
  }

  @Override
  public void createCustomCSVSubBinding(
      String[] subFields,
      int index,
      String column,
      MetaJsonField parentField,
      FileField fileField,
      CSVBind parentBind,
      CSVBind dummyBind,
      boolean isSameParentExist,
      Map<String, CSVBind> subBindMap,
      String fullFieldName)
      throws ClassNotFoundException {

    if ((index >= subFields.length)
        || parentField == null
        || (parentField.getTargetJsonModel() == null
            && Strings.isNullOrEmpty(parentField.getTargetModel()))) {
      return;
    }

    int importType = fileField.getImportType();

    if (parentField.getTargetJsonModel() != null) {
      MetaJsonField childField =
          customAdvancedImportService.getJsonField(
              subFields[index], null, null, parentField.getTargetJsonModel());

      CSVBind subBind = null;
      fullFieldName += "." + subFields[index];
      String fieldName = "$attrs.".concat(childField.getName());

      if (childField.getTargetJsonModel() != null
          || !Strings.isNullOrEmpty(childField.getTargetModel())) {
        subBind =
            checkAvailableSubBinding(
                subBindMap, parentBind, subBind, fieldName, fullFieldName, importType);

        this.setSearch(
            column, fieldName, fileField, childField, parentBind, index, isSameParentExist);

        if (importType == FileFieldRepository.IMPORT_TYPE_FIND) {
          return;
        }

        parentBind.setUpdate(false);

        this.createCustomCSVSubBinding(
            subFields,
            index + 1,
            column,
            childField,
            fileField,
            importType == FileFieldRepository.IMPORT_TYPE_FIND ? parentBind : subBind,
            dummyBind,
            isSameParentExist,
            subBindMap,
            fullFieldName);
      } else {
        if (importType != FileFieldRepository.IMPORT_TYPE_FIND) {
          subBind =
              createCustomChildCSVSubBinding(fileField, childField, dummyBind, fieldName, column);

          parentBind.getBindings().add(subBind);
          subBindMap.put(fullFieldName, subBind);

          parentBind.setUpdate(false);

          String key = childField.getName().concat(".").concat(fileField.getSubImportField());
          parentBind.setBindings(createNameFieldBinding(key, parentBind.getBindings()));

          this.checkAndCreateJsonModelBinding(
              parentBind, parentField.getTargetJsonModel().getName());
        }
        this.setSearch(
            column, fieldName, fileField, childField, parentBind, index, isSameParentExist);
      }
    } else if (!Strings.isNullOrEmpty(parentField.getTargetModel())) {
      Mapper mapper = advancedImportService.getMapper(parentField.getTargetModel());
      Property childProp = mapper.getProperty(subFields[index]);

      this.createRealCSVSubBind(
          subFields,
          index + 1,
          column,
          childProp,
          fileField,
          parentBind,
          dummyBind,
          isSameParentExist,
          subBindMap,
          fullFieldName);
    }
  }

  private void createRealCSVSubBind(
      String[] subFields,
      int index,
      String column,
      Property parentProp,
      FileField fileField,
      CSVBind parentBind,
      CSVBind dummyBind,
      boolean isSameParentExist,
      Map<String, CSVBind> subBindMap,
      String fullFieldName)
      throws ClassNotFoundException {

    if (index > subFields.length) {
      return;
    }
    int importType = fileField.getImportType();
    String relationship = fileField.getRelationship();

    if (parentProp != null && parentProp.getTarget() != null) {
      Mapper mapper = advancedImportService.getMapper(parentProp.getTarget().getName());
      Property childProp = mapper.getProperty(subFields[index]);
      fullFieldName += "." + parentProp.getName();

      CSVBind subBind =
          checkAvailableSubBinding(
              subBindMap, parentBind, null, parentProp.getName(), fullFieldName, importType);

      String fieldName =
          parentProp.getName()
              + "."
              + Joiner.on(".").join(Arrays.asList(subFields).subList(index, subFields.length));

      dataImportService.setSearch(
          column, fieldName, parentProp, fileField, parentBind, index - 1, isSameParentExist);

      if (importType == FileFieldRepository.IMPORT_TYPE_FIND) {
        return;
      }

      parentBind.setUpdate(false);

      this.createRealCSVSubBind(
          subFields,
          index + 1,
          column,
          childProp,
          fileField,
          isSameParentExist && importType == FileFieldRepository.IMPORT_TYPE_FIND
              ? parentBind
              : subBind,
          dummyBind,
          isSameParentExist,
          subBindMap,
          fullFieldName);
    } else {
      if (AdvancedExportService.FIELD_ATTRS.equals(parentProp.getName())) {
        parentBind =
            createSubAttrsCSVBinding(
                subFields,
                index,
                column,
                parentProp.getEntity().getCanonicalName(),
                fileField,
                parentBind,
                dummyBind,
                isSameParentExist,
                subBindMap,
                fullFieldName);
      } else {
        String fieldName =
            isSameParentExist && importType == FileFieldRepository.IMPORT_TYPE_FIND
                ? fileField.getSubImportField()
                : parentProp.getName();

        createChildCSVSubBinding(
            fileField,
            parentProp,
            parentBind,
            dummyBind,
            fieldName,
            column,
            relationship,
            importType,
            index,
            isSameParentExist);
      }

      if (importType != FileFieldRepository.IMPORT_TYPE_FIND) {
        parentBind.setUpdate(false);
      }
    }
  }

  @Override
  public void createChildCSVSubBinding(
      FileField fileField,
      Property parentProp,
      CSVBind parentBind,
      CSVBind dummyBind,
      String fieldName,
      String column,
      String relationship,
      int importType,
      int index,
      boolean isSameParentExist)
      throws ClassNotFoundException {
    String expression =
        dataImportService.setExpression(column, fileField, parentProp.getSelection());
    String adapter = null;
    String dateFormat = fileField.getDateFormat();

    if (Strings.isNullOrEmpty(expression) && !Strings.isNullOrEmpty(dateFormat)) {
      adapter =
          dataImportService.getAdapter(parentProp.getJavaType().getSimpleName(), dateFormat.trim());
    }

    if (!fileField.getIsMatchWithFile()) {
      dataImportService.createBindForNotMatchWithFile(
          column, importType, dummyBind, expression, adapter, parentBind, parentProp);

    } else {
      dataImportService.createBindForMatchWithFile(
          column, importType, expression, adapter, relationship, parentBind, parentProp);
    }

    dataImportService.setSearch(
        column, fieldName, parentProp, fileField, parentBind, index, isSameParentExist);
  }

  private CSVBind createCustomChildCSVSubBinding(
      FileField fileField,
      MetaJsonField jsonField,
      CSVBind dummyBind,
      String fieldName,
      String column) {
    String selection = jsonField.getSelection();
    String expression = dataImportService.setExpression(column, fileField, selection);
    String adapter = null;
    String dateFormat = fileField.getDateFormat();
    boolean isRequired = jsonField.getRequired();

    if (Strings.isNullOrEmpty(expression) && !Strings.isNullOrEmpty(dateFormat)) {
      String type = fileField.getTargetType();
      adapter = dataImportService.getAdapter(type, dateFormat.trim());
    }

    CSVBind bind = null;
    if (!fileField.getIsMatchWithFile()) {
      dummyBind.setExpression(expression);
      dummyBind.setAdapter(adapter);
      bind = dataImportService.createCSVBind(column, fieldName, null, null, null, null);
      dataImportService.setImportIf(isRequired, bind, column);
    } else {
      bind = dataImportService.createCSVBind(column, fieldName, null, expression, adapter, null);
      dataImportService.setImportIf(isRequired, bind, column);
    }
    if (jsonField.getType().equals("boolean")) {
      bind.setExpression(column + " == 'true' ? true : false");
    }

    return bind;
  }

  private void checkAndCreateJsonModelBinding(CSVBind parentBind, String modelName) {
    if (parentBind.getBindings().stream()
            .filter(bind -> bind.getField().equals("jsonModel"))
            .count()
        == 0) {
      String expr = "'" + modelName + "'";
      CSVBind modelBind =
          dataImportService.createCSVBind(null, "jsonModel", null, expr, null, null);
      parentBind.getBindings().add(modelBind);
    }
  }

  @Override
  public CSVBind checkAvailableSubBinding(
      Map<String, CSVBind> subBindMap,
      CSVBind parentBind,
      CSVBind subBind,
      String fieldName,
      String key,
      int importType) {
    if (subBindMap.containsKey(key)) {
      subBind = subBindMap.get(key);

    } else if (importType != FileFieldRepository.IMPORT_TYPE_FIND) {
      subBind = dataImportService.createCSVBind(null, fieldName, null, null, null, true);
      subBind.setBindings(new ArrayList<>());
      parentBind.getBindings().add(subBind);
      subBindMap.put(key, subBind);
    }
    return subBind;
  }

  @Override
  public void addJsonModelAndSearch(
      List<CSVBind> allBindings,
      Map<Object, Object> searchMap,
      CSVInputJson jsonCsvInput,
      String model)
      throws ClassNotFoundException {

    CSVBind jsonModelBind =
        dataImportService.createCSVBind(null, "jsonModel", null, "'" + model + "'", null, null);
    allBindings.add(jsonModelBind);

    String preparedSearch = null;
    for (Entry<Object, Object> entry : searchMap.entrySet()) {
      String search =
          this.prepareSearchField((FileField) entry.getKey(), (String) entry.getValue());

      if (!Strings.isNullOrEmpty(preparedSearch)) {
        preparedSearch = preparedSearch + " AND " + search;
      } else {
        preparedSearch = search;
      }
    }

    if (!Strings.isNullOrEmpty(preparedSearch)) {
      jsonCsvInput.setSearch(preparedSearch);
    }
  }

  @Override
  public void addCustomObjectNameField(FileField fileField, String column) {
    if (Strings.isNullOrEmpty(fileField.getSubImportField())) {
      if (fileField.getJsonField().getNameField()) {
        nameFieldMap.put(fileField.getJsonField().getName(), column);
      }
    } else {
      String[] subFields = fileField.getSubImportField().split("\\.");

      this.addJsonNameFields(
          fileField.getJsonField(), subFields, column, fileField.getSubImportField(), 0);
    }
  }

  private void addJsonNameFields(
      MetaJsonField jsonField,
      String[] subFields,
      String column,
      String subImportField,
      int index) {
    if (jsonField.getTargetJsonModel() != null) {
      jsonField =
          customAdvancedImportService.getJsonField(
              subFields[index], null, null, jsonField.getTargetJsonModel());

      addJsonNameFields(jsonField, subFields, column, subImportField, index + 1);
    } else if (jsonField.getNameField()) {
      String key = jsonField.getName().concat(".").concat(subImportField);

      nameFieldMap.put(key, column);
    }
  }

  public String appendSearchFieldFilter(FileField fileField, String model, String column)
      throws ClassNotFoundException {
    StringBuilder filterQuery = new StringBuilder(" ");
    String[] subFields = fileField.getSubImportField().split("\\.");
    Mapper mapper =
        advancedImportService.getMapper(fileField.getImportField().getMetaModel().getFullName());
    Property prop = mapper.getProperty(fileField.getImportField().getName());

    for (int subIndex = 0; subIndex < subFields.length; subIndex++) {

      if (prop.getName().equals("attrs")) {
        if (subIndex == 0) {
          if (subFields.length == 1) {
            filterQuery.append(
                getJsonExtractType(fileField.getTargetType())
                    + "self.attrs,'"
                    + subFields[0]
                    + "') = :"
                    + column);

            return filterQuery.toString();
          }

          filterQuery.append(prepareJsonExtract(subFields[0], "MetaJsonRecord", "self."));

          MetaJsonField jsonField =
              customAdvancedImportService.getJsonField(
                  subFields[0], mapper.getBeanClass().getCanonicalName(), null, null);

          appendSearchForJsonField(filterQuery, jsonField, fileField, column, subIndex + 1);
        } else {

          filterQuery.append(".id IN (SELECT id FROM " + prop.getEntity().getName() + " WHERE ");

          appendSearchForAttrsField(filterQuery, fileField, prop, column, subIndex);
          filterQuery.append(")");
        }

        break;
      } else {
        if (subIndex == 0) {
          filterQuery.append("self");
        }

        filterQuery.append("." + prop.getName());
        mapper = advancedImportService.getMapper(prop.getTarget().getName());
        prop = mapper.getProperty(subFields[subIndex]);
      }
    }

    return filterQuery.toString();
  }

  private String prepareSearchField(FileField searchField, String column)
      throws ClassNotFoundException {
    StringBuilder preparedFilter = new StringBuilder();
    String jsonFieldName = searchField.getJsonField().getName();
    String subImportField = searchField.getSubImportField();

    if (Strings.isNullOrEmpty(subImportField)) {
      preparedFilter.append(
          "("
              + getJsonExtractType(searchField.getTargetType())
              + "self.attrs,'"
              + jsonFieldName
              + "') = :"
              + column
              + ")");
      return preparedFilter.toString();
    }

    if (searchField.getJsonField().getTargetJsonModel() != null) {
      preparedFilter.append("(" + prepareJsonExtract(jsonFieldName, "MetaJsonRecord", "self."));

      appendSearchForJsonField(preparedFilter, searchField.getJsonField(), searchField, column, 0);
    } else {
      if (!subImportField.contains("attrs.")) {
        preparedFilter.append(
            "("
                + prepareJsonExtract(
                    jsonFieldName, searchField.getJsonField().getTargetModel(), "self.")
                + searchField.getSubImportField()
                + " = :"
                + column
                + "))");
        return preparedFilter.toString();
      }

      String[] subFields = subImportField.split("\\.");
      Mapper mapper = advancedImportService.getMapper(searchField.getJsonField().getTargetModel());

      preparedFilter.append(
          prepareJsonExtract(jsonFieldName, searchField.getJsonField().getTargetModel(), "self."));

      this.checkAndAppendAttrsSearch(
          preparedFilter, subFields, searchField, mapper, column, 0, false, false, false);
    }
    preparedFilter.append(")");
    return preparedFilter.toString();
  }

  @Override
  public void appendSearchForJsonField(
      StringBuilder preparedFilter,
      MetaJsonField jsonField,
      FileField fileField,
      String column,
      int index)
      throws ClassNotFoundException {
    String[] subImportFieldArr = fileField.getSubImportField().split("\\.");
    int length = subImportFieldArr.length;

    jsonField =
        customAdvancedImportService.getJsonField(
            subImportFieldArr[index], null, null, jsonField.getTargetJsonModel());

    if (!Strings.isNullOrEmpty(jsonField.getTargetModel())) {

      List<String> subFieldList = Arrays.asList(subImportFieldArr).subList(index + 1, length);

      if (subFieldList.contains("attrs")) {
        Mapper mapper = advancedImportService.getMapper(jsonField.getTargetModel());

        preparedFilter.append(
            "jsonModel = '"
                + jsonField.getJsonModel().getName()
                + "' AND json_extract_integer(attrs,'"
                + subImportFieldArr[index]
                + "','id')");

        this.checkAndAppendAttrsSearch(
            preparedFilter,
            subImportFieldArr,
            fileField,
            mapper,
            column,
            index + 1,
            false,
            false,
            true);
      } else {
        String field =
            Joiner.on(".")
                .join(
                    Arrays.asList(subImportFieldArr).subList(index + 1, subImportFieldArr.length));

        preparedFilter.append(
            "jsonModel = '"
                + jsonField.getJsonModel().getName()
                + "' AND "
                + prepareJsonExtract(jsonField.getName(), jsonField.getTargetModel(), "")
                + field
                + " = :"
                + column);
      }

      preparedFilter.append(")");
    } else {
      if (length - 1 == index) {
        preparedFilter.append(
            "jsonModel = '"
                + jsonField.getJsonModel().getName()
                + "' AND "
                + getJsonExtractType(fileField.getTargetType())
                + "attrs,'"
                + subImportFieldArr[index]
                + "') = :"
                + column);
      } else {
        preparedFilter.append(
            "jsonModel = '"
                + jsonField.getJsonModel().getName()
                + "' AND ("
                + prepareJsonExtract(jsonField.getName(), "MetaJsonRecord", ""));

        appendSearchForJsonField(preparedFilter, jsonField, fileField, column, index + 1);
        preparedFilter.append(")");
      }
    }
    preparedFilter.append(")");
  }

  @Override
  public void appendSearchForAttrsField(
      StringBuilder preparedFilter,
      FileField fileField,
      Property parentProp,
      String column,
      int index)
      throws ClassNotFoundException {

    String[] subFields = fileField.getSubImportField().split("\\.");

    if (AdvancedExportService.FIELD_ATTRS.equals(parentProp.getName())) {
      MetaJsonField jsonField =
          customAdvancedImportService.getJsonField(
              subFields[index], parentProp.getEntity().getCanonicalName(), null, null);

      if (Strings.isNullOrEmpty(jsonField.getTargetModel())
          && jsonField.getTargetJsonModel() == null) {

        preparedFilter.append(
            getJsonExtractType(fileField.getTargetType())
                + "attrs,'"
                + subFields[index]
                + "') = :"
                + column);

      } else {
        preparedFilter.append(prepareJsonExtract(subFields[index], "MetaJsonRecord", ""));

        appendSearchForJsonField(preparedFilter, jsonField, fileField, column, index + 1);
      }

    } else {
      Mapper mapper = advancedImportService.getMapper(parentProp.getTarget().getName());
      Property childProp = mapper.getProperty(subFields[index]);
      appendSearchForAttrsField(preparedFilter, fileField, childProp, column, index + 1);
    }
  }

  @Override
  public void setSearch(
      String column,
      String field,
      FileField fileField,
      MetaJsonField jsonField,
      CSVBind bind,
      int index,
      boolean isSameParentExist)
      throws ClassNotFoundException {

    int importType = fileField.getImportType();
    String cond = "";

    if (importType == FileFieldRepository.IMPORT_TYPE_FIND
        || importType == FileFieldRepository.IMPORT_TYPE_FIND_NEW) {

      if (!Strings.isNullOrEmpty(bind.getSearch())) {
        cond = bind.getSearch() + " AND ";
      }

      String[] subFields = fileField.getSubImportField().split("\\.");
      StringBuilder searchString = new StringBuilder();

      if (jsonField != null && jsonField.getTargetJsonModel() != null) {
        this.appendSearchForJsonField(searchString, jsonField, fileField, column, index + 1);

        bind.setSearch(
            cond
                + prepareJsonExtract(jsonField.getName(), "MetaJsonRecord", "self.")
                + searchString.toString());
      } else if (jsonField != null && !Strings.isNullOrEmpty(jsonField.getTargetModel())) {
        List<String> subFieldList = Arrays.asList(subFields).subList(index + 1, subFields.length);

        if (subFieldList.contains("attrs")) {
          Mapper mapper = advancedImportService.getMapper(jsonField.getTargetModel());
          searchString.append(
              prepareJsonExtract(jsonField.getName(), jsonField.getTargetModel(), "self."));

          this.checkAndAppendAttrsSearch(
              searchString, subFields, fileField, mapper, column, index + 1, true, false, false);

          searchString.append(")");
          bind.setSearch(cond + searchString.toString());
        } else {
          field =
              Joiner.on(".").join(Arrays.asList(subFields).subList(index + 1, subFields.length));
          bind.setSearch(
              cond
                  + prepareJsonExtract(jsonField.getName(), jsonField.getTargetModel(), "self.")
                  + field
                  + " = :"
                  + column
                  + ")");
        }
      } else {
        bind.setSearch(
            cond
                + this.getJsonExtractType(fileField.getTargetType())
                + "self.attrs,'"
                + subFields[index]
                + "') = :"
                + column);
      }
    }
  }

  @Override
  public void checkAndAppendAttrsSearch(
      StringBuilder searchString,
      String[] subFields,
      FileField fileField,
      Mapper mapper,
      String column,
      int index,
      boolean isJson,
      boolean isReal,
      boolean isAppend)
      throws ClassNotFoundException {
    for (int subIndex = index; subIndex < subFields.length; subIndex++) {
      Property prop = mapper.getProperty(subFields[subIndex]);

      if (prop.getName().equals("attrs")) {

        if (isAppend) {
          if (subIndex > index) {
            searchString.append("id");
          }
          searchString.append(" IN (SELECT id FROM " + prop.getEntity().getName() + " WHERE ");
        } else {
          if (subIndex > index || isReal) {
            searchString.append("id IN (SELECT id FROM " + prop.getEntity().getName() + " WHERE ");
          }
        }

        appendSearchForAttrsField(searchString, fileField, prop, column, subIndex + 1);

        if (!isJson || subIndex > index) {
          searchString.append(")");
        }
        break;
      } else {
        if (subIndex == index && isAppend) {
          searchString.append(
              " IN (SELECT id FROM "
                  + prop.getEntity().getName()
                  + " WHERE "
                  + prop.getName()
                  + ".");
        } else {
          searchString.append(prop.getName() + ".");
        }

        mapper = advancedImportService.getMapper(prop.getTarget().getName());
      }
    }
  }

  private String prepareJsonExtract(String fieldName, String model, String alias) {
    return "json_extract_integer("
        + alias
        + "attrs,'"
        + fieldName
        + "','id') IN (SELECT id FROM "
        + model
        + " WHERE ";
  }

  private String getJsonExtractType(String dataType) {
    String jsonExtractType = "json_extract";

    switch (dataType) {
      case ValidatorService.BIG_DECIMAL:
        jsonExtractType += "_decimal(";
        break;

      case ValidatorService.INTEGER:
        jsonExtractType += "_integer(";
        break;

      case ValidatorService.BOOLEAN:
        jsonExtractType += "_boolean(";
        break;

      default:
        jsonExtractType += "(";
    }

    return jsonExtractType;
  }
}
