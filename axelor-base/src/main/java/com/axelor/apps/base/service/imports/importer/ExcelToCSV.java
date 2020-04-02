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
package com.axelor.apps.base.service.imports.importer;

import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelToCSV {

  public List<Map> generateExcelSheets(File file) throws IOException {
    List<Map> newSheets = new ArrayList<>();
    Object sheet = new Object();

    FileInputStream inputStream;
    Workbook workBook = null;

    try {

      inputStream = new FileInputStream(file);
      workBook = new XSSFWorkbook(inputStream);

      for (int i = 0; i < workBook.getNumberOfSheets(); i++) {
        sheet = workBook.getSheetAt(i).getSheetName();
        Map<String, Object> newSheet = new HashMap<String, Object>();
        newSheet.put("name", sheet);
        newSheets.add(newSheet);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return newSheets;
  }

  public void writeTOCSV(File sheetFile, Sheet sheet, int startRow, int startColumn)
      throws IOException, ParseException, AxelorException {
    String separator = ";";
    FileWriter writer = new FileWriter(sheetFile);
    int cnt = 0;

    for (int row = startRow; row <= sheet.getLastRowNum(); row++) {

      if (row == startRow) {
        Row headerRow = sheet.getRow(row);
        for (int cell = startColumn; cell < headerRow.getLastCellNum(); cell++) {
          Cell headerCell = headerRow.getCell(cell);
          if (headerCell == null || headerCell.getCellType() != Cell.CELL_TYPE_STRING) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_INCONSISTENCY,
                I18n.get(IExceptionMessage.INVALID_HEADER));
          }
          writer.append(headerCell.getStringCellValue() + separator);
          cnt++;
        }
        writer.append("\n");

      } else {

        Row dataRow = sheet.getRow(row);
        for (int cell = startColumn; cell <= cnt; cell++) {
          try {

            Cell dataCell = dataRow.getCell(cell);
            if (dataCell != null) {

              switch (dataCell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                  String strData = dataCell.getStringCellValue();
                  writer.append("\"" + strData + "\"" + separator);
                  break;

                case Cell.CELL_TYPE_NUMERIC:
                  if (DateUtil.isCellDateFormatted(dataCell)) {
                    String dateInString = getDateValue(dataCell);
                    writer.append("\"" + dateInString + "\"" + separator);

                  } else {
                    Integer val = (int) dataCell.getNumericCellValue();
                    writer.append(val.toString() + separator);
                  }
                  break;

                case Cell.CELL_TYPE_BLANK:
                default:
                  writer.append("" + separator);
                  break;
              }
            } else {
              writer.append("" + separator);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        writer.append("\n");
      }
    }

    writer.flush();
    writer.close();
  }

  public static String getDateValue(Cell cell) {

    Calendar cal = Calendar.getInstance();
    Date date = cell.getDateCellValue();
    cal.setTime(date);
    int hours = cal.get(Calendar.HOUR_OF_DAY);
    int minutes = cal.get(Calendar.MINUTE);
    int seconds = cal.get(Calendar.SECOND);

    SimpleDateFormat format;
    if (hours == 0 && minutes == 0 && seconds == 0) format = new SimpleDateFormat("yyyy-MM-dd");
    else if (seconds == 0) format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    else format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS");

    return format.format(date);
  }
}
