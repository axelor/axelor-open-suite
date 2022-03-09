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
package com.axelor.apps.dmn.service;

import com.axelor.apps.bpm.db.WkfDmnModel;
import com.axelor.apps.bpm.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.google.common.base.Strings;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.Output;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;

public class DmnExportServiceImpl implements DmnExportService {

  private Workbook workbook;

  @Override
  public File exportDmnTable(WkfDmnModel wkfDmnModel) throws AxelorException {
    if (wkfDmnModel.getDiagramXml() == null) {
      return null;
    }

    workbook = new XSSFWorkbook();
    File exportFile = null;

    try {
      exportFile = File.createTempFile(wkfDmnModel.getName(), ".xlsx");
    } catch (IOException e) {
      TraceBackService.trace(e);
    }

    DmnModelInstance dmnModelInstance =
        Dmn.readModelFromStream(new ByteArrayInputStream(wkfDmnModel.getDiagramXml().getBytes()));

    Collection<DecisionTable> tables = dmnModelInstance.getModelElementsByType(DecisionTable.class);
    this.processTables(tables);

    FileOutputStream fout;
    try {
      fout = new FileOutputStream(exportFile);
      workbook.write(fout);
      fout.close();
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return exportFile;
  }

  private void processTables(Collection<DecisionTable> tables) throws AxelorException {
    for (DecisionTable table : tables) {
      String sheetName = table.getParentElement().getAttributeValue("id");
      Sheet sheet = workbook.createSheet(sheetName);
      this.createHeaderRow(sheet, table);
      this.createDataRow(sheet, table);
    }
  }

  private void createHeaderRow(Sheet sheet, DecisionTable table) throws AxelorException {
    Row titleRow = sheet.createRow(sheet.getLastRowNum());
    Cell titleCell = titleRow.createCell(0);
    titleCell.setCellValue(table.getParentElement().getAttributeValue("name"));
    sheet.autoSizeColumn(0);

    Row row = sheet.createRow(sheet.getLastRowNum() + 1);
    int inputIndex = 0;
    for (Input input : table.getInputs()) {
      if (Strings.isNullOrEmpty(input.getLabel())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE, IExceptionMessage.MISSING_INPUT_LABEL);
      }
      Cell cell = row.createCell(inputIndex);
      cell.setCellValue(input.getLabel() + "(" + input.getId() + ")");
      sheet.autoSizeColumn(inputIndex);
      inputIndex++;
    }

    int outputIndex = row.getLastCellNum();
    for (Output output : table.getOutputs()) {
      if (Strings.isNullOrEmpty(output.getLabel())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE, IExceptionMessage.MISSING_OUTPUT_LABEL);
      }
      Cell cell = row.createCell(outputIndex);
      cell.setCellValue(output.getLabel() + "(" + output.getId() + ")");
      sheet.autoSizeColumn(outputIndex);
      outputIndex++;
    }

    Cell cell = row.createCell(outputIndex);
    cell.setCellValue("Annotation");
    sheet.autoSizeColumn(outputIndex);
  }

  private void createDataRow(Sheet sheet, DecisionTable table) {
    int index = sheet.getLastRowNum() + 1;
    for (Rule rule : table.getRules()) {
      Row row = sheet.createRow(index);
      int ipCellIndex = 0;
      for (InputEntry ie : rule.getInputEntries()) {
        Cell cell = row.createCell(ipCellIndex);
        cell.setCellValue(ie.getTextContent());
        sheet.autoSizeColumn(ipCellIndex);
        ipCellIndex++;
      }

      int opCellIndex = row.getLastCellNum();
      for (OutputEntry oe : rule.getOutputEntries()) {
        Cell cell = row.createCell(opCellIndex);
        cell.setCellValue(oe.getTextContent());
        sheet.autoSizeColumn(opCellIndex);
        opCellIndex++;
      }

      Cell cell = row.createCell(opCellIndex);
      cell.setCellValue(
          rule.getDescription() != null ? rule.getDescription().getTextContent() : null);
      sheet.autoSizeColumn(opCellIndex);

      index++;
    }
  }
}
