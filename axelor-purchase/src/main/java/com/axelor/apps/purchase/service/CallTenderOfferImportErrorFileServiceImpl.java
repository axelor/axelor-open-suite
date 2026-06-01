/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.purchase.service;

import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import jakarta.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CallTenderOfferImportErrorFileServiceImpl
    implements CallTenderOfferImportErrorFileService {

  protected final MetaFiles metaFiles;

  @Inject
  public CallTenderOfferImportErrorFileServiceImpl(MetaFiles metaFiles) {
    this.metaFiles = metaFiles;
  }

  @Override
  public MetaFile generateErrorFile(File originalExcelFile, Map<Integer, String> errorsByRow)
      throws IOException {

    if (errorsByRow.isEmpty()) {
      return null;
    }

    try (Workbook workbook = new XSSFWorkbook(new FileInputStream(originalExcelFile))) {
      Sheet sheet = workbook.getSheetAt(0);
      CellStyle errorStyle = createErrorStyle(workbook);

      Row headerRow = sheet.getRow(0);
      int errorsColIndex = headerRow.getLastCellNum();

      Cell errorsHeaderCell = headerRow.createCell(errorsColIndex);
      errorsHeaderCell.setCellValue(I18n.get("Errors"));
      CellStyle headerErrorStyle = workbook.createCellStyle();
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerErrorStyle.setFont(headerFont);
      errorsHeaderCell.setCellStyle(headerErrorStyle);

      for (Map.Entry<Integer, String> entry : errorsByRow.entrySet()) {
        Row row = sheet.getRow(entry.getKey());
        if (row == null) {
          continue;
        }
        for (int j = 0; j < row.getLastCellNum(); j++) {
          Cell cell = row.getCell(j);
          if (cell != null) {
            cell.setCellStyle(errorStyle);
          }
        }
        Cell errorCell = row.createCell(errorsColIndex);
        errorCell.setCellValue(entry.getValue());
        errorCell.setCellStyle(errorStyle);
      }

      sheet.autoSizeColumn(errorsColIndex);

      File tempFile = File.createTempFile("import-errors", ".xlsx");
      try (FileOutputStream fos = new FileOutputStream(tempFile)) {
        workbook.write(fos);
      }
      try (FileInputStream inStream = new FileInputStream(tempFile)) {
        return metaFiles.upload(inStream, "import-errors.xlsx");
      }
    }
  }

  protected CellStyle createErrorStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setColor(IndexedColors.RED.getIndex());
    style.setFont(font);
    return style;
  }
}
