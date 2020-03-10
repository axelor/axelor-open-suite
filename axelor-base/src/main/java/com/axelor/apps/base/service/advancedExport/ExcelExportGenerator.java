/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.app.internal.AppFilter;
import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.AdvancedExportLine;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.DateFormatConverter;
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
    CellStyle dateCellStyle = workbook.createCellStyle();
    CellStyle dateTimeCellStyle = workbook.createCellStyle();

    DateFormat fmt = DateFormat.getDateInstance(DateFormat.SHORT, AppFilter.getLocale());
    if (fmt instanceof SimpleDateFormat) {
      String pattern = ((SimpleDateFormat) fmt).toPattern();
      // use full year
      pattern = pattern.replaceAll("y+", "yyyy");
      dateCellStyle.setDataFormat(
          workbook
              .createDataFormat()
              .getFormat(DateFormatConverter.convert(AppFilter.getLocale(), pattern)));
    }

    fmt = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, AppFilter.getLocale());

    if (fmt instanceof SimpleDateFormat) {
      String pattern = ((SimpleDateFormat) fmt).toPattern();
      // use full year
      pattern = pattern.replaceAll("y+", "yyyy");
      dateTimeCellStyle.setDataFormat(
          workbook
              .createDataFormat()
              .getFormat(DateFormatConverter.convert(AppFilter.getLocale(), pattern)));
    }

    for (List listObj : dataList) {
      Row row = sheet.createRow(sheet.getLastRowNum() + 1);
      for (int colIndex = 0; colIndex < listObj.size(); colIndex++) {
        Object value = listObj.get(colIndex);
        Cell cell = row.createCell(colIndex);
        String columnValue = null;
        if (!(value == null || value.equals(""))) {
          if (value instanceof LocalDate) {
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(
                Date.from(
                    ((LocalDate) value).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
          }
          if (value instanceof LocalDateTime) {
            cell.setCellStyle(dateTimeCellStyle);
            cell.setCellValue(
                Date.from(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant()));
          } else if (value instanceof ZonedDateTime) {
            cell.setCellStyle(dateTimeCellStyle);
            cell.setCellValue(Date.from(((ZonedDateTime) value).toInstant()));
          } else if (value instanceof Instant) {
            cell.setCellStyle(dateTimeCellStyle);
            cell.setCellValue(Date.from((Instant) value));
          } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
          } else {
            cell.setCellValue(value.toString());
          }
        }
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
