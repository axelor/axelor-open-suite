/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Print;
import com.axelor.apps.base.db.PrintLine;
import com.axelor.apps.base.db.repo.PrintRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.studio.app.service.AppService;
import com.axelor.utils.file.PdfTool;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintServiceImpl implements PrintService {

  private final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected static final String FILE_EXTENSION_PDF = ".pdf";

  protected PrintRepository printRepo;
  protected MetaFiles metaFiles;
  protected String attachmentPath;

  @Inject
  PrintServiceImpl(PrintRepository printRepo, MetaFiles metaFiles) throws AxelorException {
    this.printRepo = printRepo;
    this.metaFiles = metaFiles;
    this.attachmentPath = AppService.getFileUploadDir();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Map<String, Object> generatePDF(Print print) throws AxelorException {
    try {
      print = printRepo.find(print.getId());
      String html = generateHtml(print);

      ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
      try (PdfDocument pdfDoc = new PdfDocument(new PdfWriter(pdfOutputStream))) {
        pdfDoc.setDefaultPageSize(
            print.getDisplayTypeSelect() == PrintRepository.DISPLAY_TYPE_LANDSCAPE
                ? PageSize.A4.rotate()
                : PageSize.A4);

        if (print.getPrintPdfFooter() != null && !print.getHidePrintSettings()) {
          com.itextpdf.layout.Document doc = new com.itextpdf.layout.Document(pdfDoc);
          pdfDoc.addEventHandler(
              PdfDocumentEvent.END_PAGE, new TableFooterEventHandler(doc, print));
        }

        ConverterProperties converterProperties = new ConverterProperties();
        converterProperties.setBaseUri(attachmentPath);
        HtmlConverter.convertToPdf(html, pdfDoc, converterProperties);
      }

      String documentName =
          (StringUtils.notEmpty(print.getDocumentName()) ? print.getDocumentName() : "")
              + "-"
              + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
              + FILE_EXTENSION_PDF;
      InputStream pdfInputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());
      MetaFile metaFile = metaFiles.upload(pdfInputStream, documentName);
      File file = MetaFiles.getPath(metaFile).toFile();

      String fileLink =
          PdfTool.getFileLinkFromPdfFile(
              PdfTool.printCopiesToFile(file, 1), metaFile.getFileName());

      if (ObjectUtils.notEmpty(file)
          && file.exists()
          && (print.getAttach() || StringUtils.notEmpty(print.getMetaFileField()))) {

        Class<? extends Model> modelClass =
            (Class<? extends Model>) Class.forName(print.getMetaModel().getFullName());
        Model objectModel = JPA.find(modelClass, print.getObjectId());

        if (ObjectUtils.notEmpty(objectModel)) {
          if (print.getAttach()) {
            metaFiles.attach(metaFile, documentName, objectModel);
          }
          if (StringUtils.notEmpty(print.getMetaFileField())) {
            saveMetaFileInModel(modelClass, objectModel, metaFile, print.getMetaFileField());
          }
        }
      }

      if (CollectionUtils.isNotEmpty(print.getPrintSet())) {
        for (Print subPrint : print.getPrintSet()) {
          generatePDF(subPrint);
        }
      }
      return ActionView.define(documentName).add("html", fileLink).map();
    } catch (IOException | ClassNotFoundException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    }
  }

  protected void saveMetaFileInModel(
      Class<? extends Model> modelClass,
      Model objectModel,
      MetaFile metaFile,
      String metaFileFieldName) {
    Mapper mapper = Mapper.of(modelClass);
    Property p = mapper.getProperty(metaFileFieldName);
    if (ObjectUtils.notEmpty(p)) {
      p.set(objectModel, metaFile);
      JPA.save(objectModel);
    }
  }

  protected String generateHtml(Print print) {
    StringBuilder htmlBuilder = new StringBuilder();

    htmlBuilder.append("<!DOCTYPE html>");
    htmlBuilder.append("<html>");
    htmlBuilder.append("<head>");
    htmlBuilder.append("<title></title>");
    htmlBuilder.append("<meta charset=\"utf-8\"/>");
    htmlBuilder.append("<style type=\"text/css\">");
    htmlBuilder.append("</style>");
    htmlBuilder.append("</head>");
    htmlBuilder.append("<body>");

    if (!print.getHidePrintSettings()) {
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
                + company.getLogo().getFilePath()
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
      if (!pdfHeader.isEmpty() || !imageTag.isEmpty()) {
        htmlBuilder.append("<hr>");
      }
    }

    if (ObjectUtils.notEmpty(print)) {
      List<PrintLine> printLineList = print.getPrintLineList();
      if (CollectionUtils.isNotEmpty(printLineList)) {
        for (PrintLine printLine : printLineList) {
          htmlBuilder.append(
              printLine.getIsWithPageBreakAfter()
                  ? "<div style=\"page-break-after: always;\">"
                  : "<div>");
          htmlBuilder.append("<table>");
          htmlBuilder.append("<tr>");
          if (printLine.getIsSignature()) {
            htmlBuilder.append("<td>&nbsp;</td></tr></table></div>");
            continue;
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
      }
    }

    htmlBuilder.append("</body>");
    htmlBuilder.append("</html>");
    return htmlBuilder.toString();
  }

  @Override
  public void attachMetaFiles(Print print, Set<MetaFile> metaFiles) {
    Preconditions.checkNotNull(print.getId());

    if (metaFiles == null || metaFiles.isEmpty()) {
      return;
    }

    LOG.debug("Add metafiles to object {} : {}", Print.class.getName(), print.getId());

    for (MetaFile metaFile : metaFiles) {
      Beans.get(MetaFiles.class).attach(metaFile, metaFile.getFileName(), print);
    }
  }
}
