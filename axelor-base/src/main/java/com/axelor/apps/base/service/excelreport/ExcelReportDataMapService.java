/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.excelreport;

import com.axelor.apps.base.db.Print;
import com.axelor.apps.base.db.PrintTemplate;
import com.axelor.apps.base.db.ReportQueryBuilder;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.PrintTemplateService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.excelreport.components.ExcelReportCellService;
import com.axelor.apps.base.service.excelreport.components.ExcelReportFooterService;
import com.axelor.apps.base.service.excelreport.components.ExcelReportHeaderService;
import com.axelor.apps.base.service.excelreport.components.ExcelReportPictureService;
import com.axelor.apps.base.service.excelreport.config.ExcelReportConstants;
import com.axelor.apps.base.service.excelreport.config.ExcelReportHelperService;
import com.axelor.apps.base.service.excelreport.config.ReportParameterVariables;
import com.axelor.apps.base.service.excelreport.utility.ExcelReportCellMergingService;
import com.axelor.apps.base.service.excelreport.utility.ExcelReportGroovyService;
import com.axelor.apps.base.service.excelreport.utility.ExcelReportShiftingService;
import com.axelor.apps.base.service.excelreport.utility.ExcelReportTranslationService;
import com.axelor.apps.base.service.excelreport.utility.ExcelReportWriteService;
import com.axelor.apps.base.service.excelreport.utility.ReportQueryBuilderService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.itextpdf.awt.geom.Dimension;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.Query;
import javax.script.ScriptException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

public class ExcelReportDataMapService {

  // constants
  private static final String DATE_FORMAT = "dd/MM/YYYY";
  private static final String DATE_TIME_FORMAT = "dd/MM/YYYY HH:mm";

  // variable parameters
  private int rowNumber = 0;
  private int mergeOffset = 0;
  private List<CellRangeAddress> mergedCellsRangeAddressList;
  private Set<CellRangeAddress> mergedCellsRangeAddressSetPerSheet;
  private int collectionEntryRow = -1;
  private int record;
  private boolean nextRowCheckActive = false;
  private Map<String, List<ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>>>>
      pictureInputMap = new HashMap<>();
  private Map<String, Map<String, List<ImmutablePair<Integer, Integer>>>> pictureRowShiftMap =
      new HashMap<>();
  private Sheet originSheet;
  private List<Integer> removeCellKeyList = new ArrayList<>();
  private List<ReportQueryBuilder> reportQueryBuilderList;
  private Print print = null;
  private PrintTemplate printTemplate;

  public ExcelReportDataMapService(PrintTemplate printTemplate, List<Long> objectIds)
      throws ClassNotFoundException, AxelorException, IOException {
    // Set global variables
    this.print =
        Beans.get(PrintTemplateService.class).getTemplatePrint(objectIds.get(0), printTemplate);
    if (ObjectUtils.notEmpty(printTemplate.getReportQueryBuilderList())) {
      this.reportQueryBuilderList = new ArrayList<>(printTemplate.getReportQueryBuilderList());
    }
    this.printTemplate = printTemplate;
  }

  public Map<Integer, Map<String, Object>> getInputMap(Workbook wb, String sheetName) {
    Map<Integer, Map<String, Object>> map = new HashMap<>();

    Sheet sheet;
    int lastColumn = 0;

    if (wb.getSheet(sheetName) == null) {
      return map;
    }
    sheet = wb.getSheet(sheetName);
    if (sheetName.equalsIgnoreCase(ExcelReportConstants.TEMPLATE_SHEET_TITLE)) {
      mergedCellsRangeAddressList = sheet.getMergedRegions();
      originSheet = sheet;
    }
    Beans.get(ExcelReportPictureService.class)
        .getPictures(sheet, pictureInputMap, mergedCellsRangeAddressList, sheetName);

    int n = 0;
    int maxRows = Beans.get(AppBaseService.class).getAppBase().getMaxRows();
    int maxColumns = Beans.get(AppBaseService.class).getAppBase().getMaxColumns();
    for (int i = 0; i < maxRows; i++) {
      Row row = sheet.getRow(i);
      if (ObjectUtils.notEmpty(row)) {
        for (int j = 0; j < maxColumns; j++) {
          Cell cell = row.getCell(j);
          if (ObjectUtils.isEmpty(cell)
              || (Beans.get(ExcelReportCellService.class).isCellEmpty(cell)
                  && !Beans.get(ExcelReportCellMergingService.class)
                      .isFirstCellOfTheMergedRegion(wb, cell, mergedCellsRangeAddressList))) {
            continue;
          }

          map.put(n, this.getDataMap(cell));
          if (lastColumn < cell.getColumnIndex()) lastColumn = cell.getColumnIndex();
          n++;
        }
      }
    }
    return map;
  }

  protected Map<String, Object> getDataMap(Cell cell) {
    Map<String, Object> map = new HashMap<>();
    Object cellValue = Beans.get(ExcelReportCellService.class).getCellValue(cell);
    map.put(ExcelReportConstants.KEY_ROW, cell.getRowIndex());
    map.put(ExcelReportConstants.KEY_COLUMN, cell.getColumnIndex());
    map.put(ExcelReportConstants.KEY_VALUE, cellValue);
    map.put(ExcelReportConstants.KEY_CELL_STYLE, cell.getCellStyle());

    return map;
  }

  public Workbook createWorkbook(
      Map<Integer, Map<String, Object>> inputMap,
      List<Model> data,
      Mapper mapper,
      String formatType,
      Workbook wb)
      throws AxelorException, ScriptException, IOException, ClassNotFoundException {
    Workbook newWb = WorkbookFactory.create(true);

    Map<Integer, Map<String, Object>> headerOutputMap;
    Map<Integer, Map<String, Object>> footerOutputMap;

    Map<Integer, Map<String, Object>> outputMap;

    mergeOffset =
        Beans.get(ExcelReportCellMergingService.class)
            .setMergeOffset(inputMap, mapper, mergedCellsRangeAddressList);

    int i = 1;
    Map<Integer, Map<String, Object>> headerInputMap =
        Beans.get(ExcelReportHeaderService.class).getHeaderInputMap(wb, print);
    Map<Integer, Map<String, Object>> footerInputMap =
        Beans.get(ExcelReportFooterService.class).getFooterInputMap(wb, print);
    String modelName = printTemplate.getMetaModel().getName();
    for (Model dataItem : data) {
      String sheetName = String.format("%s %s", modelName, i++);

      headerOutputMap = headerInputMap;
      footerOutputMap = footerInputMap;

      outputMap =
          this.getOutputMap(
              inputMap, mapper, dataItem, ExcelReportConstants.TEMPLATE_SHEET_TITLE, sheetName, wb);

      // hide collections if any and recalculate
      if (ObjectUtils.notEmpty(removeCellKeyList)) {
        outputMap =
            this.getOutputMap(
                getHideCollectionInputMap(inputMap),
                mapper,
                dataItem,
                ExcelReportConstants.TEMPLATE_SHEET_TITLE,
                sheetName,
                wb);
      }

      Sheet newSheet = newWb.createSheet(sheetName);

      if (formatType.equals("XLSX")) {
        newSheet =
            Beans.get(ExcelReportHeaderService.class)
                .setHeader(
                    originSheet,
                    newSheet,
                    print,
                    outputMap,
                    headerOutputMap,
                    mergedCellsRangeAddressSetPerSheet,
                    pictureInputMap,
                    pictureRowShiftMap);
        newSheet = this.writeTemplateSheet(outputMap, newSheet, 0);
        newSheet =
            Beans.get(ExcelReportFooterService.class)
                .setFooter(
                    originSheet, newSheet, mergedCellsRangeAddressSetPerSheet, footerOutputMap);
      } else if (formatType.equals("PDF")) {
        newSheet = this.writeTemplateSheet(outputMap, newSheet, 0);
        Beans.get(ExcelReportPictureService.class)
            .writePictures(
                newSheet,
                pictureRowShiftMap,
                pictureInputMap.get(ExcelReportConstants.TEMPLATE_SHEET_TITLE),
                ExcelReportConstants.TEMPLATE_SHEET_TITLE);
      }

      Beans.get(ExcelReportPictureService.class).resetPictureMap(pictureInputMap);
    }

    return newWb;
  }

  private Map<Integer, Map<String, Object>> getHideCollectionInputMap(
      Map<Integer, Map<String, Object>> inputMap) {
    List<ImmutablePair<Integer, Integer>> removeCellRowColumnPair = new ArrayList<>();
    List<Integer> finalRemoveKeyPairList = new ArrayList<>();
    // new input map
    Map<Integer, Map<String, Object>> newInputMap = new HashMap<>(inputMap);

    // get location of all cells to hide
    for (Integer key : removeCellKeyList) {
      Integer row = (Integer) inputMap.get(key).get(ExcelReportConstants.KEY_ROW);
      Integer column = (Integer) inputMap.get(key).get(ExcelReportConstants.KEY_COLUMN);
      removeCellRowColumnPair.add(new ImmutablePair<>(row, column));
      removeCellRowColumnPair.add(new ImmutablePair<>(row + 1, column));
      removeCellRowColumnPair.add(new ImmutablePair<>(row - 1, column));
    }

    // get all hiding cell keys
    for (Map.Entry<Integer, Map<String, Object>> entry : inputMap.entrySet()) {
      for (ImmutablePair<Integer, Integer> pair : removeCellRowColumnPair) {
        if (entry.getValue().get(ExcelReportConstants.KEY_ROW).equals(pair.getLeft())
            && entry.getValue().get(ExcelReportConstants.KEY_COLUMN).equals(pair.getRight())) {
          finalRemoveKeyPairList.add(entry.getKey());
        }
      }
    }

    // shift cells to left which occur after the cells to remove
    for (Map.Entry<Integer, Map<String, Object>> entry : inputMap.entrySet()) {
      for (ImmutablePair<Integer, Integer> pair : removeCellRowColumnPair) {
        if (entry.getValue().get(ExcelReportConstants.KEY_ROW).equals(pair.getLeft())
            && (Integer) entry.getValue().get(ExcelReportConstants.KEY_COLUMN)
                > (pair.getRight())) {
          newInputMap
              .get(entry.getKey())
              .replace(
                  ExcelReportConstants.KEY_COLUMN,
                  (Integer) entry.getValue().get(ExcelReportConstants.KEY_COLUMN) - 1);
        }
      }
    }

    // remove cells to hide
    for (Integer key : finalRemoveKeyPairList) {
      newInputMap.remove(key);
    }

    return newInputMap;
  }

  protected Sheet createSheet(
      Workbook workbook, Map<Integer, Map<String, Object>> map, String sheetName) {

    Sheet sheet = workbook.createSheet(sheetName);
    sheet = Beans.get(ExcelReportWriteService.class).write(map, originSheet, sheet, 0, false);
    Beans.get(ExcelReportCellMergingService.class)
        .fillMergedRegionCells(sheet, mergedCellsRangeAddressSetPerSheet);

    return sheet;
  }

  protected Triple<String, String, String> getOperatingTriple(String formula) {

    Triple<String, String, String> triple = null;

    Pattern p = Pattern.compile("\\((.*?)\\)");
    Matcher m = p.matcher(formula);
    String expr = null;
    if (m.find()) {
      expr = formula.substring(m.start() + 1, m.end());
    }
    String type = "SUM";
    if (expr.toLowerCase().startsWith("sum")) {
      type = "SUM";
    }
    m = p.matcher(expr);
    String params[] = null;
    if (m.find()) {
      String paramString = expr.substring(m.start() + 1, m.end() - 1);
      params = paramString.split("\\s*,\\s*");
    }

    if (params.length > 2) {
      String condition = params[1];
      for (int i = 2; i < params.length; i++)
        condition = String.format("%s AND %s", condition, params[i]);
      triple = Triple.of(type, params[0], condition);
    } else if (params.length == 2) {
      triple = Triple.of(type, params[0], params[1]);
    } else {
      triple = Triple.of(type, params[0], null);
    }

    return triple;
  }

  @SuppressWarnings("unchecked")
  protected Map<Integer, Map<String, Object>> getOutputMap(
      Map<Integer, Map<String, Object>> inputMap,
      Mapper mapper,
      Object object,
      String sheetType,
      String sheetName,
      Workbook wb)
      throws AxelorException, IOException, ScriptException, ClassNotFoundException {

    mergedCellsRangeAddressSetPerSheet = new HashSet<>();
    collectionEntryRow = -1;
    record = 0;
    Map<Integer, Map<String, Object>> outputMap = new HashMap<>(inputMap);
    Object mainObject = object;
    Map<String, List<Object>> reportQueryBuilderMap =
        Beans.get(ReportQueryBuilderService.class)
            .getAllReportQueryBuilderResult(reportQueryBuilderList, object);
    Property property = null;
    int index = inputMap.size();

    int totalRecord = 0;

    long recordId = (long) mapper.getProperty("id").get(object);
    if (sheetType.equalsIgnoreCase(ExcelReportConstants.TEMPLATE_SHEET_TITLE)) {
      Set<CellRangeAddress> blankMergedCellsRangeAddressSet =
          Beans.get(ExcelReportCellMergingService.class)
              .getBlankMergedCells(originSheet, mergedCellsRangeAddressList, sheetType);
      mergedCellsRangeAddressSetPerSheet.addAll(blankMergedCellsRangeAddressSet);
      mergedCellsRangeAddressSetPerSheet.addAll(mergedCellsRangeAddressList);
    }

    for (Map.Entry<Integer, Map<String, Object>> entry : inputMap.entrySet()) {
      Map<String, Object> m = new HashMap<>(entry.getValue());
      boolean hide = false; // groovy condition boolean
      boolean translate = false; // language translation boolean
      String operationString = null;

      if (nextRowCheckActive) {
        if ((int) m.get(ExcelReportConstants.KEY_ROW) > collectionEntryRow) {
          Beans.get(ExcelReportPictureService.class)
              .setPictureRowShiftMap(
                  pictureInputMap,
                  pictureRowShiftMap,
                  sheetName,
                  sheetType,
                  collectionEntryRow,
                  record);
          totalRecord = totalRecord + record;
          record = 0;
        }
        nextRowCheckActive = false;
      }

      Object cellValue = m.get(ExcelReportConstants.KEY_VALUE);
      String value = cellValue == null ? null : cellValue.toString();
      String originValue = value;

      // check for translation function
      String translatedValue =
          Beans.get(ExcelReportTranslationService.class).checkTranslationFunction(value);
      if (!translatedValue.equals(value)) {
        value = translatedValue;
        translate = true;
      }

      outputMap.put(entry.getKey(), m);
      if (StringUtils.notBlank(value)) {
        if (value.contains("$")) {

          String propertyName = value;

          if (!value.contains("$formula")) {

            // Check for groovy conditional text
            ImmutableTriple<String, String, Boolean> tripleResult =
                this.checkGroovyConditionalText(propertyName, object, m);
            if (tripleResult.getRight() == null) {
              continue;
            } else {
              propertyName = tripleResult.getLeft();
              operationString = tripleResult.getMiddle();
              hide = tripleResult.getRight();
            }

            if (propertyName.contains("_tr(value:")) {
              translate = true;
              propertyName =
                  org.apache.commons.lang3.StringUtils.chop(
                      propertyName.trim().replace("_tr(value:", ""));
            }

            propertyName = propertyName.substring(1);
            property = this.getProperty(mapper, propertyName);

            if (ObjectUtils.isEmpty(property)) {
              if (!propertyName.contains(".") || reportQueryBuilderMap.isEmpty()) {
                CellStyle newCellStyle = wb.createCellStyle();
                newCellStyle.setFont(
                    ((XSSFCellStyle) m.get(ExcelReportConstants.KEY_CELL_STYLE)).getFont());
                m.replace(ExcelReportConstants.KEY_VALUE, "");
                m.replace(ExcelReportConstants.KEY_CELL_STYLE, newCellStyle);
                continue;
              }
              String variableName = propertyName.substring(0, propertyName.indexOf("."));
              if (!reportQueryBuilderMap.isEmpty()
                  && reportQueryBuilderMap.containsKey(variableName)) {
                List<Object> collection = reportQueryBuilderMap.get(variableName);
                if (ObjectUtils.isEmpty(collection)) {
                  m.replace(ExcelReportConstants.KEY_VALUE, "");
                  m.replace(ExcelReportConstants.KEY_CELL_STYLE, wb.createCellStyle());
                  continue;
                }
                String modelFullName =
                    ((LinkedHashMap<String, Object>) collection.get(0))
                        .values()
                        .iterator()
                        .next()
                        .getClass()
                        .getName();
                boolean isModel =
                    Beans.get(MetaModelRepository.class)
                            .all()
                            .filter("self.fullName = ?1", modelFullName)
                            .count()
                        > 0;
                String fieldName = propertyName.substring(propertyName.indexOf(".") + 1);

                // continue if no such field found in report query
                if (isModel && ObjectUtils.isEmpty(collection)
                    || (!isModel
                        && ((ObjectUtils.notEmpty(collection)
                            && !((LinkedHashMap<String, String>) collection.get(0))
                                .containsKey(fieldName))))) {
                  m.replace(ExcelReportConstants.KEY_VALUE, "");
                  m.replace(ExcelReportConstants.KEY_CELL_STYLE, wb.createCellStyle());
                  continue;
                }

                Map<String, Object> entryValueMap = new HashMap<>(entry.getValue());

                if (collectionEntryRow != (int) entryValueMap.get(ExcelReportConstants.KEY_ROW)) {
                  collectionEntryRow = (int) entryValueMap.get(ExcelReportConstants.KEY_ROW);
                }

                rowNumber = (Integer) entryValueMap.get(ExcelReportConstants.KEY_ROW);

                ImmutablePair<Integer, Map<Integer, Map<String, Object>>> collectionEntryPair;
                Beans.get(ExcelReportCellMergingService.class)
                    .setMergedCellsRangeAddressSetPerSheet(
                        entryValueMap,
                        collection,
                        totalRecord,
                        mergedCellsRangeAddressList,
                        mergedCellsRangeAddressSetPerSheet,
                        mergeOffset);

                collectionEntryPair =
                    Beans.get(ReportQueryBuilderService.class)
                        .getReportQueryBuilderCollectionEntry(
                            new ReportParameterVariables(
                                printTemplate,
                                outputMap,
                                entryValueMap,
                                collection,
                                entry,
                                fieldName,
                                index,
                                totalRecord,
                                hide,
                                operationString,
                                removeCellKeyList,
                                collectionEntryRow,
                                rowNumber,
                                mergeOffset,
                                record,
                                nextRowCheckActive,
                                isModel,
                                mergedCellsRangeAddressList,
                                mergedCellsRangeAddressSetPerSheet));

                outputMap = collectionEntryPair.getRight();
                totalRecord = collectionEntryPair.getLeft();

                continue;
              } else {
                throw new AxelorException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    I18n.get(IExceptionMessage.NO_SUCH_FIELD) + propertyName);
              }
            }

            if (!property.isCollection()) {

              if (hide) {
                m.replace(ExcelReportConstants.KEY_VALUE, "");
                continue;
              }

              m =
                  this.getNonCollectionEntry(
                      m,
                      mapper,
                      property,
                      mainObject,
                      propertyName,
                      totalRecord,
                      operationString,
                      translate);

            } else {

              Map<String, Object> entryValueMap = new HashMap<>(entry.getValue());
              if (!propertyName.contains(".")) {
                m.replace(ExcelReportConstants.KEY_VALUE, property.getTitle());
                Beans.get(ExcelReportShiftingService.class)
                    .shiftRows(
                        m,
                        totalRecord,
                        collectionEntryRow,
                        mergedCellsRangeAddressList,
                        mergedCellsRangeAddressSetPerSheet);
                continue;
              }

              if (collectionEntryRow != (int) entryValueMap.get(ExcelReportConstants.KEY_ROW)) {
                collectionEntryRow = (int) entryValueMap.get(ExcelReportConstants.KEY_ROW);
              }

              rowNumber = (Integer) entryValueMap.get(ExcelReportConstants.KEY_ROW);

              Collection<Object> collection = (Collection<Object>) property.get(object);
              ImmutablePair<Integer, Map<Integer, Map<String, Object>>> collectionEntryPair;
              Beans.get(ExcelReportCellMergingService.class)
                  .setMergedCellsRangeAddressSetPerSheet(
                      entryValueMap,
                      collection,
                      totalRecord,
                      mergedCellsRangeAddressList,
                      mergedCellsRangeAddressSetPerSheet,
                      mergeOffset);

              collectionEntryPair =
                  this.getCollectionEntry(
                      outputMap,
                      entryValueMap,
                      collection,
                      entry,
                      property,
                      propertyName,
                      index,
                      totalRecord,
                      hide,
                      operationString,
                      translate);
              outputMap = collectionEntryPair.getRight();
              totalRecord = collectionEntryPair.getLeft();
            }
          } else {
            if (propertyName.contains(":")) {
              hide = Beans.get(ExcelReportGroovyService.class).getConditionResult(value, object);
              value = value.substring(0, value.indexOf(":")).trim();
            }
            outputMap.put(
                entry.getKey(),
                this.getFormulaResultMap(entry, value, totalRecord, recordId, hide));
          }
          object = mainObject;
        } else {
          String temp = value;
          value = getLabel(value, object, translate);
          // replace cell value if different
          if (!temp.equals(value) || !originValue.equals(value)) {
            ((XSSFRichTextString) cellValue).setString(value);
          }
          Beans.get(ExcelReportShiftingService.class)
              .shiftRows(
                  m,
                  totalRecord,
                  collectionEntryRow,
                  mergedCellsRangeAddressList,
                  mergedCellsRangeAddressSetPerSheet);
        }
      } else {

        Beans.get(ExcelReportShiftingService.class)
            .shiftRows(
                m,
                totalRecord,
                collectionEntryRow,
                mergedCellsRangeAddressList,
                mergedCellsRangeAddressSetPerSheet);
      }
    }

    return outputMap;
  }

  private ImmutableTriple<String, String, Boolean> checkGroovyConditionalText(
      String propertyName, Object object, Map<String, Object> m)
      throws IOException, AxelorException {

    Boolean hide = false;
    String operationString = "";

    if (propertyName.contains(":")
        && (propertyName.contains("hide") || propertyName.contains("show"))) {
      hide = Beans.get(ExcelReportGroovyService.class).getConditionResult(propertyName, object);
      propertyName = propertyName.substring(0, propertyName.indexOf(":")).trim();
    } else if (propertyName.startsWith("if") && propertyName.contains("->")) {
      ImmutablePair<String, String> valueOperationPair =
          Beans.get(ExcelReportGroovyService.class).getIfConditionResult(propertyName, object);

      propertyName = valueOperationPair.getLeft();
      operationString = valueOperationPair.getRight();
    } else if (propertyName.contains(":") && propertyName.startsWith("$eval:")) {
      m.replace(
          ExcelReportConstants.KEY_VALUE,
          Beans.get(ExcelReportGroovyService.class)
              .validateCondition(propertyName.substring(propertyName.indexOf(":") + 1), object));
      hide = null;
    }

    return new ImmutableTriple<>(propertyName, operationString, hide);
  }

  private String getLabel(String value, Object bean, boolean translate)
      throws IOException, AxelorException {

    ResourceBundle resourceBundle =
        Beans.get(ExcelReportHelperService.class).getResourceBundle(printTemplate);
    if (value.contains(" : ") && (value.contains("hide") || value.contains("show"))) {
      if (Beans.get(ExcelReportGroovyService.class).getConditionResult(value, bean)) {
        value = "";
      } else {
        value = value.substring(0, value.lastIndexOf(" : ")).trim();

        if (Beans.get(ExcelReportTranslationService.class).isTranslationFunction(value)) {

          value =
              Beans.get(ExcelReportTranslationService.class)
                  .getTranslatedValue(value, printTemplate)
                  .toString();
        }
      }
    } else if (value.startsWith("if") && value.contains("->")) { // if else condition
      value = Beans.get(ExcelReportGroovyService.class).getIfConditionResult(value, bean).getLeft();
      if (Beans.get(ExcelReportTranslationService.class).isTranslationFunction(value)) {
        value =
            Beans.get(ExcelReportTranslationService.class)
                .getTranslatedValue(value, printTemplate)
                .toString();
      }
    }
    if (translate) {
      value = resourceBundle.getString(value);
    }

    return value;
  }

  protected ImmutablePair<Integer, Map<Integer, Map<String, Object>>> getCollectionEntry(
      Map<Integer, Map<String, Object>> outputMap,
      Map<String, Object> entryValueMap,
      Collection<Object> collection,
      Map.Entry<Integer, Map<String, Object>> entry,
      Property property,
      String propertyName,
      int index,
      int totalRecord,
      boolean hide,
      String operationString,
      boolean translate)
      throws AxelorException, ScriptException {
    boolean isFirstIteration = true;
    int bigDecimalScale = Beans.get(ExcelReportHelperService.class).getBigDecimalScale();
    ImmutablePair<Property, Object> pair;
    Mapper o2mMapper = Mapper.of(property.getTarget());
    propertyName = propertyName.substring(propertyName.indexOf(".") + 1);

    if (hide) {
      removeCellKeyList.add(entry.getKey());
    }

    Map<String, Object> newEntryValueMap = new HashMap<>(entryValueMap);
    Beans.get(ExcelReportShiftingService.class)
        .shiftRows(
            newEntryValueMap,
            totalRecord,
            collectionEntryRow,
            mergedCellsRangeAddressList,
            mergedCellsRangeAddressSetPerSheet);
    rowNumber = (int) newEntryValueMap.get(ExcelReportConstants.KEY_ROW);

    int localMergeOffset = 0;
    int rowOffset = 0;
    if (!collection.isEmpty()) {

      for (Object ob : collection) {
        Map<String, Object> newMap = new HashMap<>();

        pair = Beans.get(ExcelReportHelperService.class).findField(o2mMapper, ob, propertyName);

        if (ObjectUtils.isEmpty(pair))
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.NO_SUCH_FIELD) + propertyName);

        property = pair.getLeft();
        newMap.putAll(newEntryValueMap);
        newMap.replace(ExcelReportConstants.KEY_ROW, rowNumber + rowOffset + localMergeOffset);
        ob = pair.getRight();

        Object keyValue = "";

        if (ObjectUtils.isEmpty(property.get(ob)) || hide) {
          keyValue = "";
        } else if (property.isReference()) {
          keyValue = findNameColumn(property, property.get(ob));
        } else if (!ObjectUtils.isEmpty(property.getSelection())) {
          String title =
              MetaStore.getSelectionItem(property.getSelection(), property.get(ob).toString())
                  .getTitle();
          keyValue = I18n.get(title);
        } else if (property.get(ob).getClass().equals(BigDecimal.class)) {
          keyValue = ((BigDecimal) property.get(ob)).setScale(bigDecimalScale).toString();
        } else {
          keyValue = property.get(ob).toString();
        }

        if (StringUtils.notEmpty(operationString)) {
          keyValue =
              Beans.get(ExcelReportGroovyService.class)
                  .calculateFromString(
                      keyValue.toString().concat(operationString), bigDecimalScale);
        }

        if (translate) {
          keyValue =
              Beans.get(ExcelReportTranslationService.class)
                  .getTranslatedValue(keyValue, printTemplate);
        }
        newMap.replace(ExcelReportConstants.KEY_VALUE, keyValue);

        while (outputMap.containsKey(index)) index++;
        if (isFirstIteration) {
          index = entry.getKey();
          isFirstIteration = false;
        }

        outputMap.put(index, newMap);
        index++;
        rowOffset = rowOffset + localMergeOffset + 1;
        if (localMergeOffset == 0 && mergeOffset != 0) localMergeOffset = mergeOffset;
      }
      if (record == 0) record = rowOffset - 1;
    } else {
      newEntryValueMap.replace(ExcelReportConstants.KEY_VALUE, "");
      outputMap.put(entry.getKey(), newEntryValueMap);
    }
    if (!nextRowCheckActive) nextRowCheckActive = true;

    return ImmutablePair.of(totalRecord, outputMap);
  }

  protected Property getProperty(Mapper mapper, String propertyName) {
    Property property;

    if (propertyName.contains(".")) {
      property = mapper.getProperty(propertyName.substring(0, propertyName.indexOf(".")));
    } else {
      property = mapper.getProperty(propertyName);
    }

    return property;
  }

  protected Map<String, Object> getFormulaResultMap(
      Map.Entry<Integer, Map<String, Object>> entry,
      String content,
      int totalRecord,
      long recordId,
      boolean hide) {
    Triple<String, String, String> operatingTriple = this.getOperatingTriple(content.substring(1));
    String operation = operatingTriple.getLeft();
    String m2oFieldName = operatingTriple.getMiddle();
    String condition = operatingTriple.getRight();
    CellRangeAddress newAddress = null;
    Map<String, Object> newMap = new HashMap<>();
    newMap.putAll(entry.getValue());
    newMap.replace(
        ExcelReportConstants.KEY_ROW,
        (Integer) newMap.get(ExcelReportConstants.KEY_ROW) + totalRecord);

    String result = "";
    if (!hide) {
      result = this.getResult(operation, m2oFieldName, condition, recordId);
    }

    newMap.replace(ExcelReportConstants.KEY_VALUE, result);

    newAddress =
        Beans.get(ExcelReportCellMergingService.class)
            .setMergedCellsForTotalRow(
                mergedCellsRangeAddressList,
                (int) entry.getValue().get(ExcelReportConstants.KEY_ROW),
                (int) entry.getValue().get(ExcelReportConstants.KEY_COLUMN),
                totalRecord);
    if (ObjectUtils.notEmpty(newAddress)) {
      mergedCellsRangeAddressSetPerSheet.add(newAddress);
    }

    return newMap;
  }

  protected Map<String, Object> getNonCollectionEntry(
      Map<String, Object> m,
      Mapper mapper,
      Property property,
      Object object,
      String propertyName,
      int totalRecord,
      String operationString,
      boolean translate)
      throws AxelorException, ScriptException {
    ImmutablePair<Property, Object> pair =
        Beans.get(ExcelReportHelperService.class).findField(mapper, object, propertyName);

    if (ObjectUtils.isEmpty(pair))
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.NO_SUCH_FIELD) + propertyName);

    property = pair.getLeft();
    object = pair.getRight();

    Object outputValue = "";
    if (object == null || ObjectUtils.isEmpty(property.get(object))) {
      outputValue = "";
    } else if (property.isReference()) {
      outputValue = findNameColumn(property, property.get(object));
    } else if (!ObjectUtils.isEmpty(property.getSelection())) {

      String title =
          MetaStore.getSelectionItem(property.getSelection(), property.get(object).toString())
              .getTitle();
      outputValue = I18n.get(title);

    } else if (property.get(object).getClass() == LocalDate.class) {
      LocalDate date = (LocalDate) property.get(object);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
      String formattedDate = date.format(formatter);
      outputValue = formattedDate;
    } else if (property.get(object).getClass() == LocalDateTime.class) {
      LocalDateTime dateTime = (LocalDateTime) property.get(object);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
      String formattedDateTime = dateTime.format(formatter);
      outputValue = formattedDateTime;
    } else {
      outputValue = property.get(object).toString();
    }

    if (StringUtils.notEmpty(operationString)) {
      outputValue =
          Beans.get(ExcelReportGroovyService.class)
              .calculateFromString(
                  outputValue.toString().concat(operationString),
                  Beans.get(ExcelReportHelperService.class).getBigDecimalScale());
    }

    if (translate) {
      outputValue =
          Beans.get(ExcelReportTranslationService.class)
              .getTranslatedValue(outputValue, printTemplate);
    }
    m.replace(ExcelReportConstants.KEY_VALUE, outputValue);

    if (totalRecord > 0) {
      Beans.get(ExcelReportShiftingService.class)
          .shiftRows(
              m,
              totalRecord,
              collectionEntryRow,
              mergedCellsRangeAddressList,
              mergedCellsRangeAddressSetPerSheet);
    }

    return m;
  }

  protected String getResult(String operation, String content, String condition, long recordId) {

    Object resultObject = null;
    String o2mFieldName = content.substring(0, content.indexOf("."));
    int bigDecimalScale = Beans.get(ExcelReportHelperService.class).getBigDecimalScale();
    String result = BigDecimal.ZERO.setScale(bigDecimalScale).toString();
    String propertyName = content.substring(content.indexOf(".") + 1);
    String queryString =
        String.format(
            "SELECT %s(l.%s) FROM %s s JOIN s.%s l where s.id = %s",
            operation,
            propertyName,
            printTemplate.getMetaModel().getName(),
            o2mFieldName,
            recordId);

    if (StringUtils.notBlank(condition))
      queryString = queryString + " AND " + condition.replace(o2mFieldName, "l");

    Query query = JPA.em().createQuery(queryString);
    resultObject = query.getSingleResult();

    if (ObjectUtils.notEmpty(resultObject))
      result = ((BigDecimal) resultObject).setScale(bigDecimalScale).toString();

    return result;
  }

  protected Object findNameColumn(Property targetField, Object value) {
    String nameColumn = targetField.getTargetName();
    for (Property property : Mapper.of(targetField.getTarget()).getProperties()) {
      if (nameColumn.equals(property.getName())) {
        return property.get(value);
      }
    }
    return null;
  }

  protected Sheet writeTemplateSheet(
      Map<Integer, Map<String, Object>> outputMap, Sheet sheet, int offset) {
    sheet =
        Beans.get(ExcelReportWriteService.class)
            .write(outputMap, originSheet, sheet, offset, false);
    Beans.get(ExcelReportCellMergingService.class)
        .fillMergedRegionCells(sheet, mergedCellsRangeAddressSetPerSheet);
    Beans.get(ExcelReportCellMergingService.class)
        .setMergedRegionsInSheet(sheet, mergedCellsRangeAddressSetPerSheet);
    return sheet;
  }

  // getters
  public Print getPrint() {
    return print;
  }

  public PrintTemplate getPrintTemplate() {
    return printTemplate;
  }
}
