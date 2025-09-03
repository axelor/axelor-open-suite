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
package com.axelor.apps.base.service.print;

import com.axelor.apps.base.db.Print;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
    File exportFile = File.createTempFile("printTemplate", ".pdf");
    byte[] byteArray = pdfOutputStream.toByteArray();
    if (print.getPrintPdfFooter() == null || print.getHidePrintSettings()) {
      FileUtils.writeByteArrayToFile(exportFile, byteArray);
      return exportFile;
    }

    Rectangle rectangle = print.getDisplayTypeSelect() == 1 ? PageSize.A4 : PageSize.A4.rotate();
    try (PdfReader reader = new PdfReader(byteArray);
        Document document = new Document(rectangle);
        FileOutputStream os = new FileOutputStream(exportFile);
        PdfWriter writer = PdfWriter.getInstance(document, os)) {
      document.open();
      PdfPTable footerTable = getTable(document, print);
      PageEvent event = new PageEvent(footerTable);
      writer.setPageEvent(event);
      editDocument(reader, document, writer);
      document.close();
    }

    return exportFile;
  }

  protected void editDocument(PdfReader reader, Document document, PdfWriter writer) {
    for (int page = 1; page <= reader.getNumberOfPages(); page++) {
      document.newPage();
      PdfImportedPage importedPage = writer.getImportedPage(reader, page);
      PdfContentByte content = writer.getDirectContent();
      content.addTemplate(importedPage, 0, 0);
    }
  }

  protected PdfPTable getTable(Document document, Print print) {
    PdfPTable footerTable = new PdfPTable(1);
    footerTable.setTotalWidth(
        document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
    PdfPCell cell = getCell(print);
    footerTable.addCell(cell);
    return footerTable;
  }

  protected PdfPCell getCell(Print print) {
    String fontFamily =
        Optional.ofNullable(print.getFooterFontType())
            .filter(StringUtils::isNotEmpty)
            .orElse(BaseFont.TIMES_ROMAN);
    float fontSize =
        Optional.ofNullable(print.getFooterFontSize())
            .filter(size -> size.signum() > 0)
            .map(BigDecimal::floatValue)
            .orElse(DEFAULT_FONT_SIZE);
    Font font = FontFactory.getFont(fontFamily, fontSize, getCellFontColor(print));
    Phrase phrase = new Phrase(print.getPrintPdfFooter(), font);
    PdfPCell cell = new PdfPCell(phrase);
    cell.setHorizontalAlignment(getCellAlignment(print));
    cell.setBorder(0);
    return cell;
  }

  protected int getCellAlignment(Print print) {
    int allignment = Element.ALIGN_LEFT;
    if (print.getFooterTextAlignment() == null) {
      return allignment;
    }
    switch (print.getFooterTextAlignment()) {
      default:
      case "left":
        allignment = Element.ALIGN_LEFT;
        break;
      case "right":
        allignment = Element.ALIGN_RIGHT;
        break;
      case "center":
        allignment = Element.ALIGN_CENTER;
        break;
    }
    return allignment;
  }

  protected Color getCellFontColor(Print print) {
    Color color = Color.BLACK;

    if (print.getFooterFontColor() == null) {
      return color;
    }

    switch (print.getFooterFontColor()) {
      case "blue":
        color = Color.BLUE;
        break;
      case "cyan":
        color = Color.CYAN;
        break;
      case "dark-gray":
        color = Color.DARK_GRAY;
        break;
      case "gray":
        color = Color.GRAY;
        break;
      case "green":
        color = Color.GREEN;
        break;
      case "light-gray":
        color = Color.LIGHT_GRAY;
        break;
      case "magenta":
        color = Color.MAGENTA;
        break;
      case "orange":
        color = Color.ORANGE;
        break;
      case "pink":
        color = Color.PINK;
        break;
      case "red":
        color = Color.RED;
        break;
      case "white":
        color = Color.WHITE;
        break;
      case "yellow":
        color = Color.YELLOW;
        break;
      default:
        break;
    }
    return color;
  }

  protected void createPdfFromHtml(String html, OutputStream outputStream) {
    ITextRenderer renderer = new ITextRenderer();
    renderer.setDocumentFromString(html);
    renderer.layout();
    renderer.createPDF(outputStream);
  }

  protected class PageEvent extends PdfPageEventHelper {
    PdfPTable table;

    public PageEvent(PdfPTable table) {
      this.table = table;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
      table.writeSelectedRows(
          0,
          -1,
          document.left(),
          document.bottom() + table.getTotalHeight(),
          writer.getDirectContent());
    }
  }
}
