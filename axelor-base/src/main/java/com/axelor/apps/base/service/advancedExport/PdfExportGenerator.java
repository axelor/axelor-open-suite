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

import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.AdvancedExportLine;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class PdfExportGenerator extends AdvancedExportGenerator {

  private Document document = null;

  private PdfPTable table = null;

  private AdvancedExport advancedExport;

  private File exportFile;

  private String exportFileName;

  public PdfExportGenerator(AdvancedExport advancedExport) throws AxelorException {
    this.advancedExport = advancedExport;
    exportFileName = advancedExport.getMetaModel().getName() + ".pdf";
    document = new Document();
    table = new PdfPTable(advancedExport.getAdvancedExportLineList().size());
    try {
      exportFile = File.createTempFile(advancedExport.getMetaModel().getName(), ".pdf");
      FileOutputStream outStream = new FileOutputStream(exportFile);
      PdfWriter.getInstance(document, outStream);
    } catch (IOException | DocumentException e) {
      TraceBackService.trace(e);
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
    document.open();
  }

  @Override
  public void generateHeader() throws AxelorException {
    try {
      PdfPCell headerCell;
      for (AdvancedExportLine advancedExportLine : advancedExport.getAdvancedExportLineList()) {
        headerCell =
            new PdfPCell(
                new Phrase(
                    I18n.get(advancedExportLine.getTitle()),
                    new Font(BaseFont.createFont(), 8, 0, BaseColor.WHITE)));
        headerCell.setBackgroundColor(BaseColor.GRAY);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(headerCell);
      }
    } catch (DocumentException | IOException e) {
      TraceBackService.trace(e);
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void generateBody(List<List> dataList) {
    PdfPCell cell;
    Font font = new Font();
    font.setSize(7);

    for (List listObj : dataList) {
      for (int colIndex = 0; colIndex < listObj.size(); colIndex++) {
        Object value = listObj.get(colIndex);
        String columnValue = null;
        if (!(value == null || value.equals(""))) {
          if (value instanceof BigDecimal) columnValue = convertDecimalValue(value);
          else columnValue = value.toString();
        }
        cell = new PdfPCell(new Phrase(columnValue, font));
        table.addCell(cell);
      }
    }
  }

  @Override
  public void close() throws AxelorException {
    try {
      document.add(table);
      document.close();
    } catch (DocumentException e) {
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
