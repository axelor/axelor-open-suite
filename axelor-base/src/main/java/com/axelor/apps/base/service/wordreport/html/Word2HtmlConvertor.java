/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.wordreport.html;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Print;
import com.axelor.apps.base.db.repo.PrintRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.io.FilenameUtils;
import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

public class Word2HtmlConvertor {

  public File convertToHtml(File wordFile, Print print) throws Docx4JException, IOException {
    if (!wordFile.isFile()) {
      return null;
    }

    File htmlFile = null;
    boolean hasWatermark = StringUtils.notEmpty(print.getWatermarkText());

    WordprocessingMLPackage wordprocessingMLPackage = null;
    if (FilenameUtils.getExtension(wordFile.getName()).equalsIgnoreCase("DOCX")) {

      wordprocessingMLPackage = WordprocessingMLPackage.load(wordFile);
      String imgDir =
          AppSettings.get().getPath("file.upload.dir", "").concat(File.separator + "tmp");
      String imgPathUri = "file://".concat(imgDir);

      HTMLSettings html = Docx4J.createHTMLSettings();
      // set picture directory address
      html.setImageDirPath(imgDir);
      html.setImageTargetUri(imgPathUri);
      html.setWmlPackage(wordprocessingMLPackage);
      // set header, footer and watermark
      html.setUserBodyTop(getBodyTop(print, hasWatermark) + getBodyTail(print));
      htmlFile =
          MetaFiles.createTempFile(I18n.get(print.getMetaModel().getName()), ".html").toFile();
      OutputStream os = new FileOutputStream(htmlFile);

      // set the output
      Docx4jProperties.setProperty("docx4j.Convert.Out.HTML.OutputMethodXML", true);
      Docx4J.toHTML(html, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
      IOUtils.closeQuietly(os);

      if (wordprocessingMLPackage.getMainDocumentPart().getFontTablePart() != null) {
        wordprocessingMLPackage
            .getMainDocumentPart()
            .getFontTablePart()
            .deleteEmbeddedFontTempFiles();
      }
    }
    // override css
    this.processHtml(htmlFile, hasWatermark, print);

    return htmlFile;
  }

  private String getBodyTop(Print print, boolean hasWatermark) {
    String bodyTop = "";
    bodyTop = bodyTop.concat(hasWatermark ? "<object type=\"watermark\"></object>" : "");
    if (StringUtils.notEmpty(print.getPrintPdfHeader())) {
      String htmlHeader = generateHeaderHtml(print);
      int fontSize = 10;
      if (htmlHeader.contains("size")) {
        String size =
            htmlHeader
                .substring(htmlHeader.indexOf("size") + 6)
                .substring(0, htmlHeader.substring(htmlHeader.indexOf("size") + 6).indexOf("\""));
        fontSize = Integer.valueOf(size);
      }
      String headerElement =
          "\n  <header style=\"position: running(header);font-size: "
              + fontSize
              + "px\">\n\t"
              + htmlHeader
              + "\n  </header>";
      bodyTop = bodyTop.concat(headerElement);
    }

    return bodyTop;
  }

  private String getBodyTail(Print print) {
    String bodyTail = "";
    if (StringUtils.notEmpty(print.getPrintPdfFooter())) {
      String footerElement =
          "\n  <footer style=\"position: running(footer)\">\n\t"
              + generateFooterHtml(print)
              + "\n  </footer>";
      bodyTail = bodyTail.concat(footerElement);
    }
    return bodyTail;
  }

  private String generateHeaderHtml(Print print) {
    StringBuilder htmlBuilder = new StringBuilder();
    String attachmentPath = AppSettings.get().getPath("file.upload.dir", "");
    if (attachmentPath != null) {
      attachmentPath =
          attachmentPath.endsWith(File.separator)
              ? attachmentPath
              : attachmentPath + File.separator;
    }

    if (Boolean.FALSE.equals(print.getHidePrintSettings())) {
      Company company = print.getCompany();
      Integer logoPosition = print.getLogoPositionSelect();
      String pdfHeader = print.getPrintPdfHeader() != null ? print.getPrintPdfHeader() : "";
      String imageTag = "";
      String logoWidth =
          print.getLogoWidth() != null ? "width: " + print.getLogoWidth() + ";" : "width: 50%;";
      String headerWidth =
          print.getHeaderContentWidth() != null
              ? "width: " + print.getHeaderContentWidth() + ";"
              : "width: 40%;";

      if (company != null
          && company.getLogo() != null
          && logoPosition != PrintRepository.LOGO_POSITION_NONE
          && new File(attachmentPath + company.getLogo().getFilePath()).exists()) {
        String width = company.getWidth() != 0 ? "width='" + company.getWidth() + "px'" : "";
        String height = company.getHeight() != 0 ? company.getHeight() + "px" : "71px";
        imageTag =
            "<img src='"
                + MetaFiles.getPath(company.getLogo()).toUri()
                + "' height='"
                + height
                + "' "
                + width
                + "/>";
      }

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

    return htmlBuilder.toString();
  }

  private String generateFooterHtml(Print print) {

    StringBuilder htmlBuilder = new StringBuilder();

    if (Boolean.FALSE.equals(print.getHidePrintSettings())) {
      String pdfFooter = print.getPrintPdfFooter() != null ? print.getPrintPdfFooter() : "";
      String fontSize =
          print.getFooterHeight() != null
              ? "font-size: " + print.getFooterFontSize().intValue() + "px; "
              : "font-size: 20; ";
      String fontFamily =
          ObjectUtils.notEmpty(print.getFooterFontType())
              ? "font-family: " + print.getFooterFontType() + "; "
              : "font-family: Arial; ";
      String footerAlign =
          print.getFooterTextAlignment() != null
              ? "text-align: " + print.getFooterTextAlignment() + "; "
              : "text-align: left; ";
      String fontColor =
          ObjectUtils.notEmpty(print.getFooterFontColor())
              ? "color: " + print.getFooterFontColor() + "; "
              : "color: black; ";
      String textDecoration =
          Boolean.TRUE.equals(print.getIsFooterUnderLine()) ? "text-decoration: underline;" : "";

      htmlBuilder.append(
          "<table style='width: 100%;'><tr><td valign='top' style='"
              + footerAlign
              + "width: 100%; "
              + fontSize
              + fontFamily
              + fontColor
              + textDecoration
              + "'>"
              + pdfFooter
              + "</td></tr></table>");
    }
    return htmlBuilder.toString();
  }

  private void processHtml(File htmlFile, boolean hasWatermark, Print print) throws IOException {
    if (htmlFile != null && htmlFile.isFile()) {
      String htmlString = new String(java.nio.file.Files.readAllBytes(htmlFile.toPath()));

      Document doc = Jsoup.parse(htmlString, "utf-8", Parser.xmlParser());
      doc.outputSettings().prettyPrint(false);
      doc.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
      doc.outputSettings().charset("UTF-8");

      Element cssStyle = doc.select("style").first();

      int headerHeight =
          ObjectUtils.notEmpty(print.getPrintPdfHeader()) ? print.getHeaderHeight() : 80;
      int footerHeight =
          ObjectUtils.notEmpty(print.getPrintPdfFooter()) ? print.getFooterHeight() : 80;

      String finalCss = "";
      String pageType = "A4";
      pageType =
          print.getDisplayTypeSelect() == PrintRepository.DISPLAY_TYPE_LANDSCAPE
              ? "A4 landscape"
              : pageType;
      String headerFooterCss =
          "@page {\n"
              + "  size: "
              + pageType
              + ";\n"
              + "  margin-top: "
              + headerHeight
              + "px;\n"
              + "  margin-left: 40px;\n"
              + "  margin-right: 40px;\n"
              + "  margin-bottom: "
              + footerHeight
              + "px;      /** should be footer height + page bottom-margin */\n"
              + "\n"
              + "  @top-left {\n"
              + "    content: element(header);\n"
              + "  }\n"
              + "\n"
              + "  @bottom-left {\n"
              + "    content: element(footer);\n"
              + "  }\n"
              + "}"
              + "body {\n"
              + "  margin: 0;\n"
              + "  padding: 0;\n"
              + "}\n"
              + "\n"
              + "header,\n"
              + "footer {\n"
              + "  width: 100%;\n"
              + "}\n"
              + "\n"
              + "header {\n"
              + "  margin-bottom: 16px;	\n"
              + "  border-bottom: 1px solid black;\n"
              + "}\n"
              + "\n"
              + "footer {\n"
              + "  margin-top: 1px;\n"
              + "  border-top: 1px solid black;\n"
              + "}";
      finalCss = finalCss.concat(headerFooterCss);

      String defaultCss =
          "\n\n/*element styles*/ \n.del  {text-decoration:line-through;color:red;} \n.ins {text-decoration:none;background:#c0ffc0;padding:1px;}"
              + " \n\n/* PARAGRAPH STYLES */ \n"
              + ".DocDefaults {display:block;padding:0;margin:0;line-height: 1.6;font-size: 10.0pt;}\n"
              + ".Normal {}\n"
              + ".TableContents {display:block;}\n"
              + "\n/* CHARACTER STYLES */ \nspan.DefaultParagraphFont {display:inline;}\n"
              + "\n/* Custom CSS */\nbody{width:100%;height:100%;line-height:1.6;font-size:10px;}\n";

      finalCss = finalCss.concat(defaultCss);

      if (hasWatermark) {
        String watermarkCss =
            "object[type=\"watermark\"] {\n"
                + "  position: fixed;\n"
                + "  display: block;\n"
                + "  width: 100%;\n"
                + "  height: 100%;\n"
                + "  transform: rotate("
                + print.getWatermarkAngle()
                + "deg);\n"
                + "  z-index: 1000;\n"
                + "  left: 0;\n"
                + "  top: 0;\n"
                + "}";

        finalCss = finalCss.concat(watermarkCss);
      }
      cssStyle.text(finalCss);

      Files.write(doc.toString().getBytes(), htmlFile);
    }
  }
}
