/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.advancedExport;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.AdvancedExportLine;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class PdfExportGenerator extends AdvancedExportGenerator {

  private AdvancedExport advancedExport;

  private Document document;
  private PdfPTable table;

  private File exportFile;
  private String exportFileName;

  public PdfExportGenerator(AdvancedExport advancedExport) throws AxelorException {
    try {

      this.advancedExport = advancedExport;
      this.exportFileName = advancedExport.getMetaModel().getName();
      this.exportFile = File.createTempFile(exportFileName, ".pdf");

      this.document = new Document();
      this.table = new PdfPTable(advancedExport.getAdvancedExportLineList().size());
      this.table.setWidthPercentage(100);
      PdfWriter.getInstance(document, new FileOutputStream(exportFile));
    } catch (IOException e) {
      TraceBackService.trace(e);
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
    document.open();
  }

  @Override
  public void generateHeader() throws AxelorException {
    Font font = getFont(8);
    for (AdvancedExportLine advancedExportLine : advancedExport.getAdvancedExportLineList()) {
      PdfPCell cell = getHeaderCell(font, advancedExportLine.getTitle());
      table.addCell(cell);
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void generateBody(List<List> dataList) throws AxelorException {
    Font font = getFont(7);
    for (List listObj : dataList) {
      for (Object value : listObj) {
        PdfPCell cell = getCell(font, getColumnValue(value));
        table.addCell(cell);
      }
    }
  }

  protected String getColumnValue(Object value) {
    String columnValue;
    if (!(value == null || value.equals(""))) {
      if (value instanceof BigDecimal) columnValue = convertDecimalValue(value);
      else columnValue = value.toString();
    } else {
      columnValue = "";
    }
    return columnValue;
  }

  protected PdfPCell getHeaderCell(Font font, String value) {
    PdfPCell cell = getCell(font, value);
    cell.setBackgroundColor(Color.LIGHT_GRAY);
    return cell;
  }

  protected PdfPCell getCell(Font font, String value) {
    Phrase phrase = new Phrase(value, font);
    PdfPCell cell = new PdfPCell(phrase);
    return cell;
  }

  protected Font getFont(float size) {
    return FontFactory.getFont(FontFactory.HELVETICA, size);
  }

  @Override
  public void close() throws AxelorException {
    document.add(table);
    document.close();
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
    return exportFileName + ".pdf";
  }
}
