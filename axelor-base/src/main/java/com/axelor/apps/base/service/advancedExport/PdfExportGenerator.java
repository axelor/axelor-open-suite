/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.Row;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.AdvancedExportLine;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

public class PdfExportGenerator extends AdvancedExportGenerator {

  private PDDocument pdfDocument;
  private PDPage pdPage;
  private Row<PDPage> row;
  private PDPageContentStream contentStream;
  private BaseTable baseTable;
  private Cell<PDPage> cell;
  private AdvancedExport advancedExport;

  private File exportFile;

  private String exportFileName;

  public PdfExportGenerator(AdvancedExport advancedExport) throws AxelorException {
    this.advancedExport = advancedExport;
    exportFileName = advancedExport.getMetaModel().getName() + ".pdf";
    pdfDocument = new PDDocument();
    pdPage = new PDPage();
    pdfDocument.addPage(pdPage);

    try {
      contentStream = new PDPageContentStream(pdfDocument, pdPage);
      exportFile = File.createTempFile(advancedExport.getMetaModel().getName(), ".pdf");

      int bottomMargin = 100;
      int margin = 10;
      float tableWidth = pdPage.getMediaBox().getWidth() - (2 * margin);
      float yStartNewPage = pdPage.getMediaBox().getHeight() - (2 * margin);

      baseTable =
          new BaseTable(
              700,
              yStartNewPage,
              bottomMargin,
              tableWidth,
              margin,
              pdfDocument,
              pdPage,
              true,
              true);
      row = baseTable.createRow(15f);

    } catch (IOException e) {
      TraceBackService.trace(e);
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  @Override
  public void generateHeader() throws AxelorException {
    for (AdvancedExportLine advancedExportLine : advancedExport.getAdvancedExportLineList()) {
      cell =
          row.createCell(
              (float) 100 / advancedExport.getAdvancedExportLineList().size(),
              advancedExportLine.getTitle());
      cell.setFillColor(Color.LIGHT_GRAY);
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void generateBody(List<List> dataList) throws AxelorException {
    for (List listObj : dataList) {
      row = baseTable.createRow(15f);
      for (Object value : listObj) {
        String columnValue = getColumnValue(value);
        cell =
            row.createCell(
                ((float) 100 / advancedExport.getAdvancedExportLineList().size()), columnValue);
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

  @Override
  public void close() throws AxelorException {
    try {
      baseTable.draw();
      contentStream.close();
      pdfDocument.save(exportFile);
      pdfDocument.close();
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
