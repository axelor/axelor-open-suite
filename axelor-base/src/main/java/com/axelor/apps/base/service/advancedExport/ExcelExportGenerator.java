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
package com.axelor.apps.base.service.advancedExport;

import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.AdvancedExportLine;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelExportGenerator extends AdvancedExportGenerator {

  private Workbook workbook;

  private Sheet sheet;

  private AdvancedExport advancedExport;

  private File exportFile;

  private String exportFileName;

  public ExcelExportGenerator(AdvancedExport advancedExport) throws AxelorException {
    this.advancedExport = advancedExport;
    exportFileName = advancedExport.getMetaModel().getName() + ".xlsx";
    try {
      exportFile = File.createTempFile(advancedExport.getMetaModel().getName(), ".xlsx");
    } catch (IOException e) {
      TraceBackService.trace(e);
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
    workbook = new XSSFWorkbook();
    sheet = workbook.createSheet(advancedExport.getMetaModel().getName());
  }

  @Override
  public void generateHeader() {
    Row headerRow = sheet.createRow(sheet.getFirstRowNum());
    int colHeaderNum = 0;
    for (AdvancedExportLine advancedExportLine : advancedExport.getAdvancedExportLineList()) {
      Cell headerCell = headerRow.createCell(colHeaderNum++);
      headerCell.setCellValue(I18n.get(advancedExportLine.getTitle()));
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void generateBody(List<List> dataList) {
    for (List listObj : dataList) {
      Row row = sheet.createRow(sheet.getLastRowNum() + 1);
      for (int colIndex = 0; colIndex < listObj.size(); colIndex++) {
        Object value = listObj.get(colIndex);
        Cell cell = row.createCell(colIndex);
        String columnValue = null;
        if (!(value == null || value.equals(""))) {
          if (value instanceof BigDecimal) columnValue = convertDecimalValue(value);
          else columnValue = value.toString();
        } else continue;
        cell.setCellValue(columnValue);
      }
    }
  }

  @Override
  public void close() throws AxelorException {
    try {
      FileOutputStream fout = new FileOutputStream(exportFile);
      workbook.write(fout);
      fout.close();
    } catch (IOException e) {
      TraceBackService.trace(e);
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  @Override
  public AdvancedExport getAdvancedExport() {
    return advancedExport;
  }

  @Override
  public File getExportFile() {
    return exportFile;
  }

  @Override
  public String getFileName() {
    return exportFileName;
  }
}
