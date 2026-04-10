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

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.helpers.StringHelper;
import jakarta.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CallTenderExcelServiceImpl implements CallTenderExcelService {

  protected final AppBaseService appBaseService;
  protected final MetaFiles metaFiles;

  @Inject
  public CallTenderExcelServiceImpl(AppBaseService appBaseService, MetaFiles metaFiles) {
    this.appBaseService = appBaseService;
    this.metaFiles = metaFiles;
  }

  @Override
  public MetaFile generateExcelFile(List<CallTenderOffer> offerList) throws IOException {

    Partner supplier = null;
    String callTenderName = null;
    if (!offerList.isEmpty()) {
      supplier = offerList.get(0).getSupplierPartner();
      callTenderName = offerList.get(0).getCallTender().getName();
    }

    String fileName =
        StringHelper.cutTooLongString(
            String.format(
                "CFT%s-%s-%s",
                callTenderName,
                Optional.ofNullable(supplier).map(Partner::getSimpleFullName).orElse(""),
                DateTimeFormatter.ofPattern("ddMMyyyyhhmm")
                    .format(appBaseService.getTodayDateTime())));

    File file = File.createTempFile(fileName, ".xlsx");
    fileName += ".xlsx";

    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Call Tender");

      CellStyle headerStyle = createHeaderStyle(workbook);
      createHeaderRow(sheet, headerStyle);
      createDataRows(sheet, offerList);
      autoSizeColumns(sheet);

      try (FileOutputStream fos = new FileOutputStream(file)) {
        workbook.write(fos);
      }
    }

    try (FileInputStream inStream = new FileInputStream(file)) {
      return metaFiles.upload(inStream, fileName);
    }
  }

  protected CellStyle createHeaderStyle(XSSFWorkbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);
    return style;
  }

  protected void createHeaderRow(Sheet sheet, CellStyle headerStyle) {
    Row headerRow = sheet.createRow(0);
    String[] headers = getHeaders();
    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
      cell.setCellStyle(headerStyle);
    }
  }

  protected void createDataRows(Sheet sheet, List<CallTenderOffer> offerList) {
    int rowNum = 1;
    for (CallTenderOffer offer : offerList) {
      Row row = sheet.createRow(rowNum++);
      row.createCell(0).setCellValue(offer.getProduct().getCode());
      row.createCell(1).setCellValue(offer.getProduct().getName());
      row.createCell(2)
          .setCellValue(
              Optional.ofNullable(offer.getCallTenderNeed())
                  .map(need -> need.getDescription())
                  .orElse(""));
      row.createCell(3).setCellValue(offer.getRequestedQty().doubleValue());
      row.createCell(4)
          .setCellValue(
              Optional.ofNullable(offer.getRequestedUnit()).map(unit -> unit.getName()).orElse(""));
      row.createCell(5)
          .setCellValue(
              Optional.ofNullable(offer.getRequestedDate()).map(LocalDate::toString).orElse(""));
      row.createCell(6)
          .setCellValue(
              Optional.ofNullable(offer.getRequestedDeliveryTime())
                  .map(String::valueOf)
                  .orElse(""));
      row.createCell(7).setCellValue("");
      row.createCell(8).setCellValue("");
    }
  }

  protected void autoSizeColumns(Sheet sheet) {
    for (int i = 0; i < getHeaders().length; i++) {
      sheet.autoSizeColumn(i);
    }
  }

  protected String[] getHeaders() {
    return new String[] {
      I18n.get("Product code"),
      I18n.get("Product name"),
      I18n.get("Description"),
      I18n.get("Qty"),
      I18n.get("Unit"),
      I18n.get("Date"),
      I18n.get("Delivery time"),
      I18n.get("Unit price"),
      I18n.get("Comment")
    };
  }
}
