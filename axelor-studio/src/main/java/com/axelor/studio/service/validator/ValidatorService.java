/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.validator;

import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.service.CommonService;
import com.axelor.studio.service.excel.importer.DataReaderService;
import com.axelor.studio.service.excel.importer.ExcelImporterService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ValidatorService {

  private String[] headers;

  private File logFile;

  private XSSFWorkbook logBook;

  private Map<String, List<String>> modelMap;

  private Map<String, String[]> invalidModelMap;

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private MenuValidator menuValidator;

  @Inject private WkfValidator wkfValidator;

  public File validate(DataReaderService reader) throws IOException {

    modelMap = new HashMap<String, List<String>>();
    invalidModelMap = new HashMap<String, String[]>();
    headers = CommonService.HEADERS;

    String[] keys = reader.getKeys();
    if (keys == null) {
      return null;
    }

    for (String key : keys) {
      if (CommonService.IGNORE_KEYS.contains(key)) {
        continue;
      }

      String modelName = key.contains("(") ? key.substring(0, key.indexOf("(")) : key;
      String modelType =
          (key.contains("(") && key.contains(")"))
              ? key.substring(key.indexOf("(") + 1, key.length() - 1)
              : null;

      try {
        if (modelType == null || !CommonService.MODEL_TYPES.contains(modelType)) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(
                  "Please mention one of following model type in a sheet name : Real | Custom"));
        }
      } catch (AxelorException e) {
        addLog(e.getMessage(), key, 0);
      }

      try {
        if (!modelName.matches("[A-Z][a-zA-Z0-9_]+")) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Please follow standard model naming convention for sheet name"));
        }
      } catch (AxelorException e) {
        addLog(e.getMessage(), key, 0);
      }

      if (validateModelHeaders(reader, key)) {
        validateKey(modelName, reader, key);
      }
    }

    checkInvalid();

    menuValidator.validate(this, reader, "Menu");
    wkfValidator.validate(this, reader);

    if (logBook != null) {
      logBook.write(new FileOutputStream(logFile));
    }

    return logFile;
  }

  public boolean validateModelHeaders(DataReaderService reader, String key) throws IOException {

    String[] headers = reader.read(key, 0);
    if (headers == null || headers.length < this.headers.length) {
      addLog("Invalid headers", key, 2);
      return false;
    }

    return true;
  }

  private void validateKey(String modelName, DataReaderService reader, String key)
      throws IOException {

    if (key == null) {
      return;
    }

    int totalLines = reader.getTotalLines(key);

    for (int rowNum = 1; rowNum < totalLines; rowNum++) {

      String row[] = reader.read(key, rowNum);
      if (row == null) {
        continue;
      }

      Map<String, String> valMap = ExcelImporterService.createValMap(row, this.headers);

      String model = getModel(modelName, key, rowNum);

      validateField(model, valMap, key, rowNum);
    }
  }

  private String getModel(String model, String key, int rowNum) throws IOException {

    if (model == null) {
      return null;
    }

    if (!modelMap.containsKey(model)) {
      modelMap.put(model, new ArrayList<String>());
    }

    invalidModelMap.remove(model);

    return model;
  }

  private void validateField(String model, Map<String, String> valMap, String key, int rowNum)
      throws IOException {

    String type = valMap.get(CommonService.TYPE);
    if (type != null) {
      type = validateType(type, valMap, key, rowNum);
    } else {
      addLog(I18n.get("No type defined"), key, rowNum);
    }

    validateName(type, model, valMap, key, rowNum);
    validatePosition(valMap, key, rowNum);
    checkSelect(type, valMap, key, rowNum);
  }

  private String validateType(String type, Map<String, String> valMap, String key, int rowNum)
      throws IOException {

    type = type.trim();
    String reference = null;

    if (type.contains("(")) {
      String[] ref = type.split("\\(");
      if (ref.length > 1) {
        reference = ref[1].replace(")", "");
      }
      type = ref[0];
    }

    if (!CommonService.FIELD_TYPES.contains(type) && !CommonService.VIEW_ELEMENTS.contains(type)) {
      addLog(I18n.get("Invalid type"), key, rowNum);
    }

    if (CommonService.RELATIONAL_FIELD_TYPES.contains(type)) {
      if (reference == null) {
        addLog(I18n.get("Reference is empty for type"), key, rowNum);

      } else if (!modelMap.containsKey(reference) && !invalidModelMap.containsKey(reference)) {
        invalidModelMap.put(reference, new String[] {key, String.valueOf(rowNum)});
      }
    }

    return type;
  }

  private void validateName(
      String type, String model, Map<String, String> valMap, String key, int rowNum)
      throws IOException {

    String name = valMap.get(CommonService.NAME);

    if (!Strings.isNullOrEmpty(name)) {
      try {
        if (type.equals("panel") && !name.endsWith("(start)") && !name.endsWith("(end)")) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(
                  "Please mention 'start' or 'end' keywords at the end of name of panel with brackets"));
        }
      } catch (AxelorException e) {
        addLog(e.getMessage(), key, rowNum);
      }

      if (name.contains("(") && type.equals("panel")) {
        String[] ref = name.split("\\(");
        name = ref[0];

      } else {
        try {
          if (!name.matches("([a-z][a-zA-Z0-9_]+)|([A-Z][A-Z0-9_]+)")) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get("Please follow standard field naming convension"));
          }
        } catch (AxelorException e) {
          addLog(e.getMessage(), key, rowNum);
        }
      }
    } else if (type != null && !CommonService.VIEW_ELEMENTS.contains(type)) {
      addLog(I18n.get("Name is empty or type is invalid."), key, rowNum);
    }
  }

  private void validatePosition(Map<String, String> valMap, String key, int rowNum)
      throws IOException {

    String position = valMap.get(CommonService.POSITION);

    if (!Strings.isNullOrEmpty(position)) {

      if (position.contains("(")) {
        String[] types = position.split("\\(");
        position = types[0];
      }

      if (!CommonService.POSITION_TYPES.contains(position)) {
        addLog(
            I18n.get("Please mention one of following positions : after | before | replace"),
            key,
            rowNum);
      }
    }
  }

  private void checkSelect(String type, Map<String, String> valMap, String key, int rowNum)
      throws IOException {

    String select = valMap.get(CommonService.SELECT);

    if (select == null) {
      select = valMap.get(CommonService.SELECT_FR);
    }

    if (select != null
        && !type.equals("string")
        && !type.equals("integer")
        && !type.equals("decimal")) {
      addLog(
          I18n.get("Selection defined for non select field. " + "Please check the type"),
          key,
          rowNum);
    }
  }

  private void checkInvalid() throws IOException {

    if (!invalidModelMap.isEmpty()) {
      List<MetaModel> models =
          metaModelRepo.all().filter("self.name in ?1", invalidModelMap.keySet()).fetch();

      for (MetaModel model : models) {
        invalidModelMap.remove(model.getName());
      }

      for (String[] row : invalidModelMap.values()) {
        addLog("Invalid reference model", row[0], Integer.parseInt(row[1]));
      }
    }
  }

  public void addLog(String log, String sheetName, int rowNum) throws IOException {

    if (logFile == null) {
      logFile = File.createTempFile("Log", ".xlsx");
      logBook = new XSSFWorkbook();
    }

    XSSFSheet sheet = logBook.getSheet(sheetName);

    if (sheet == null) {
      sheet = logBook.createSheet(sheetName);
      XSSFRow titleRow = sheet.createRow(0);
      titleRow.createCell(0).setCellValue("Sheet/Model");
      titleRow.createCell(1).setCellValue("Row Number");
      titleRow.createCell(2).setCellValue("Issues");
    }

    Iterator<Row> rowIterator = sheet.rowIterator();
    Row logRow = null;
    while (rowIterator.hasNext()) {
      Row sheetRow = rowIterator.next();
      Cell cell = sheetRow.getCell(0);
      if (cell.getCellType() != Cell.CELL_TYPE_NUMERIC) {
        continue;
      }
      double value = cell.getNumericCellValue();
      if (value == rowNum + 1) {
        logRow = sheetRow;
        break;
      }
    }

    if (logRow == null) {
      logRow = sheet.createRow(sheet.getPhysicalNumberOfRows());
    }

    Cell cell = logRow.getCell(0);
    if (cell == null) {
      cell = logRow.createCell(0);
      if (rowNum == 0) {
        cell.setCellValue(sheetName);
      }
    }
    sheet.autoSizeColumn(cell.getColumnIndex());

    cell = logRow.getCell(1);
    if (cell == null) {
      cell = logRow.createCell(1);
      if (rowNum != 0) {
        cell.setCellValue(rowNum + 1);
      }
    }
    sheet.autoSizeColumn(cell.getColumnIndex());

    cell = logRow.getCell(2);
    if (cell == null) {
      cell = logRow.createCell(2);
    }

    String oldValue = cell.getStringCellValue();
    if (Strings.isNullOrEmpty(oldValue)) {
      cell.setCellValue(log);
    } else {
      cell.setCellValue(oldValue + "\n" + log);
    }
    sheet.autoSizeColumn(cell.getColumnIndex());
  }

  public boolean isValidModel(String name) {

    if (modelMap.containsKey(name)) {
      return true;
    } else if (name.equals("MetaJsonRecord")) {
      return true;
    }

    MetaModel model = metaModelRepo.findByName(name);
    if (model != null) {
      return true;
    }

    return false;
  }
}
