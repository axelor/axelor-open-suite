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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class AdvancedImportServiceImpl implements AdvancedImportService {

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private MetaFieldRepository metaFieldRepo;

  @Inject private DataReaderFactory dataReaderFactory;

  @Inject private FileFieldService fileFieldService;

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

  private boolean process(DataReaderService reader, AdvancedImport advancedImport)
      throws AxelorException, ClassNotFoundException {

    boolean isValid = true;

    if (!CollectionUtils.isEmpty(advancedImport.getFileTabList())) {
      advancedImport.getFileTabList().stream().forEach(tab -> tab.clearFileFieldList());
      advancedImport.clearFileTabList();
    }

    String[] sheets = reader.getSheetNames();
    boolean isConfig = advancedImport.getIsConfigInFile();
    boolean isHeader = advancedImport.getIsHeader();
    int linesToIgnore = advancedImport.getNbOfFirstLineIgnore();
    int startIndex = isConfig ? 0 : linesToIgnore;

    for (String sheet : sheets) {
      int totalLines = reader.getTotalLines(sheet);
      if (totalLines == 0) {
        continue;
      }

      FileTab fileTab = new FileTab();
      fileTab.setName(sheet);

      String[] objectRow = reader.read(sheet, startIndex, 0);
      if (objectRow == null) {
        isValid = false;
        break;
      }
      isValid = this.applyObject(objectRow, fileTab, isConfig, linesToIgnore);
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
          this.applyWithConfig(row, line, fileFieldList, ignoreFields, fileTab);
        } else {
          this.applyWithoutConfig(row, (line - linesToIgnore), fileFieldList, isHeader);
        }
      }

      if (isConfig) {
        fileFieldList.removeIf(field -> field.getImportField() == null);
      }

      fileTab.setFileFieldList(fileFieldList);
      advancedImport.addFileTabListItem(fileTab);
    }
    return isValid;
  }

  private boolean applyObject(String[] row, FileTab fileTab, boolean isConfig, int linesToIgnore)
      throws AxelorException {

    if (isConfig) {
      if (StringUtils.containsIgnoreCase(row[0], "Object")) {
        String objName = row[0].split("\\:")[1].trim();
        MetaModel model = metaModelRepo.findByName(objName);
        fileTab.setMetaModel(model);
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.ADVANCED_IMPORT_4));
      }
    } else {
      if (Strings.isNullOrEmpty(row[0]) && linesToIgnore == 0) {
        return false;
      }

      if (StringUtils.containsIgnoreCase(row[0], "Object")) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.ADVANCED_IMPORT_3));
      }
    }
    return true;
  }

  private void applyWithConfig(
      String[] row,
      int line,
      List<FileField> fileFieldList,
      List<Integer> ignoreFields,
      FileTab fileTab)
      throws AxelorException, ClassNotFoundException {

    Mapper mapper = getMapper(fileTab.getMetaModel().getFullName());

    int index = 0;
    for (int i = 0; i < row.length; i++) {
      index = line;
      if (line == 1) {
        this.readFields(row, i, fileFieldList, ignoreFields, mapper);
        continue;
      }

      if (ignoreFields.contains(i)) {
        continue;
      }

      if (fileFieldList.size() <= i) {
        break;
      }

      FileField fileField = fileFieldList.get(i);
      if (line == 2) {
        fileField.setColumnTitle(row[i]);
        continue;
      }

      line += -2;
      this.setSampleLines(line, row[i], fileField);
      line = index;
    }
  }

  private void applyWithoutConfig(
      String[] row, int line, List<FileField> fileFieldList, boolean isHeader)
      throws AxelorException {

    int index = 0;
    for (int i = 0; i < row.length; i++) {
      index = line;
      String value = row[i];
      FileField fileField = null;

      if (line == 0) {
        fileField = new FileField();
        fileField.setIsMatchWithFile(true);
        fileFieldList.add(fileField);

        if (isHeader) {
          fileField.setColumnTitle(value);
        } else {
          fileField.setFirstLine(value);
        }
        fileField.setSequence(i);
        fileField.setFullName(fileFieldService.computeFullName(fileField));
        continue;
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

  private void readFields(
      String[] row,
      int index,
      List<FileField> fileFieldList,
      List<Integer> ignoreFields,
      Mapper mapper)
      throws AxelorException, ClassNotFoundException {

    FileField fileField = new FileField();
    fileField.setSequence(index);
    String value = row[index];
    if (Strings.isNullOrEmpty(value)) {
      return;
    }

    String importType = StringUtils.substringBetween(value, "(", ")");
    fileField.setImportType(this.getImportType(value, importType));

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
        return FileFieldRepository.IMPORT_TYPE_FIND;
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
          return FileFieldRepository.IMPORT_TYPE_FIND;
        }
        return FileFieldRepository.IMPORT_TYPE_DIRECT;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Mapper getMapper(String modelFullName) throws ClassNotFoundException {
    Class<? extends Model> klass = (Class<? extends Model>) Class.forName(modelFullName);
    return Mapper.of(klass);
  }
}
