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
package com.axelor.apps.base.service.print;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import com.axelor.apps.base.db.Print;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.xhtmlrenderer.pdf.ITextRenderer;

public class PrintPdfGenerationServiceImpl implements PrintPdfGenerationService {

  public static final int MARGIN = 50;
  public static final int BOTTOM_MARGIN = 0;
  public static final float HEIGHT_FOOTER = 5;
  public static final float DEFAULT_FONT_SIZE = 10;
  public static final float FOOTER_Y_POS = 40;

  public File generateFile(Print print, String html, ByteArrayOutputStream pdfOutputStream)
      throws IOException {
    createPdfFromHtml(html, pdfOutputStream);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());
    File exportFile = File.createTempFile("printTemplate", ".pdf");
    try (PDDocument doc = PDDocument.load(inputStream)) {
      if (print.getPrintPdfFooter() != null && !print.getHidePrintSettings()) {
        for (PDPage page : doc.getPages()) {
          createFooter(print, page, doc);
        }
      }
      doc.save(exportFile);
    }
    return exportFile;
  }

  protected void createFooter(Print print, PDPage page, PDDocument doc) throws IOException {
    Row<PDPage> row;
    float tableWidth = page.getMediaBox().getWidth() - (2 * MARGIN);
    float yStartNewPage = page.getMediaBox().getHeight() - (2 * MARGIN);

    BaseTable baseTable =
        new BaseTable(
            FOOTER_Y_POS, yStartNewPage, BOTTOM_MARGIN, tableWidth, MARGIN, doc, page, false, true);

    row = baseTable.createRow(HEIGHT_FOOTER);
    createFooterCell(print, row);
    baseTable.draw();
  }

  protected void createFooterCell(Print print, Row<PDPage> row) {
    Cell<PDPage> cell;
    cell = row.createCell(100f, print.getPrintPdfFooter());
    cell.setFontSize(
        print.getFooterFontSize().compareTo(BigDecimal.ZERO) > 0
            ? print.getFooterFontSize().floatValue()
            : DEFAULT_FONT_SIZE);
    setCellAlignment(cell, print);
    setCellFontColor(cell, print);
    setCellFont(cell, print);
  }

  protected void setCellAlignment(Cell cell, Print print) {
    if (print.getFooterTextAlignment() == null) {
      cell.setAlign(HorizontalAlignment.LEFT);
      return;
    }
    switch (print.getFooterTextAlignment()) {
      default:
      case "left":
        cell.setAlign(HorizontalAlignment.LEFT);
        break;
      case "right":
        cell.setAlign(HorizontalAlignment.RIGHT);
        break;
      case "center":
        cell.setAlign(HorizontalAlignment.CENTER);
        break;
    }
  }

  protected void setCellFontColor(Cell cell, Print print) {
    if (print.getFooterFontColor() == null) {
      return;
    }
    switch (print.getFooterFontColor()) {
      case "blue":
        cell.setTextColor(Color.BLUE);
        break;
      case "cyan":
        cell.setTextColor(Color.CYAN);
        break;
      case "dark-gray":
        cell.setTextColor(Color.DARK_GRAY);
        break;
      case "gray":
        cell.setTextColor(Color.GRAY);
        break;
      case "green":
        cell.setTextColor(Color.GREEN);
        break;
      case "light-gray":
        cell.setTextColor(Color.LIGHT_GRAY);
        break;
      case "magenta":
        cell.setTextColor(Color.MAGENTA);
        break;
      case "orange":
        cell.setTextColor(Color.ORANGE);
        break;
      case "pink":
        cell.setTextColor(Color.PINK);
        break;
      case "red":
        cell.setTextColor(Color.RED);
        break;
      case "white":
        cell.setTextColor(Color.WHITE);
        break;
      case "yellow":
        cell.setTextColor(Color.YELLOW);
        break;
      default:
        break;
    }
  }

  protected void setCellFont(Cell cell, Print print) {
    if (print.getFooterFontType() == null) {
      return;
    }
    switch (print.getFooterFontType()) {
      default:
      case "Times":
        return;
      case "Courier":
        cell.setFont(PDType1Font.COURIER);
        break;
      case "Courier-Bold":
        cell.setFont(PDType1Font.COURIER_BOLD);
        break;
      case "Courier-Oblique":
        cell.setFont(PDType1Font.COURIER_OBLIQUE);
        break;
      case "Courier-BoldOblique":
        cell.setFont(PDType1Font.COURIER_BOLD_OBLIQUE);
        break;
      case "Helvetica":
        cell.setFont(PDType1Font.HELVETICA);
        break;
      case "Helvetica-Bold":
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        break;
      case "Helvetica-Oblique":
        cell.setFont(PDType1Font.HELVETICA_OBLIQUE);
        break;
      case "Helvetica-BoldOblique":
        cell.setFont(PDType1Font.HELVETICA_BOLD_OBLIQUE);
        break;
      case "Symbol":
        cell.setFont(PDType1Font.SYMBOL);
        break;
      case "Times-Roman":
        cell.setFont(PDType1Font.TIMES_ROMAN);
        break;
      case "Times-Bold":
        cell.setFont(PDType1Font.TIMES_BOLD);
        break;
      case "Times-Italic":
        cell.setFont(PDType1Font.TIMES_ITALIC);
        break;
      case "Times-BoldItalic":
        cell.setFont(PDType1Font.TIMES_BOLD_ITALIC);
        break;
      case "ZapfDingbats":
        cell.setFont(PDType1Font.ZAPF_DINGBATS);
        break;
    }
  }

  protected void createPdfFromHtml(String html, OutputStream outputStream) {
    ITextRenderer renderer = new ITextRenderer();
    renderer.setDocumentFromString(html);
    renderer.layout();
    renderer.createPDF(outputStream);
  }
}
