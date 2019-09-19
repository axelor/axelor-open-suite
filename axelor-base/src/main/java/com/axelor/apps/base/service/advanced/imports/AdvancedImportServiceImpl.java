/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.FileField;
import com.axelor.apps.base.db.FileTab;
import com.axelor.apps.base.db.repo.AdvancedImportRepository;
import com.axelor.apps.base.db.repo.FileFieldRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.readers.DataReaderFactory;
import com.axelor.apps.base.service.readers.DataReaderService;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class AdvancedImportServiceImpl implements AdvancedImportService {

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private MetaFieldRepository metaFieldRepo;

  @Inject private DataReaderFactory dataReaderFactory;

  @Inject private FileFieldService fileFieldService;

  @Inject private AdvancedImportRepository advancedImportRepository;

  @Inject private FileFieldRepository fileFieldRepository;

  private static final String forSelectUseValues = "values";
  private static final String forSelectUseTitles = "titles";
  private static final String forSelectUseTranslatedTitles = "translated titles";
  private List<String> searchFieldList;
  private boolean isTabWithoutConfig = false;

  @Override
  public boolean apply(AdvancedImport advancedImport)
      throws AxelorException, ClassNotFoundException {

    if (advancedImport.getImportFile() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.ADVANCED_IMPORT_NO_IMPORT_FILE));
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

    if (!CollectionUtils.isEmpty(advancedImport.getFileTabList())) {
      advancedImport.getFileTabList().stream().forEach(tab -> tab.clearFileFieldList());
      advancedImport.clearFileTabList();
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

      FileTab fileTab = new FileTab();
      fileTab.setName(sheet);
      fileTab.setSequence(fileTabSequence);
      fileTabSequence++;

      String[] objectRow = reader.read(sheet, startIndex, 0);
      if (objectRow == null) {
        isValid = false;
        break;
      }

      if (isConfig && isTabConfig) {
        tabConfigRowCount = getTabConfigRowCount(sheet, reader, totalLines, objectRow);
      }

      isValid = this.applyObject(objectRow, fileTab, isConfig, linesToIgnore, isTabConfig);
      if (!isValid) {
        break;
      }

      List<FileField> fileFieldList = new ArrayList<>();
      List<Integer> ignoreFields = new ArrayList<Integer>();

      for (int line = isConfig ? 1 : linesToIgnore; line < totalLines; line++) {
        String[] row = reader.read(sheet, line, isConfig ? 0 : objectRow.length);
        if (row == null) {
          continue;
        }

        if (isConfig) {
          this.applyWithConfig(
              row, line, fileFieldList, ignoreFields, fileTab, isTabConfig, tabConfigRowCount);
        } else {
          this.applyWithoutConfig(row, (line - linesToIgnore), fileFieldList, fileTab, isHeader);
        }
      }

      if (isConfig) {
        fileFieldList.removeIf(field -> field.getImportField() == null);
        if (!fileTab.getImportType().equals(FileFieldRepository.IMPORT_TYPE_NEW)) {
          fileTab = this.setSearchField(fileTab, searchFieldList, fileFieldList);
        }
      }
      advancedImport.addFileTabListItem(fileTab);
      advancedImportRepository.save(advancedImport);
    }
    return isValid;
  }

  private boolean applyObject(
      String[] row, FileTab fileTab, boolean isConfig, int linesToIgnore, boolean isTabConfig)
      throws AxelorException {
    int rowIndex = isConfig ? (isTabConfig ? 1 : 0) : 0;

    if (isTabConfig && row[0] != null) {
      rowIndex = 0;
      isTabWithoutConfig = true;
    }

    if (StringUtils.contains(row[rowIndex], "Object") && isConfig) {
      this.setFileTabConfig(row, fileTab, rowIndex);

    } else if ((StringUtils.containsIgnoreCase(row[0], "Object")
            || StringUtils.contains(row[1], "Object"))
        && !isConfig) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.ADVANCED_IMPORT_3));

    } else if (isConfig
        && (!StringUtils.containsIgnoreCase(row[0], "Object")
            && !StringUtils.contains(row[1], "Object"))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.ADVANCED_IMPORT_4));

    } else if (isConfig) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.ADVANCED_IMPORT_NO_OBJECT),
          fileTab.getName());
    }

    if ((StringUtils.isBlank(row[0]) && StringUtils.isBlank(row[1]) && linesToIgnore == 0)) {
      return false;
    }

    return true;
  }

  private void applyWithConfig(
      String[] row,
      int line,
      List<FileField> fileFieldList,
      List<Integer> ignoreFields,
      FileTab fileTab,
      Boolean isTabConfig,
      int tabConfigRowCount)
      throws AxelorException, ClassNotFoundException {

    Mapper mapper = getMapper(fileTab.getMetaModel().getFullName());
    FileField fileField = null;

    int index = 0;
    for (int i = 0; i < row.length; i++) {
      if (Strings.isNullOrEmpty(row[i])) {
        continue;
      }

      index = line;
      String value = row[i].trim();
      if (line == 1) {
        this.readFields(value, i, fileFieldList, ignoreFields, mapper, fileTab);
        continue;
      }

      if (ignoreFields.contains(i)) {
        continue;
      }

      if (fileFieldList.size() < i) {
        break;
      } else if (!isTabConfig && fileFieldList.size() <= i) {
        break;
      }

      if (row[0] == null) {
        fileField = fileFieldList.get(i - 1);
      } else if (!isTabConfig && row[0] != null) {
        fileField = fileFieldList.get(i);
      }

      if (isTabWithoutConfig) {
        fileField = fileFieldList.get(i);
      }

      if (line == 2) {
        fileField.setColumnTitle(value);
        continue;
      }

      if (isTabConfig && i > 0 && row[0] != null && line >= 3 && line <= 11) {
        fileField = fileFieldList.get(i - 1);
        this.setFileFieldConfig(row, i, fileField);
      }

      if (isTabConfig && i > 0 && row[0] == null) {
        line += -(tabConfigRowCount + 2);
        this.setSampleLines(line, value, fileField);
        line = index;
      } else if (!isTabConfig) {
        line += -2;
        this.setSampleLines(line, value, fileField);
        line = index;
      }
    }
  }

  @Transactional
  public void applyWithoutConfig(
      String[] row, int line, List<FileField> fileFieldList, FileTab fileTab, boolean isHeader)
      throws AxelorException {
    int index = 0;
    for (int i = 0; i < row.length; i++) {
      if (Strings.isNullOrEmpty(row[i])) {
        continue;
      }
      index = line;
      String value = row[i].trim();
      FileField fileField = null;

      if (line == 0) {
        fileField = new FileField();
        fileField.setIsMatchWithFile(true);
        fileFieldList.add(fileField);
        fileField.setFileTab(fileTab);

        if (isHeader) {
          fileField.setColumnTitle(value);
        } else {
          fileField.setFirstLine(value);
        }
        fileField.setSequence(i);
        fileField.setFullName(fileFieldService.computeFullName(fileField));
        fileField = fileFieldRepository.save(fileField);
        continue;
      }

      if (fileFieldList.size() <= i) {
        break;
      }

      if (!isHeader) {
        line += 1;
      }

      if (fileField == null) {
        fileField = fileFieldList.get(i);
      }

      setSampleLines(line, value, fileField);
      line = index;
    }
  }

  private void setSampleLines(int line, String value, FileField fileField) {
    if (!StringUtils.isBlank(fileField.getTargetType())
        && fileField.getTargetType().equals("String")
        && !StringUtils.isBlank(value)
        && value.length() > 255) {
      value = StringUtils.abbreviate(value, 255);
    }

    switch (line) {
      case 1:
        fileField.setFirstLine(value);
        break;
      case 2:
        fileField.setSecondLine(value);
        break;
      case 3:
        fileField.setThirdLine(value);
        break;
      default:
        break;
    }
  }

  @Transactional
  public void readFields(
      String value,
      int index,
      List<FileField> fileFieldList,
      List<Integer> ignoreFields,
      Mapper mapper,
      FileTab fileTab)
      throws AxelorException, ClassNotFoundException {

    FileField fileField = new FileField();
    fileField.setSequence(index);
    if (Strings.isNullOrEmpty(value)) {
      return;
    }

    String importType = StringUtils.substringBetween(value, "(", ")");

    if (!Strings.isNullOrEmpty(importType)
        && (importType.equalsIgnoreCase(forSelectUseValues)
            || importType.equalsIgnoreCase(forSelectUseTitles)
            || importType.equalsIgnoreCase(forSelectUseTranslatedTitles))) {
      fileField.setForSelectUse(this.getForStatusSelect(importType));
    } else {
      fileField.setImportType(this.getImportType(value, importType));
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
      this.setImportFields(mapper, fileField, importField, subImportField);
    } else {
      ignoreFields.add(index);
    }
    fileFieldList.add(fileField);
    fileField = fileFieldRepository.save(fileField);
    fileField.setFileTab(fileTab);
  }

  private boolean checkFields(Mapper mapper, String importField, String subImportField)
      throws AxelorException, ClassNotFoundException {

    if (importField != null) {
      Property parentProp = mapper.getProperty(importField);

      if (parentProp == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(IExceptionMessage.ADVANCED_IMPORT_1),
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
                I18n.get(IExceptionMessage.ADVANCED_IMPORT_5),
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
                  I18n.get(IExceptionMessage.ADVANCED_IMPORT_2),
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
                    I18n.get(IExceptionMessage.ADVANCED_IMPORT_5), subFields[index], model));
          }
        }
      }
    }
    return isValid;
  }

  private void setImportFields(
      Mapper mapper, FileField fileField, String importField, String subImportField) {

    Property prop = mapper.getProperty(importField);
    MetaField field =
        metaFieldRepo
            .all()
            .filter(
                "self.name = ?1 AND self.metaModel.name = ?2",
                prop.getName(),
                prop.getEntity().getSimpleName())
            .fetchOne();
    fileField.setImportField(field);
    fileField.setIsMatchWithFile(true);

    if (!Strings.isNullOrEmpty(subImportField)) {
      fileField.setSubImportField(subImportField);
    }

    fileField.setFullName(fileFieldService.computeFullName(fileField));
    fileField = fileFieldService.fillType(fileField);

    if (fileField.getTargetType().equals("MetaFile")) {
      fileField.setImportType(FileFieldRepository.IMPORT_TYPE_NEW);
    }
  }

  private int getImportType(String value, String importType) {

    if (Strings.isNullOrEmpty(importType)) {
      if (value.contains(".")) {
        return FileFieldRepository.IMPORT_TYPE_NEW;
      }
      return FileFieldRepository.IMPORT_TYPE_DIRECT;
    }

    switch (importType.toLowerCase()) {
      case "find":
        return FileFieldRepository.IMPORT_TYPE_FIND;

      case "findnew":
        return FileFieldRepository.IMPORT_TYPE_FIND_NEW;

      case "new":
        return FileFieldRepository.IMPORT_TYPE_NEW;

      case "ignore/empty":
        return FileFieldRepository.IMPORT_TYPE_IGNORE_EMPTY;

      default:
        if (value.contains(".")) {
          return FileFieldRepository.IMPORT_TYPE_NEW;
        }
        return FileFieldRepository.IMPORT_TYPE_DIRECT;
    }
  }

  private int getForStatusSelect(String importType) {

    switch (importType.toLowerCase()) {
      case forSelectUseTitles:
        return FileFieldRepository.SELECT_USE_TITLES;
      case forSelectUseValues:
        return FileFieldRepository.SELECT_USE_VALUES;
      case forSelectUseTranslatedTitles:
        return FileFieldRepository.SELECT_USE_TRANSLATED_TITLES;
      default:
        return FileFieldRepository.SELECT_USE_VALUES;
    }
  }

  protected void setFileTabConfig(String row[], FileTab fileTab, int rowIndex)
      throws AxelorException {

    searchFieldList = new ArrayList<String>();
    String objName = row[rowIndex].split("\\:")[1].trim();
    MetaModel model = metaModelRepo.findByName(objName);
    fileTab.setMetaModel(model);

    if (row.length >= 2 && StringUtils.containsIgnoreCase(row[rowIndex + 1], "importType")) {
      String type = row[rowIndex + 1].split("\\:")[1].trim();

      if (!type.equals(null)) {

        fileTab.setImportType(this.getImportType(null, type));

        if (!fileTab.getImportType().equals(FileFieldRepository.IMPORT_TYPE_NEW)) {
          if (StringUtils.containsIgnoreCase(row[rowIndex + 2], "searchFieldSet")) {
            String searchFieldtype = row[rowIndex + 2].split("\\:")[1].trim();
            String[] searchFieldArr = searchFieldtype.split("\\,");

            for (String search : searchFieldArr) {
              searchFieldList.add(search);
            }
          } else {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(IExceptionMessage.ADVANCED_IMPORT_6),
                fileTab.getName());
          }
        }
      }
    }
  }

  protected void setFileFieldConfig(String[] row, Integer i, FileField fileField) {

    String fieldName = row[0].trim();

    switch (fieldName.toLowerCase()) {
      case "forselectuse":
        fileField.setForSelectUse(this.getForStatusSelect(row[i]));
        break;

      case "noimportif":
        fileField.setNoImportIf(row[i]);
        break;

      case "expression":
        fileField.setExpression(row[i]);
        break;

      case "dateformat":
        fileField.setDateFormat(row[i]);
        break;

      case "importtype":
        fileField.setImportType(Integer.parseInt(row[i]));
        break;

      case "subimportfield":
        fileField.setSubImportField(row[i]);
        break;

      case "splitby":
        fileField.setSplitBy(row[i]);
        break;

      case "defaultifnotfound":
        fileField.setDefaultIfNotFound(row[i]);
        break;
    }
  }

  protected FileTab setSearchField(
      FileTab fileTab, List<String> searchFieldList, List<FileField> fileFieldList) {
    Set<FileField> searchFieldSet = new HashSet<FileField>();

    for (String searchField : searchFieldList) {
      for (FileField fileField : fileFieldList) {
        if (fileField.getFullName().endsWith("- " + searchField)) {
          searchFieldSet.add(fileField);
        }
      }
    }
    fileTab.setSearchFieldSet(searchFieldSet);
    return fileTab;
  }

  @Override
  public int getTabConfigRowCount(
      String sheet, DataReaderService reader, int totalLines, String[] objectRow) {

    int tabConfigRowCount = 0;
    if (objectRow[0] == null && StringUtils.containsIgnoreCase(objectRow[1], "Object")) {
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
}
