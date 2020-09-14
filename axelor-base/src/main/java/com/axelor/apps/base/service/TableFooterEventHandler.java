/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Print;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import java.io.IOException;
import java.math.BigDecimal;

public class TableFooterEventHandler implements IEventHandler {
  protected Table tableFooter;
  protected Document doc;
  protected Print print;

  public TableFooterEventHandler(Document doc, Print print) throws IOException {
    this.doc = doc;
    this.print = print;
    String footerTextAlignment = print.getFooterTextAlignment();
    String footerFontColor = print.getFooterFontColor();

    tableFooter = new Table(1);
    tableFooter.setWidth(
        doc.getPdfDocument().getDefaultPageSize().getRight()
            - doc.getPdfDocument().getDefaultPageSize().getLeft()
            - doc.getLeftMargin()
            - doc.getRightMargin());

    Cell cellFooter = new Cell();
    cellFooter.setBorder(Border.NO_BORDER);
    if (footerTextAlignment != null) {
      this.setCellFooterTextAlignment(cellFooter, footerTextAlignment);
    }
    cellFooter.setFont(
        PdfFontFactory.createFont(
            print.getFooterFontType() != null
                ? print.getFooterFontType()
                : StandardFonts.TIMES_ROMAN));
    cellFooter.setFontSize(
        print.getFooterFontSize().compareTo(BigDecimal.ZERO) > 0
            ? print.getFooterFontSize().floatValue()
            : 10);
    if (footerFontColor != null) {
      this.setCellFooterFontColor(cellFooter, footerFontColor);
    }
    if (print.getIsFooterUnderLine()) {
      cellFooter.setUnderline();
    }
    cellFooter.add(
        new Paragraph(print.getPrintPdfFooter() != null ? print.getPrintPdfFooter() : ""));
    tableFooter.addCell(cellFooter);
  }

  @SuppressWarnings("resource")
  @Override
  public void handleEvent(Event event) {
    PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
    PdfDocument pdfDoc = docEvent.getDocument();
    PdfPage page = docEvent.getPage();
    PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
    Rectangle rect1 =
        new Rectangle(
            pdfDoc.getDefaultPageSize().getX() + doc.getLeftMargin(),
            pdfDoc.getDefaultPageSize().getBottom() - doc.getBottomMargin(),
            100,
            90);
    new Canvas(canvas, pdfDoc, rect1).add(tableFooter);
  }

  private void setCellFooterTextAlignment(Cell cellFooter, String footerTextAlignment) {

    switch (footerTextAlignment) {
      case "left":
        cellFooter.setTextAlignment(TextAlignment.LEFT);
        break;
      case "center":
        cellFooter.setTextAlignment(TextAlignment.CENTER);
        break;
      case "right":
        cellFooter.setTextAlignment(TextAlignment.RIGHT);
        break;
      default:
        break;
    }
  }

  private void setCellFooterFontColor(Cell cellFooter, String footerFontColor) {

    switch (footerFontColor) {
      case "blue":
        cellFooter.setFontColor(ColorConstants.BLUE);
        break;
      case "cyan":
        cellFooter.setFontColor(ColorConstants.CYAN);
        break;
      case "dark-gray":
        cellFooter.setFontColor(ColorConstants.DARK_GRAY);
        break;
      case "gray":
        cellFooter.setFontColor(ColorConstants.GRAY);
        break;
      case "green":
        cellFooter.setFontColor(ColorConstants.GREEN);
        break;
      case "light-gray":
        cellFooter.setFontColor(ColorConstants.LIGHT_GRAY);
        break;
      case "magneta":
        cellFooter.setFontColor(ColorConstants.MAGENTA);
        break;
      case "orange":
        cellFooter.setFontColor(ColorConstants.ORANGE);
        break;
      case "pink":
        cellFooter.setFontColor(ColorConstants.PINK);
        break;
      case "red":
        cellFooter.setFontColor(ColorConstants.RED);
        break;
      case "white":
        cellFooter.setFontColor(ColorConstants.WHITE);
        break;
      case "yellow":
        cellFooter.setFontColor(ColorConstants.YELLOW);
        break;
      default:
        break;
    }
  }
}
