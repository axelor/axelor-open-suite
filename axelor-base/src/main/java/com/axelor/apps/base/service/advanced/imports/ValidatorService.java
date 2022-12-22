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

import static com.axelor.apps.tool.MetaJsonFieldType.MANY_TO_MANY;

import com.axelor.apps.base.db.AdvancedImport;
import com.axelor.apps.base.db.AdvancedImportFileField;
import com.axelor.apps.base.db.AdvancedImportFileTab;
import com.axelor.apps.base.db.repo.AdvancedImportFileFieldRepository;
import com.axelor.apps.base.db.repo.AdvancedImportFileTabRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.tool.reader.DataReaderFactory;
import com.axelor.apps.tool.reader.DataReaderService;
import com.axelor.common.Inflector;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class ValidatorService {

  public static final String STRING = "String";
  public static final String INTEGER = "Integer";
  public static final String LONG = "Long";
  public static final String BIG_DECIMAL = "BigDecimal";
  public static final String BOOLEAN = "Boolean";
  public static final String ZONED_DATE_TIME = "ZonedDateTime";
  public static final String LOCAL_DATE_TIME = "LocalDateTime";
  public static final String LOCAL_TIME = "LocalTime";
  public static final String LOCAL_DATE = "LocalDate";

  private Map<String, Object> titleMap;

  private Map<String, Object> fieldMap;

  @Inject private MetaFiles metaFiles;

  @Inject private AdvancedImportService advancedImportService;

  @Inject private AdvancedImportFileTabRepository advancedImportFileTabRepo;

  @Inject private ActionService actionService;

  @Inject private DataReaderFactory dataReaderFactory;

  @Inject private LogService logService;

  @Inject private MetaJsonFieldRepository metaJsonFieldRepo;

  @Inject private SearchCallService searchCallService;

  public boolean validate(AdvancedImport advancedImport)
      throws AxelorException, IOException, ClassNotFoundException {

    if (advancedImport.getImportFile() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_NO_IMPORT_FILE));
    }

    if (advancedImport.getAttachment() != null
        && !Files.getFileExtension(advancedImport.getAttachment().getFileName()).equals("zip")) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_ATTACHMENT_FORMAT));
    }

    String extension = Files.getFileExtension(advancedImport.getImportFile().getFileName());

    if (extension == null
        || (!extension.equals("xlsx") && !extension.equals("xls") && !extension.equals("csv"))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_FILE_FORMAT_INVALID));
    }

    DataReaderService reader = dataReaderFactory.getDataReader(extension);
    reader.initialize(advancedImport.getImportFile(), advancedImport.getFileSeparator());

    return validate(reader, advancedImport);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public boolean validate(DataReaderService reader, AdvancedImport advancedImport)
      throws IOException, ClassNotFoundException, AxelorException {

    boolean isLog = false;
    String[] sheets = reader.getSheetNames();

    this.validateTab(sheets, advancedImport);

    boolean isConfig = advancedImport.getIsConfigInFile();
    boolean isTabConfig = advancedImport.getIsFileTabConfigAdded();

    sortFileTabList(advancedImport.getAdvancedImportFileTabList());

    for (AdvancedImportFileTab advancedImportFileTab :
        advancedImport.getAdvancedImportFileTabList()) {
      if (!Arrays.stream(sheets).anyMatch(sheet -> sheet.equals(advancedImportFileTab.getName()))) {
        continue;
      }

      fieldMap = new HashMap<>();
      titleMap = new HashMap<>();
      String sheet = advancedImportFileTab.getName();
      logService.initialize(sheet);

      this.validateModel(advancedImportFileTab);

      int tabConfigRowCount = 0;
      int totalLines = reader.getTotalLines(advancedImportFileTab.getName());

      if (isConfig) {
        String[] objectRow = reader.read(sheet, 0, 0);
        if (isTabConfig) {
          tabConfigRowCount =
              advancedImportService.getTabConfigRowCount(sheet, reader, totalLines, objectRow);
        }
        this.validateObject(objectRow, advancedImportFileTab, isTabConfig);
      }
      this.validateSearch(advancedImportFileTab);
      this.validateObjectRequiredFields(advancedImportFileTab);
      this.validateFieldAndData(
          reader, sheet, advancedImportFileTab, isConfig, isTabConfig, tabConfigRowCount);
      this.validateActions(advancedImportFileTab);

      if (advancedImportFileTab.getValidationLog() != null) {
        advancedImportFileTab.setValidationLog(null);
      }

      if (logService.isLogGenerated()) {
        logService.write();
        logService.close();

        File logFile = logService.getLogFile();
        advancedImportFileTab.setValidationLog(
            metaFiles.upload(new FileInputStream(logFile), sheet + "_err.xlsx"));

        logFile.delete();
        isLog = true;
      } else {
        createCustomObjectSet(
            advancedImportFileTab.getClass().getName(),
            advancedImportFileTab.getMetaModel().getFullName(),
            0);
        createCustomButton(
            advancedImportFileTab.getClass().getName(),
            advancedImportFileTab.getMetaModel().getFullName(),
            1);
      }
      advancedImportFileTabRepo.save(advancedImportFileTab);
    }
    return isLog;
  }

  private void validateTab(String[] sheets, AdvancedImport advancedImport) throws AxelorException {
    if (sheets == null) {
      return;
    }
    List<String> sheetList = Arrays.asList(sheets);
    List<String> tabList =
        advancedImport.getAdvancedImportFileTabList().stream()
            .map(tab -> tab.getName())
            .collect(Collectors.toList());

    if (!CollectionUtils.containsAny(tabList, sheetList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_TAB_ERR));
    }
  }

  private void validateModel(AdvancedImportFileTab advancedImportFileTab)
      throws IOException, AxelorException {
    if (advancedImportFileTab.getMetaModel() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_NO_OBJECT),
              advancedImportFileTab.getName()));
    }
  }

  private void validateObject(
      String[] row, AdvancedImportFileTab advancedImportFileTab, Boolean isTabConfig)
      throws IOException, AxelorException {

    int rowIndex = isTabConfig ? 1 : 0;

    if (isTabConfig && row[0] != null) {
      rowIndex = 0;
    }
    if (row == null || (StringUtils.isBlank(row[rowIndex]))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_FILE_FORMAT_INVALID));
    }

    String object = row[rowIndex].trim();
    if (StringUtils.containsIgnoreCase(object, "Object")) {
      String model = object.split("\\:")[1].trim();
      if (advancedImportFileTab.getMetaModel() != null
          && !advancedImportFileTab.getMetaModel().getName().equals(model)) {
        logService.addLog(
            LogService.COMMON_KEY, BaseExceptionMessage.ADVANCED_IMPORT_LOG_1, rowIndex);
      }
    }
  }

  private void validateObjectRequiredFields(AdvancedImportFileTab advancedImportFileTab)
      throws ClassNotFoundException, IOException, AxelorException {

    if (CollectionUtils.isEmpty(advancedImportFileTab.getAdvancedImportFileFieldList())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_NO_FIELDS),
              advancedImportFileTab.getName()));
    }

    List<String> fieldList = new ArrayList<String>();

    for (AdvancedImportFileField advancedImportFileField :
        advancedImportFileTab.getAdvancedImportFileFieldList()) {
      if (advancedImportFileField.getImportField() != null) {
        fieldList.add(advancedImportFileField.getImportField().getName());

      } else {
        logService.addLog(
            BaseExceptionMessage.ADVANCED_IMPORT_LOG_2,
            advancedImportFileField.getColumnTitle(),
            null);
      }
    }

    if (advancedImportFileTab.getImportType()
        == AdvancedImportFileFieldRepository.IMPORT_TYPE_FIND) {
      return;
    }

    if (advancedImportFileTab.getMetaModel() != null) {
      Mapper mapper =
          advancedImportService.getMapper(advancedImportFileTab.getMetaModel().getFullName());
      Model obj = null;
      try {
        obj =
            (Model) Class.forName(advancedImportFileTab.getMetaModel().getFullName()).newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        TraceBackService.trace(e);
      }

      for (Property prop : mapper.getProperties()) {
        if (prop.isRequired() && !fieldList.contains(prop.getName())) {
          if (obj != null && mapper.get(obj, prop.getName()) != null) {
            continue;
          }
          logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_3, prop.getName(), null);
        }
      }
    }
  }

  private void validateFieldAndData(
      DataReaderService reader,
      String sheet,
      AdvancedImportFileTab advancedImportFileTab,
      boolean isConfig,
      boolean isTabConfig,
      int tabConfigRowCount)
      throws ClassNotFoundException, IOException {

    AdvancedImport advancedImport = advancedImportFileTab.getAdvancedImport();
    Map<String, Object> map = isConfig ? fieldMap : titleMap;
    int linesToIgnore = advancedImport.getNbOfFirstLineIgnore();
    int startIndex = isConfig ? 1 : linesToIgnore;

    String[] row = reader.read(sheet, startIndex, 0);
    if (row == null) {
      return;
    }

    sortFileFieldList(advancedImportFileTab.getAdvancedImportFileFieldList());

    int rowCount = row.length;
    for (int cell = 0; cell < rowCount; cell++) {
      String value = row[cell];
      if (Strings.isNullOrEmpty(value)) {
        continue;
      }
      value = value.trim();
      map.put(isConfig ? value.contains("(") ? value.split("\\(")[0] : value : value, cell);
      if (cell == row.length - 1) {
        this.validateFields(startIndex, isConfig, advancedImportFileTab);
      }
    }

    if (!advancedImport.getIsValidateValue()) {
      return;
    }

    int totalLines = reader.getTotalLines(sheet);

    startIndex =
        isConfig
            ? tabConfigRowCount + 3
            : advancedImportFileTab.getAdvancedImport().getIsHeader()
                ? linesToIgnore + 1
                : linesToIgnore;

    for (int line = startIndex; line < totalLines; line++) {
      String[] dataRow = reader.read(sheet, line, row.length);
      if (dataRow == null) {
        continue;
      }
      this.validateData(dataRow, line, isConfig, advancedImportFileTab);
    }
  }

  private void validateFields(
      int line, boolean isConfig, AdvancedImportFileTab advancedImportFileTab)
      throws IOException, ClassNotFoundException {

    List<String> relationalFieldList =
        advancedImportFileTab.getAdvancedImportFileFieldList().stream()
            .filter(field -> !Strings.isNullOrEmpty(field.getSubImportField()))
            .map(field -> field.getImportField().getName() + "." + field.getSubImportField())
            .collect(Collectors.toList());

    for (AdvancedImportFileField advancedImportFileField :
        advancedImportFileTab.getAdvancedImportFileFieldList()) {
      MetaField importField = advancedImportFileField.getImportField();

      if (importField != null
          && Strings.isNullOrEmpty(advancedImportFileField.getSubImportField())) {
        if (importField.getRelationship() != null) {
          logService.addLog(
              BaseExceptionMessage.ADVANCED_IMPORT_LOG_4, importField.getName(), line);
        }

        this.validateImportRequiredField(
            line,
            Class.forName(advancedImportFileTab.getMetaModel().getFullName()),
            importField.getName(),
            advancedImportFileField,
            null);

        this.validateDateField(line, advancedImportFileField);

      } else if (!Strings.isNullOrEmpty(advancedImportFileField.getSubImportField())) {

        Mapper mapper = advancedImportService.getMapper(importField.getMetaModel().getFullName());
        Property parentProp = mapper.getProperty(importField.getName());
        if (parentProp == null) {
          return;
        }

        Property subProperty =
            this.getAndValidateSubField(line, parentProp, advancedImportFileField, false);

        if (subProperty != null) {
          this.validateImportRequiredField(
              line,
              subProperty.getEntity(),
              subProperty.getName(),
              advancedImportFileField,
              relationalFieldList);

          this.validateDateField(line, advancedImportFileField);
        }
      }
    }
  }

  private void validateDateField(int line, AdvancedImportFileField advancedImportFileField)
      throws IOException {

    String type = advancedImportFileField.getTargetType();
    Integer rowNum = advancedImportFileField.getIsMatchWithFile() ? line : null;
    if (!Strings.isNullOrEmpty(type)
        && (type.equals(LOCAL_DATE)
            || type.equals(LOCAL_DATE_TIME)
            || type.equals(LOCAL_TIME)
            || type.equals(ZONED_DATE_TIME))) {

      String field = getField(advancedImportFileField);

      if (Strings.isNullOrEmpty(advancedImportFileField.getDateFormat())
          && Strings.isNullOrEmpty(advancedImportFileField.getExpression())) {

        logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_6, field, rowNum);
      }
    }
  }

  private void validateImportRequiredField(
      int line,
      Class<?> model,
      String fieldName,
      AdvancedImportFileField advancedImportFileField,
      List<String> relationalFieldList)
      throws IOException, ClassNotFoundException {

    Mapper mapper = advancedImportService.getMapper(model.getName());
    String field = getField(advancedImportFileField);

    Integer rowNum = advancedImportFileField.getIsMatchWithFile() ? line : null;
    int importType = advancedImportFileField.getImportType();

    for (Property prop : mapper.getProperties()) {
      if (prop.isRequired()) {
        if (prop.getName().equals(fieldName)
            && importType == AdvancedImportFileFieldRepository.IMPORT_TYPE_IGNORE_EMPTY) {

          logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_5, field, rowNum);

        } else if ((importType == AdvancedImportFileFieldRepository.IMPORT_TYPE_FIND_NEW
                || importType == AdvancedImportFileFieldRepository.IMPORT_TYPE_NEW)
            && field.contains(".")
            && !advancedImportFileField.getTargetType().equals("MetaFile")) {

          String newField = StringUtils.substringBeforeLast(field, ".");
          newField = newField + "." + prop.getName();

          if (!relationalFieldList.contains(newField)) {
            logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_3, newField, null);
          }
        }
      }
    }
  }

  private void validateData(
      String[] dataRow, int line, boolean isConfig, AdvancedImportFileTab advancedImportFileTab)
      throws IOException, ClassNotFoundException {

    Map<String, Object> map = isConfig ? fieldMap : titleMap;

    for (int fieldIndex = 0;
        fieldIndex < advancedImportFileTab.getAdvancedImportFileFieldList().size();
        fieldIndex++) {
      AdvancedImportFileField advancedImportFileField =
          advancedImportFileTab.getAdvancedImportFileFieldList().get(fieldIndex);

      if (!advancedImportFileField.getIsMatchWithFile()
          || !Strings.isNullOrEmpty(advancedImportFileField.getExpression())) {
        continue;
      }

      Mapper mapper = null;
      Property property = null;

      String key = null;
      if (isConfig) {
        key = this.getField(advancedImportFileField);
      } else {
        key = advancedImportFileField.getColumnTitle();
      }

      int cellIndex = 0;
      if (map.containsKey(key)) {
        cellIndex = (int) map.get(key);
      }

      cellIndex =
          (!isConfig && !advancedImportFileTab.getAdvancedImport().getIsHeader())
              ? fieldIndex
              : cellIndex;

      if (advancedImportFileField.getImportField() != null) {
        mapper =
            advancedImportService.getMapper(
                advancedImportFileField.getImportField().getMetaModel().getFullName());
        property = mapper.getProperty(advancedImportFileField.getImportField().getName());
      }

      if (property != null && Strings.isNullOrEmpty(advancedImportFileField.getSubImportField())) {
        if (this.validateDataRequiredField(
            dataRow,
            cellIndex,
            line,
            Class.forName(advancedImportFileTab.getMetaModel().getFullName()),
            property.getName(),
            advancedImportFileField)) {

          continue;
        }

        if (!Strings.isNullOrEmpty(property.getSelection())
            && advancedImportFileField.getForSelectUse()
                != AdvancedImportFileFieldRepository.SELECT_USE_VALUES) {
          continue;
        }

        this.validateDataType(
            dataRow,
            cellIndex,
            line,
            property.getJavaType().getSimpleName(),
            advancedImportFileField);

      } else if (!Strings.isNullOrEmpty(advancedImportFileField.getSubImportField())) {

        Property subProperty =
            this.getAndValidateSubField(line, property, advancedImportFileField, true);

        if (subProperty != null) {
          if (this.validateDataRequiredField(
              dataRow,
              cellIndex,
              line,
              subProperty.getEntity(),
              subProperty.getName(),
              advancedImportFileField)) {

            continue;
          }

          if (!Strings.isNullOrEmpty(subProperty.getSelection())
              && advancedImportFileField.getForSelectUse()
                  != AdvancedImportFileFieldRepository.SELECT_USE_VALUES) {
            continue;
          }

          this.validateDataType(
              dataRow,
              cellIndex,
              line,
              subProperty.getJavaType().getSimpleName(),
              advancedImportFileField);
        }
      }
    }
  }

  private boolean validateDataRequiredField(
      String row[],
      int cell,
      int line,
      Class<?> model,
      String fieldName,
      AdvancedImportFileField advancedImportFileField)
      throws IOException, ClassNotFoundException {

    boolean flag = false;
    String field = getField(advancedImportFileField);
    int importType = advancedImportFileField.getImportType();

    Mapper mapper = advancedImportService.getMapper(model.getName());
    Property prop = mapper.getProperty(fieldName);
    if (prop != null) {
      if (prop.isRequired()
          && Strings.isNullOrEmpty(row[cell])
          && importType != AdvancedImportFileFieldRepository.IMPORT_TYPE_FIND) {

        logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_8, field, line);

      } else if (importType == AdvancedImportFileFieldRepository.IMPORT_TYPE_IGNORE_EMPTY) {
        flag = true;
        return flag;
      }
    }
    return flag;
  }

  private Property getAndValidateSubField(
      int line, Property parentProp, AdvancedImportFileField advancedImportFileField, boolean isLog)
      throws IOException, ClassNotFoundException {

    String field = getField(advancedImportFileField);
    Integer rowNum = advancedImportFileField.getIsMatchWithFile() ? line : null;
    String[] subFields = advancedImportFileField.getSubImportField().split("\\.");
    return this.getAndValidateSubField(subFields, 0, rowNum, parentProp, field, isLog);
  }

  public Property getAndValidateSubField(
      String[] subFields,
      int index,
      Integer rowNum,
      Property parentProp,
      String field,
      boolean isLog)
      throws IOException, ClassNotFoundException {

    Property subProperty = null;

    if (parentProp.getTarget() != null) {
      Mapper mapper = advancedImportService.getMapper(parentProp.getTarget().getName());
      Property childProp = mapper.getProperty(subFields[index]);

      if (childProp == null) {
        if (!isLog) {
          logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_7, field, rowNum);
        }
      }

      if (childProp != null && childProp.getTarget() != null) {
        if (index != subFields.length - 1) {
          subProperty =
              this.getAndValidateSubField(subFields, index + 1, rowNum, childProp, field, isLog);
        } else {
          subProperty = childProp;
          if (!isLog) {
            logService.addLog(BaseExceptionMessage.ADVANCED_IMPORT_LOG_4, field, rowNum);
          }
        }
      } else {
        subProperty = childProp;
      }
    }
    return subProperty;
  }

  private void validateDataType(
      String[] row,
      int cell,
      int line,
      String type,
      AdvancedImportFileField advancedImportFileField)
      throws IOException {

    if (Strings.isNullOrEmpty(row[cell])) {
      return;
    }

    String field = getField(advancedImportFileField);
    String value = row[cell].trim();

    switch (type) {
      case INTEGER:
      case LONG:
      case BIG_DECIMAL:
        this.checkNumeric(value, line, field, type);
        break;

      case LOCAL_DATE:
      case ZONED_DATE_TIME:
      case LOCAL_DATE_TIME:
      case LOCAL_TIME:
        this.checkDateTime(value, line, type, advancedImportFileField);
        break;

      case BOOLEAN:
        String boolPat = "(true|false|1|0|no|yes|n|y)";

        if (!value.matches(boolPat)) {
          logService.addLog(
              BaseExceptionMessage.ADVANCED_IMPORT_LOG_9, field + "(" + type + ")", line);
        }
        break;

      default:
        if (!type.equals(STRING)) {
          try {
            new BigInteger(value);
          } catch (Exception e) {
            logService.addLog(
                BaseExceptionMessage.ADVANCED_IMPORT_LOG_9, field + "(" + type + ")", line);
          }
        }
        break;
    }
  }

  private void checkNumeric(String value, int line, String field, String type) throws IOException {

    switch (type) {
      case INTEGER:
        try {
          Integer.parseInt(value);
        } catch (NumberFormatException e) {
          logService.addLog(
              BaseExceptionMessage.ADVANCED_IMPORT_LOG_9, field + "(" + type + ")", line);
        }
        break;

      case LONG:
        try {
          Long.parseLong(value);
        } catch (NumberFormatException e) {
          logService.addLog(
              BaseExceptionMessage.ADVANCED_IMPORT_LOG_9, field + "(" + type + ")", line);
        }
        break;

      case BIG_DECIMAL:
        try {
          new BigDecimal(value);
        } catch (NumberFormatException e) {
          logService.addLog(
              BaseExceptionMessage.ADVANCED_IMPORT_LOG_9, field + "(" + type + ")", line);
        }
        break;
    }
  }

  private void checkDateTime(
      String value, int line, String type, AdvancedImportFileField advancedImportFileField)
      throws IOException {

    if (!Strings.isNullOrEmpty(advancedImportFileField.getDateFormat())
        && Strings.isNullOrEmpty(advancedImportFileField.getExpression())) {

      String pattern = advancedImportFileField.getDateFormat().trim();
      try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        switch (type) {
          case LOCAL_DATE:
            LocalDate.parse(value, formatter);
            break;

          case LOCAL_TIME:
            LocalTime.parse(value, formatter);
            break;

          case LOCAL_DATE_TIME:
            LocalDateTime.parse(value, formatter);
            break;

          case ZONED_DATE_TIME:
            ZonedDateTime.parse(value, formatter);
            break;
        }
      } catch (DateTimeParseException e) {
        logService.addLog(
            BaseExceptionMessage.ADVANCED_IMPORT_LOG_9,
            getField(advancedImportFileField) + "(" + type + ")",
            line);
      }
    }
  }

  private void validateActions(AdvancedImportFileTab advancedImportFileTab) {
    String actions = advancedImportFileTab.getActions();
    if (StringUtils.isBlank(actions)) {
      return;
    }
    if (!actionService.validate(actions)) {
      logService.addLog(
          LogService.COMMON_KEY,
          String.format(
              BaseExceptionMessage.ADVANCED_IMPORT_LOG_10, advancedImportFileTab.getName()),
          1);
    }
  }

  private void validateSearchCall(AdvancedImportFileTab advancedImportFileTab) {
    String searchCall = advancedImportFileTab.getSearchCall();
    if (!searchCallService.validate(searchCall)) {
      logService.addLog(
          LogService.COMMON_KEY,
          String.format(
              BaseExceptionMessage.ADVANCED_IMPORT_LOG_11, advancedImportFileTab.getName()),
          1);
    }
  }

  public String getField(AdvancedImportFileField advancedImportFileField) {
    String field =
        !Strings.isNullOrEmpty(advancedImportFileField.getSubImportField())
            ? advancedImportFileField.getImportField().getName()
                + "."
                + advancedImportFileField.getSubImportField().trim()
            : advancedImportFileField.getImportField().getName();

    return field;
  }

  public void sortFileTabList(List<AdvancedImportFileTab> advancedImportFileTabList) {
    advancedImportFileTabList.sort(Comparator.comparing(AdvancedImportFileTab::getSequence));
  }

  public void sortFileFieldList(List<AdvancedImportFileField> advancedImportFileFieldList) {
    advancedImportFileFieldList.sort(Comparator.comparing(AdvancedImportFileField::getSequence));
  }

  public void addLog(BufferedWriter writer, String errorType, String log) throws IOException {
    writer.write(I18n.get(errorType) + ":   " + log);
    writer.newLine();
  }

  @Transactional(rollbackOn = {Exception.class})
  public void createCustomObjectSet(String modelName, String targetModelName, int sequence) {

    String simpleModelName = StringUtils.substringAfterLast(targetModelName, ".");
    String fieldName = Inflector.getInstance().camelize(simpleModelName, true) + "Set";
    String viewName = Inflector.getInstance().dasherize(simpleModelName);

    if (metaJsonFieldRepo
            .all()
            .filter(
                "self.type = ?1 AND self.model = ?2 AND self.targetModel = ?3",
                MANY_TO_MANY,
                modelName,
                targetModelName)
            .count()
        > 0) {
      return;
    }

    MetaJsonField jsonField = new MetaJsonField();
    jsonField.setName(fieldName);
    jsonField.setType(MANY_TO_MANY);
    jsonField.setTitle(Inflector.getInstance().titleize(simpleModelName));
    jsonField.setSequence(sequence);
    jsonField.setModel(modelName);
    jsonField.setModelField("attrs");
    jsonField.setTargetModel(targetModelName);
    jsonField.setGridView(viewName + "-grid");
    jsonField.setFormView(viewName + "-form");
    jsonField.setWidgetAttrs("{\"colSpan\": \"12\"}");
    jsonField.setShowIf(fieldName + " != null && $record.advancedImport.statusSelect > 0");

    metaJsonFieldRepo.save(jsonField);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void createCustomButton(String modelName, String targetModelName, int sequence) {

    String simpleModelName = StringUtils.substringAfterLast(targetModelName, ".");
    String fieldName = Inflector.getInstance().camelize(simpleModelName, true) + "Set";
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
    jsonField.setTitle("Show " + Inflector.getInstance().titleize(simpleModelName));
    jsonField.setSequence(sequence);
    jsonField.setModel(modelName);
    jsonField.setModelField("attrs");
    jsonField.setOnClick("action-advanced-import-file-tab-method-show-record,close");
    jsonField.setWidgetAttrs("{\"colSpan\": \"4\"}");
    jsonField.setShowIf(fieldName + " != null && $record.advancedImport.statusSelect > 0");

    metaJsonFieldRepo.save(jsonField);
  }

  private void validateSearch(AdvancedImportFileTab advancedImportFileTab) throws AxelorException {
    if (advancedImportFileTab.getImportType()
        != AdvancedImportFileFieldRepository.IMPORT_TYPE_NEW) {
      if (CollectionUtils.isEmpty(advancedImportFileTab.getAdvancedImportSearchFieldSet())
          && StringUtils.isBlank(advancedImportFileTab.getSearchCall())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_6), advancedImportFileTab.getName()));
      }
      this.validateSearchCall(advancedImportFileTab);
    }
  }
}
