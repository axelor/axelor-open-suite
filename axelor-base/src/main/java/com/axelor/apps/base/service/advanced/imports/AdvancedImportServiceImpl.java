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

import com.axelor.apps.base.db.AdvancedImport;
import com.axelor.apps.base.db.AdvancedImportFileField;
import com.axelor.apps.base.db.AdvancedImportFileTab;
import com.axelor.apps.base.db.repo.AdvancedImportFileFieldRepository;
import com.axelor.apps.base.db.repo.AdvancedImportRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.tool.reader.DataReaderFactory;
import com.axelor.apps.tool.reader.DataReaderService;
import com.axelor.common.Inflector;
import com.axelor.db.EntityHelper;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.rpc.JsonContext;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedImportServiceImpl implements AdvancedImportService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String forSelectUseValues = "values";
  private static final String forSelectUseTitles = "titles";
  private static final String forSelectUseTranslatedTitles = "translated titles";

  private Inflector inflector = Inflector.getInstance();

  private List<String> searchFieldList = new ArrayList<>();

  private boolean isTabWithoutConfig = false;

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private MetaFieldRepository metaFieldRepo;

  @Inject private DataReaderFactory dataReaderFactory;

  @Inject private AdvancedImportFileFieldService advancedImportFileFieldService;

  @Inject private AdvancedImportRepository advancedImportRepository;

  @Inject private AdvancedImportFileFieldRepository advancedImportFileFieldRepository;

  @Inject private DataImportService dataImportService;

  @Override
  public boolean apply(AdvancedImport advancedImport)
      throws AxelorException, ClassNotFoundException {

    if (advancedImport.getImportFile() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_NO_IMPORT_FILE));
    }

    String extension = Files.getFileExtension(advancedImport.getImportFile().getFileName());
    if (extension == null
        || (!extension.equals("xlsx") && !extension.equals("xls") && !extension.equals("csv"))) {
      return false;
    }

    DataReaderService reader = dataReaderFactory.getDataReader(extension);
    reader.initialize(advancedImport.getImportFile(), advancedImport.getFileSeparator());

    return this.process(reader, advancedImport);
  }

  @Transactional
  public boolean process(DataReaderService reader, AdvancedImport advancedImport)
      throws AxelorException, ClassNotFoundException {

    boolean isValid = true;

    if (!CollectionUtils.isEmpty(advancedImport.getAdvancedImportFileTabList())) {
      advancedImport.getAdvancedImportFileTabList().stream()
          .forEach(tab -> tab.clearAdvancedImportFileFieldList());
      advancedImport.clearAdvancedImportFileTabList();
    }

    String[] sheets = reader.getSheetNames();
    boolean isConfig = advancedImport.getIsConfigInFile();
    boolean isTabConfig = advancedImport.getIsFileTabConfigAdded();
    boolean isHeader = advancedImport.getIsHeader();
    int linesToIgnore = advancedImport.getNbOfFirstLineIgnore();
    int tabConfigRowCount = 0;
    int startIndex = isConfig ? 0 : linesToIgnore;
    int fileTabSequence = 1;

    for (String sheet : sheets) {
      int totalLines = reader.getTotalLines(sheet);
      if (totalLines == 0) {
        continue;
      }

      AdvancedImportFileTab advancedImportFileTab = new AdvancedImportFileTab();
      advancedImportFileTab.setName(sheet);
      advancedImportFileTab.setSequence(fileTabSequence);
      fileTabSequence++;

      String[] objectRow = reader.read(sheet, startIndex, 0);
      if (objectRow == null) {
        isValid = false;
        break;
      }

      if (isConfig && isTabConfig) {
        tabConfigRowCount = getTabConfigRowCount(sheet, reader, totalLines, objectRow);
      }

      isValid =
          this.applyObject(objectRow, advancedImportFileTab, isConfig, linesToIgnore, isTabConfig);
      if (!isValid) {
        break;
      }

      List<AdvancedImportFileField> advancedImportFileFieldList = new ArrayList<>();
      List<Integer> ignoreFields = new ArrayList<Integer>();

      for (int line = isConfig ? 1 : linesToIgnore; line < totalLines; line++) {
        String[] row = reader.read(sheet, line, isConfig ? 0 : objectRow.length);
        if (row == null) {
          continue;
        }

        if (isConfig) {
          this.applyWithConfig(
              row,
              line,
              advancedImportFileFieldList,
              ignoreFields,
              advancedImportFileTab,
              isTabConfig,
              tabConfigRowCount);
        } else {
          this.applyWithoutConfig(
              row,
              (line - linesToIgnore),
              advancedImportFileFieldList,
              advancedImportFileTab,
              isHeader);
        }
      }

      if (isConfig) {
        advancedImportFileFieldList.removeIf(field -> field.getImportField() == null);
        if (!advancedImportFileTab
            .getImportType()
            .equals(AdvancedImportFileFieldRepository.IMPORT_TYPE_NEW)) {
          advancedImportFileTab =
              this.setSearchField(
                  advancedImportFileTab, searchFieldList, advancedImportFileFieldList);
        }
      }
      advancedImport.addAdvancedImportFileTabListItem(advancedImportFileTab);
      advancedImportRepository.save(advancedImport);
    }
    return isValid;
  }

  private boolean applyObject(
      String[] row,
      AdvancedImportFileTab advancedImportFileTab,
      boolean isConfig,
      int linesToIgnore,
      boolean isTabConfig)
      throws AxelorException {
    int rowIndex = isConfig ? (isTabConfig ? 1 : 0) : 0;

    if (isTabConfig && row[0] != null) {
      rowIndex = 0;
      isTabWithoutConfig = true;
    }

    if (StringUtils.contains(row[rowIndex], "Object") && isConfig) {
      this.setFileTabConfig(row, advancedImportFileTab, rowIndex);

    } else if ((StringUtils.containsIgnoreCase(row[0], "Object")
            || (row.length > 1 && StringUtils.contains(row[1], "Object")))
        && !isConfig) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_3));

    } else if (isConfig
        && (!StringUtils.containsIgnoreCase(row[0], "Object")
            && (row.length > 1 && !StringUtils.contains(row[1], "Object")))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_4));

    } else if (isConfig) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_NO_OBJECT),
          advancedImportFileTab.getName());
    }

    if ((StringUtils.isBlank(row[0])
        && (row.length > 1 && StringUtils.isBlank(row[1]))
        && linesToIgnore == 0)) {
      return false;
    }

    return true;
  }

  private void applyWithConfig(
      String[] row,
      int line,
      List<AdvancedImportFileField> advancedImportFileFieldList,
      List<Integer> ignoreFields,
      AdvancedImportFileTab advancedImportFileTab,
      Boolean isTabConfig,
      int tabConfigRowCount)
      throws AxelorException, ClassNotFoundException {

    Mapper mapper = getMapper(advancedImportFileTab.getMetaModel().getFullName());
    AdvancedImportFileField advancedImportFileField = null;

    int index = 0;
    for (int i = 0; i < row.length; i++) {
      if (Strings.isNullOrEmpty(row[i])) {
        continue;
      }

      index = line;
      String value = row[i].trim();
      if (line == 1) {
        this.readFields(
            value, i, advancedImportFileFieldList, ignoreFields, mapper, advancedImportFileTab);
        continue;
      }

      if (ignoreFields.contains(i)) {
        continue;
      }

      if (advancedImportFileFieldList.size() < i) {
        break;
      } else if (!isTabConfig && advancedImportFileFieldList.size() <= i) {
        break;
      }

      if (row[0] == null) {
        advancedImportFileField = advancedImportFileFieldList.get(i - 1);
      } else if (!isTabConfig && row[0] != null) {
        advancedImportFileField = advancedImportFileFieldList.get(i);
      }

      if (isTabWithoutConfig) {
        advancedImportFileField = advancedImportFileFieldList.get(i);
      }

      if (line == 2) {
        advancedImportFileField.setColumnTitle(value);
        continue;
      }

      if (isTabConfig && i > 0 && row[0] != null && line >= 3 && line <= 11) {
        advancedImportFileField = advancedImportFileFieldList.get(i - 1);
        this.setFileFieldConfig(row, i, advancedImportFileField);
      }

      if (isTabConfig && i > 0 && row[0] == null) {
        line += -(tabConfigRowCount + 2);
        this.setSampleLines(line, value, advancedImportFileField);
        line = index;
      } else if (!isTabConfig) {
        line += -2;
        this.setSampleLines(line, value, advancedImportFileField);
        line = index;
      }
    }
  }

  @Transactional
  public void applyWithoutConfig(
      String[] row,
      int line,
      List<AdvancedImportFileField> advancedImportFileFieldList,
      AdvancedImportFileTab advancedImportFileTab,
      boolean isHeader)
      throws AxelorException {
    int index = 0;
    for (int i = 0; i < row.length; i++) {
      if (Strings.isNullOrEmpty(row[i])) {
        continue;
      }
      index = line;
      String value = row[i].trim();
      AdvancedImportFileField advancedImportFileField = null;

      if (line == 0) {
        advancedImportFileField = new AdvancedImportFileField();
        advancedImportFileField.setIsMatchWithFile(true);
        advancedImportFileFieldList.add(advancedImportFileField);
        advancedImportFileField.setAdvancedImportFileTab(advancedImportFileTab);

        if (isHeader) {
          advancedImportFileField.setColumnTitle(value);
        } else {
          advancedImportFileField.setFirstLine(value);
        }
        advancedImportFileField.setSequence(i);
        advancedImportFileField.setFullName(
            advancedImportFileFieldService.computeFullName(advancedImportFileField));
        advancedImportFileField = advancedImportFileFieldRepository.save(advancedImportFileField);
        continue;
      }

      if (advancedImportFileFieldList.size() <= i) {
        break;
      }

      if (!isHeader) {
        line += 1;
      }

      if (advancedImportFileField == null) {
        advancedImportFileField = advancedImportFileFieldList.get(i);
      }

      setSampleLines(line, value, advancedImportFileField);
      line = index;
    }
  }

  private void setSampleLines(
      int line, String value, AdvancedImportFileField advancedImportFileField) {
    if (!StringUtils.isBlank(advancedImportFileField.getTargetType())
        && advancedImportFileField.getTargetType().equals("String")
        && !StringUtils.isBlank(value)
        && value.length() > 255) {
      value = StringUtils.abbreviate(value, 255);
    }

    switch (line) {
      case 1:
        advancedImportFileField.setFirstLine(value);
        break;
      case 2:
        advancedImportFileField.setSecondLine(value);
        break;
      case 3:
        advancedImportFileField.setThirdLine(value);
        break;
      default:
        break;
    }
  }

  @Transactional
  public void readFields(
      String value,
      int index,
      List<AdvancedImportFileField> advancedImportFileFieldList,
      List<Integer> ignoreFields,
      Mapper mapper,
      AdvancedImportFileTab advancedImportFileTab)
      throws AxelorException, ClassNotFoundException {

    AdvancedImportFileField advancedImportFileField = new AdvancedImportFileField();
    advancedImportFileField.setSequence(index);
    if (Strings.isNullOrEmpty(value)) {
      return;
    }

    String importType = StringUtils.substringBetween(value, "(", ")");

    if (!Strings.isNullOrEmpty(importType)
        && (importType.equalsIgnoreCase(forSelectUseValues)
            || importType.equalsIgnoreCase(forSelectUseTitles)
            || importType.equalsIgnoreCase(forSelectUseTranslatedTitles))) {
      advancedImportFileField.setForSelectUse(this.getForStatusSelect(importType));
    } else {
      advancedImportFileField.setImportType(this.getImportType(value, importType));
    }

    value = value.split("\\(")[0];
    String importField = null;
    String subImportField = null;
    if (value.contains(".")) {
      importField = value.substring(0, value.indexOf("."));
      subImportField = value.substring(value.indexOf(".") + 1, value.length());
    } else {
      importField = value;
    }

    boolean isValid = this.checkFields(mapper, importField, subImportField);

    if (isValid) {
      this.setImportFields(mapper, advancedImportFileField, importField, subImportField);
    } else {
      ignoreFields.add(index);
    }
    advancedImportFileFieldList.add(advancedImportFileField);
    advancedImportFileField = advancedImportFileFieldRepository.save(advancedImportFileField);
    advancedImportFileTab.addAdvancedImportFileFieldListItem(advancedImportFileField);
  }

  private boolean checkFields(Mapper mapper, String importField, String subImportField)
      throws AxelorException, ClassNotFoundException {

    if (importField != null) {
      Property parentProp = mapper.getProperty(importField);

      if (parentProp == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_1),
                importField,
                mapper.getBeanClass().getSimpleName()));
      }

      if (parentProp.getType().name().equals("ONE_TO_MANY")) {
        return false;
      }

      if (!Strings.isNullOrEmpty(subImportField)) {
        String[] subFields = subImportField.split("\\.");
        return this.checkSubFields(
            subFields, 0, parentProp, parentProp.getEntity().getSimpleName());

      } else if (parentProp.getTarget() != null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_5),
                importField,
                mapper.getBeanClass().getSimpleName()));
      }
    }
    return true;
  }

  private boolean checkSubFields(String[] subFields, int index, Property parentProp, String model)
      throws AxelorException, ClassNotFoundException {
    boolean isValid = true;

    if (index < subFields.length) {
      if (parentProp.getTarget() != null) {
        Mapper mapper = getMapper(parentProp.getTarget().getName());
        Property childProp = mapper.getProperty(subFields[index]);

        if (childProp == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              String.format(
                  I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_2),
                  subFields[index],
                  parentProp.getName(),
                  model));
        }
        if (childProp.getTarget() != null) {
          if (childProp.getType().name().equals("ONE_TO_MANY")) {
            isValid = false;
            return isValid;
          }
          if (index != subFields.length - 1) {
            isValid = this.checkSubFields(subFields, index + 1, childProp, model);
          } else {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                String.format(
                    I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_5), subFields[index], model));
          }
        }
      }
    }
    return isValid;
  }

  private void setImportFields(
      Mapper mapper,
      AdvancedImportFileField advancedImportFileField,
      String importField,
      String subImportField) {

    Property prop = mapper.getProperty(importField);
    MetaField field =
        metaFieldRepo
            .all()
            .filter(
                "self.name = ?1 AND self.metaModel.name = ?2",
                prop.getName(),
                prop.getEntity().getSimpleName())
            .fetchOne();
    advancedImportFileField.setImportField(field);
    advancedImportFileField.setIsMatchWithFile(true);

    if (!Strings.isNullOrEmpty(subImportField)) {
      advancedImportFileField.setSubImportField(subImportField);
    }

    advancedImportFileField.setFullName(
        advancedImportFileFieldService.computeFullName(advancedImportFileField));
    advancedImportFileField = advancedImportFileFieldService.fillType(advancedImportFileField);

    if (advancedImportFileField.getTargetType().equals("MetaFile")) {
      advancedImportFileField.setImportType(AdvancedImportFileFieldRepository.IMPORT_TYPE_NEW);
    }
  }

  private int getImportType(String value, String importType) {

    if (Strings.isNullOrEmpty(importType)) {
      if (value.contains(".")) {
        return AdvancedImportFileFieldRepository.IMPORT_TYPE_NEW;
      }
      return AdvancedImportFileFieldRepository.IMPORT_TYPE_DIRECT;
    }

    switch (importType.toLowerCase()) {
      case "find":
        return AdvancedImportFileFieldRepository.IMPORT_TYPE_FIND;

      case "findnew":
        return AdvancedImportFileFieldRepository.IMPORT_TYPE_FIND_NEW;

      case "new":
        return AdvancedImportFileFieldRepository.IMPORT_TYPE_NEW;

      case "ignore/empty":
        return AdvancedImportFileFieldRepository.IMPORT_TYPE_IGNORE_EMPTY;

      default:
        if (value.contains(".")) {
          return AdvancedImportFileFieldRepository.IMPORT_TYPE_NEW;
        }
        return AdvancedImportFileFieldRepository.IMPORT_TYPE_DIRECT;
    }
  }

  private int getForStatusSelect(String importType) {

    switch (importType.toLowerCase()) {
      case forSelectUseTitles:
        return AdvancedImportFileFieldRepository.SELECT_USE_TITLES;
      case forSelectUseValues:
        return AdvancedImportFileFieldRepository.SELECT_USE_VALUES;
      case forSelectUseTranslatedTitles:
        return AdvancedImportFileFieldRepository.SELECT_USE_TRANSLATED_TITLES;
      default:
        return AdvancedImportFileFieldRepository.SELECT_USE_VALUES;
    }
  }

  protected void setFileTabConfig(
      String row[], AdvancedImportFileTab advancedImportFileTab, int rowIndex)
      throws AxelorException {

    final String KEY_OBJECT = "object";
    final String KEY_IMPORT_TYPE = "importtype";
    final String KEY_SEARCH_FIELD_SET = "searchfieldset";
    final String KEY_ACTIONS = "actions";
    final String KEY_SEARCH_CALL = "search-call";
    final List<String> KEY_LIST =
        Arrays.asList(
            KEY_IMPORT_TYPE, KEY_OBJECT, KEY_SEARCH_FIELD_SET, KEY_ACTIONS, KEY_SEARCH_CALL);
    Map<String, String> tabConfigDataMap = getTabConfigDataMap(row, KEY_LIST);

    MetaModel model = metaModelRepo.findByName(tabConfigDataMap.get(KEY_OBJECT));
    advancedImportFileTab.setMetaModel(model);

    if (tabConfigDataMap.containsKey(KEY_IMPORT_TYPE)) {

      Integer importType = this.getImportType(null, tabConfigDataMap.get(KEY_IMPORT_TYPE));
      advancedImportFileTab.setImportType(importType);

      if (importType != AdvancedImportFileFieldRepository.IMPORT_TYPE_NEW) {
        if (!tabConfigDataMap.containsKey(KEY_SEARCH_FIELD_SET)
            && !tabConfigDataMap.containsKey(KEY_SEARCH_CALL)) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_6),
              advancedImportFileTab.getName());
        }
        if (tabConfigDataMap.containsKey(KEY_SEARCH_CALL)) {
          advancedImportFileTab.setSearchCall(tabConfigDataMap.get(KEY_SEARCH_CALL));
        } else {
          searchFieldList = Arrays.asList(tabConfigDataMap.get(KEY_SEARCH_FIELD_SET).split("\\,"));
        }
      }
    }

    advancedImportFileTab.setActions(tabConfigDataMap.get(KEY_ACTIONS));
  }

  protected void setFileFieldConfig(
      String[] row, Integer i, AdvancedImportFileField advancedImportFileField) {

    String fieldName = row[0].trim();

    switch (fieldName.toLowerCase()) {
      case "forselectuse":
        advancedImportFileField.setForSelectUse(this.getForStatusSelect(row[i]));
        break;

      case "noimportif":
        advancedImportFileField.setNoImportIf(row[i]);
        break;

      case "expression":
        advancedImportFileField.setExpression(row[i]);
        break;

      case "dateformat":
        advancedImportFileField.setDateFormat(row[i]);
        break;

      case "importtype":
        advancedImportFileField.setImportType(Integer.parseInt(row[i]));
        break;

      case "subimportfield":
        advancedImportFileField.setSubImportField(row[i]);
        break;

      case "splitby":
        advancedImportFileField.setSplitBy(row[i]);
        break;

      case "defaultifnotfound":
        advancedImportFileField.setDefaultIfNotFound(row[i]);
        break;
    }
  }

  protected AdvancedImportFileTab setSearchField(
      AdvancedImportFileTab advancedImportFileTab,
      List<String> searchFieldList,
      List<AdvancedImportFileField> advancedImportFileFieldList) {
    Set<AdvancedImportFileField> advancedImportSearchFieldSet =
        new HashSet<AdvancedImportFileField>();

    for (String searchField : searchFieldList) {
      for (AdvancedImportFileField advancedImportFileField : advancedImportFileFieldList) {
        if (advancedImportFileField.getFullName().endsWith("- " + searchField)) {
          advancedImportSearchFieldSet.add(advancedImportFileField);
        }
      }
    }
    advancedImportFileTab.setAdvancedImportSearchFieldSet(advancedImportSearchFieldSet);
    return advancedImportFileTab;
  }

  @Override
  public int getTabConfigRowCount(
      String sheet, DataReaderService reader, int totalLines, String[] objectRow) {

    int tabConfigRowCount = 0;
    if (objectRow[0] == null
        && (objectRow.length > 1 && StringUtils.containsIgnoreCase(objectRow[1], "Object"))) {
      int linesForTab;
      for (linesForTab = 3; linesForTab < totalLines; linesForTab++) {
        if (reader.read(sheet, linesForTab, 0)[0] != null) {
          tabConfigRowCount++;
        } else {
          break;
        }
      }
    }
    return tabConfigRowCount;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Mapper getMapper(String modelFullName) throws ClassNotFoundException {
    Class<? extends Model> klass = (Class<? extends Model>) Class.forName(modelFullName);
    return Mapper.of(klass);
  }

  @Override
  public boolean resetImport(AdvancedImport advancedImport) throws ClassNotFoundException {
    List<AdvancedImportFileTab> advancedImportFileTabList =
        advancedImport.getAdvancedImportFileTabList();
    Beans.get(ValidatorService.class).sortFileTabList(advancedImportFileTabList);

    boolean isResetValue = resetRelationalFields(advancedImportFileTabList);
    if (isResetValue) {
      removeRecords(advancedImportFileTabList);
    }
    return isResetValue;
  }

  @SuppressWarnings("unchecked")
  public boolean resetRelationalFields(List<AdvancedImportFileTab> advancedImportFileTabList)
      throws ClassNotFoundException {

    boolean isResetValue = false;

    for (AdvancedImportFileTab advancedImportFileTab : advancedImportFileTabList) {

      Map<String, Object> jsonContextMap =
          dataImportService.createJsonContext(advancedImportFileTab);
      JsonContext jsonContext = (JsonContext) jsonContextMap.get("jsonContext");
      String fieldName =
          inflector.camelize(advancedImportFileTab.getMetaModel().getName(), true) + "Set";

      List<Object> recordList = (List<Object>) jsonContext.get(fieldName);
      if (CollectionUtils.isEmpty(recordList)) {
        continue;
      }
      isResetValue = true;

      Class<? extends Model> modelKlass =
          (Class<? extends Model>)
              Class.forName(advancedImportFileTab.getMetaModel().getFullName());
      this.resetPropertyValue(modelKlass, recordList);
      this.resetSubPropertyValue(modelKlass, jsonContext);
    }
    return isResetValue;
  }

  @Transactional
  private void resetPropertyValue(Class<? extends Model> klass, List<Object> recordList)
      throws ClassNotFoundException {

    JpaRepository<? extends Model> modelRepo = JpaRepository.of(klass);

    for (Property prop : Mapper.of(klass).getProperties()) {
      if (prop.getTarget() == null || prop.isRequired()) {
        continue;
      }

      for (Object obj : recordList) {
        Map<String, Object> recordMap = Mapper.toMap(EntityHelper.getEntity(obj));
        Long id = Long.valueOf(recordMap.get("id").toString());
        Object bean = modelRepo.find(id);
        if (bean != null) {
          prop.set(bean, null);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void resetSubPropertyValue(Class<? extends Model> klass, JsonContext jsonContext)
      throws ClassNotFoundException {

    for (Property prop : Mapper.of(klass).getProperties()) {
      if (prop.getTarget() == null || prop.isRequired()) {
        continue;
      }

      String simpleModelName = StringUtils.substringAfterLast(prop.getTarget().getName(), ".");
      String field = inflector.camelize(simpleModelName, true) + "Set";

      if (!jsonContext.containsKey(field)) {
        continue;
      }

      List<Object> recordList = (List<Object>) jsonContext.get(field);

      Class<? extends Model> modelKlass =
          (Class<? extends Model>) Class.forName(prop.getTarget().getName());

      this.resetPropertyValue(modelKlass, recordList);
    }
  }

  @SuppressWarnings("unchecked")
  public void removeRecords(List<AdvancedImportFileTab> advancedImportFileTabList)
      throws ClassNotFoundException {

    for (AdvancedImportFileTab advancedImportFileTab : advancedImportFileTabList) {

      String targetModelName = advancedImportFileTab.getMetaModel().getFullName();

      Map<String, Object> jsonContextMap =
          dataImportService.createJsonContext(advancedImportFileTab);
      JsonContext jsonContext = (JsonContext) jsonContextMap.get("jsonContext");
      String fieldName =
          inflector.camelize(advancedImportFileTab.getMetaModel().getName(), true) + "Set";

      List<Object> recordList = (List<Object>) jsonContext.get(fieldName);
      if (CollectionUtils.isEmpty(recordList)) {
        continue;
      }

      Class<? extends Model> modelKlass = (Class<? extends Model>) Class.forName(targetModelName);
      removeRecord(advancedImportFileTab, modelKlass, recordList, advancedImportFileTabList);
      removeSubRecords(modelKlass, jsonContext);
    }
  }

  @SuppressWarnings("unchecked")
  @Transactional
  public void removeRecord(
      AdvancedImportFileTab advancedImportFileTab,
      Class<? extends Model> modelKlass,
      List<Object> recordList,
      List<AdvancedImportFileTab> advancedImportFileTabList)
      throws ClassNotFoundException {

    JpaRepository<? extends Model> modelRepo = JpaRepository.of(modelKlass);

    for (AdvancedImportFileTab tab : advancedImportFileTabList) {

      Map<String, Object> jsonContextMap = dataImportService.createJsonContext(tab);
      JsonContext jsonContext = (JsonContext) jsonContextMap.get("jsonContext");
      String fieldName = inflector.camelize(tab.getMetaModel().getName(), true) + "Set";

      List<Object> recList = (List<Object>) jsonContext.get(fieldName);
      if (CollectionUtils.isEmpty(recList)) {
        continue;
      }

      Class<? extends Model> klass =
          (Class<? extends Model>) Class.forName(tab.getMetaModel().getFullName());

      Property[] props = Mapper.of(klass).getProperties();

      for (Property prop : props) {
        if (prop.getTarget() != null && prop.getTarget() == modelKlass && prop.isRequired()) {
          removeRecord(tab, klass, recList, advancedImportFileTabList);
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
    advancedImportFileTab.setAttrs(null);

    LOG.debug("Reset imported data : {}", modelKlass.getSimpleName());
  }

  @SuppressWarnings("unchecked")
  @Transactional
  public void removeSubRecords(Class<? extends Model> klass, JsonContext jsonContext)
      throws ClassNotFoundException {

    for (Property prop : Mapper.of(klass).getProperties()) {
      if (prop.getTarget() == null || prop.isCollection()) {
        continue;
      }

      String simpleModelName = StringUtils.substringAfterLast(prop.getTarget().getName(), ".");
      String field = inflector.camelize(simpleModelName, true) + "Set";

      if (!jsonContext.containsKey(field)) {
        continue;
      }

      List<Object> recList = (List<Object>) jsonContext.get(field);

      String ids =
          recList.stream()
              .map(
                  obj -> {
                    Map<String, Object> recordMap = Mapper.toMap(EntityHelper.getEntity(obj));
                    return recordMap.get("id").toString();
                  })
              .collect(Collectors.joining(","));

      JpaRepository<? extends Model> modelRepo =
          JpaRepository.of((Class<? extends Model>) Class.forName(prop.getTarget().getName()));

      modelRepo.all().filter("self.id IN (" + ids + ")").delete();
    }
  }

  protected Map<String, String> getTabConfigDataMap(String row[], List<String> keys) {
    Map<String, String> tabConfigDataMap = new HashMap<>();
    for (String data : row) {
      if (StringUtils.isBlank(data)) {
        continue;
      }

      String[] keyValue = data.split("\\:", 2);
      if (keyValue.length != 2) {
        continue;
      }
      String key = keyValue[0].replaceAll("(^\\h*)|(\\h*$)", "").trim().toLowerCase();
      if (keys.contains(key)) {
        tabConfigDataMap.put(key, keyValue[1].trim());
      }
    }
    return tabConfigDataMap;
  }
}
