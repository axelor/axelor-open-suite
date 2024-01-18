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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Print;
import com.axelor.apps.base.db.PrintLine;
import com.axelor.apps.base.db.repo.PrintRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import java.io.File;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class PrintHtmlGenerationServiceImpl implements PrintHtmlGenerationService {
  @Override
  public String generateHtml(Print print, String attachmentPath) {
    StringBuilder htmlBuilder = new StringBuilder();

    addHtmlHeader(print, htmlBuilder);

    if (!print.getHidePrintSettings()) {
      Company company = print.getCompany();
      Integer logoPosition = print.getLogoPositionSelect();
      String pdfHeader = print.getPrintPdfHeader() != null ? print.getPrintPdfHeader() : "";
      String imageTag = getImageTag(attachmentPath, company, logoPosition);
      String logoWidth =
          print.getLogoWidth() != null ? "width: " + print.getLogoWidth() + ";" : "width: 50%;";
      String headerWidth =
          print.getHeaderContentWidth() != null
              ? "width: " + print.getHeaderContentWidth() + ";"
              : "width: 40%;";

      addLogo(logoPosition, htmlBuilder, logoWidth, imageTag, headerWidth, pdfHeader);

      if (!pdfHeader.isEmpty() || !imageTag.isEmpty()) {
        htmlBuilder.append("<hr/>");
      }
    }

    if (ObjectUtils.notEmpty(print)) {
      List<PrintLine> printLineList = print.getPrintLineList();
      if (CollectionUtils.isNotEmpty(printLineList)) {
        for (PrintLine printLine : printLineList) {
          addPrintLineContent(printLine, htmlBuilder);
        }
      }
    }

    htmlBuilder.append("</body>");
    htmlBuilder.append("</html>");

    return htmlToxhtml(htmlBuilder.toString());
  }

  protected void addHtmlHeader(Print print, StringBuilder htmlBuilder) {
    htmlBuilder.append("<!DOCTYPE html>");
    htmlBuilder.append("<html>");
    htmlBuilder.append("<head>");
    htmlBuilder.append("<title></title>");
    htmlBuilder.append("<meta charset=\"utf-8\"/>");
    htmlBuilder.append("<style type=\"text/css\">");
    if (print.getDisplayTypeSelect() == PrintRepository.DISPLAY_TYPE_LANDSCAPE) {
      htmlBuilder.append("@page { size: A4 landscape;}");
    }
    htmlBuilder.append("</style>");
    htmlBuilder.append("</head>");
    htmlBuilder.append("<body>");
  }

  protected String getImageTag(String attachmentPath, Company company, Integer logoPosition) {
    String imageTag = "";

    if (company != null
        && company.getLogo() != null
        && logoPosition != PrintRepository.LOGO_POSITION_NONE
        && new File(attachmentPath + company.getLogo().getFilePath()).exists()) {
      String width = company.getWidth() != 0 ? "width='" + company.getWidth() + "px'" : "";
      String height = company.getHeight() != 0 ? company.getHeight() + "px" : "71px";
      imageTag =
          "<img src='"
              + company.getLogo().getFilePath()
              + "' height='"
              + height
              + "' "
              + width
              + "/>";
    }
    return imageTag;
  }

  protected void addPrintLineContent(PrintLine printLine, StringBuilder htmlBuilder) {
    htmlBuilder.append(
        printLine.getIsWithPageBreakAfter()
            ? "<div style=\"page-break-after: always;\">"
            : "<div>");
    htmlBuilder.append("<table>");
    htmlBuilder.append("<tr>");
    if (printLine.getIsSignature()) {
      htmlBuilder.append("<td>&nbsp;</td></tr></table></div>");
      return;
    }
    Integer nbColumns = printLine.getNbColumns() == 0 ? 1 : printLine.getNbColumns();
    for (int i = 0; i < nbColumns; i++) {
      htmlBuilder.append("<td style=\"padding: 0px 10px 0px 10px\">");

      if (StringUtils.notEmpty(printLine.getTitle())) {
        htmlBuilder.append(printLine.getTitle());
      }
      if (StringUtils.notEmpty(printLine.getContent())) {
        htmlBuilder.append(printLine.getContent());
      }
      htmlBuilder.append("</td>");
    }
    htmlBuilder.append("</tr>");
    htmlBuilder.append("</table>");
    htmlBuilder.append("</div>");
  }

  protected void addLogo(
      Integer logoPosition,
      StringBuilder htmlBuilder,
      String logoWidth,
      String imageTag,
      String headerWidth,
      String pdfHeader) {
    switch (logoPosition) {
      case PrintRepository.LOGO_POSITION_LEFT:
        htmlBuilder.append(
            "<table style='width: 100%;'><tr><td valign='top' style='text-align: left; "
                + logoWidth
                + "'>"
                + imageTag
                + "</td><td valign='top' style='width: 10%'></td><td valign='top' style='text-align: left; "
                + headerWidth
                + "'>"
                + pdfHeader
                + "</td></tr></table>");
        break;
      case PrintRepository.LOGO_POSITION_CENTER:
        htmlBuilder.append(
            "<table style=\"width: 100%;\"><tr><td valign='top' style='width: 33.33%;'></td><td valign='top' style='text-align: center; width: 33.33%;'>"
                + imageTag
                + "</td><td valign='top' style='text-align: left; width: 33.33%;'>"
                + pdfHeader
                + "</td></tr></table>");
        break;
      case PrintRepository.LOGO_POSITION_RIGHT:
        htmlBuilder.append(
            "<table style='width: 100%;'><tr><td valign='top' style='text-align: left; "
                + headerWidth
                + "'>"
                + pdfHeader
                + "</td><td valign='top' style='width: 10%'></td><td valign='top' style='text-align: center; "
                + logoWidth
                + "'>"
                + imageTag
                + "</td></tr></table>");
        break;
      default:
        htmlBuilder.append(
            "<table style='width: 100%;'><tr><td style='width: 60%;'></td><td valign='top' style='text-align: left; width: 40%'>"
                + pdfHeader
                + "</td></tr></table>");
        break;
    }
  }

  protected String htmlToxhtml(String html) {
    Document document = Jsoup.parse(html);
    document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
    return document.html();
  }
}
