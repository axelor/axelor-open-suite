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
import com.axelor.apps.base.db.PrintLine;
import com.axelor.apps.base.db.repo.PrintRepository;
import com.axelor.apps.tool.file.PdfTool;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.itextpdf.html2pdf.HtmlConverter;
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

  @Inject
  PrintServiceImpl(PrintRepository printRepo, MetaFiles metaFiles) {
    this.printRepo = printRepo;
    this.metaFiles = metaFiles;
  }

  @Override
  @Transactional
  public Map<String, Object> generatePDF(Print print) throws AxelorException {
    try {
      print = printRepo.find(print.getId());
      String html = generateHtml(print);

      ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
      HtmlConverter.convertToPdf(html, pdfOutputStream);

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

    if (ObjectUtils.notEmpty(print)) {
      List<PrintLine> printLineList = print.getPrintLineList();
      if (CollectionUtils.isNotEmpty(printLineList)) {
        for (PrintLine printLine : printLineList) {
          if (StringUtils.notEmpty(printLine.getTitle())) {
            htmlBuilder.append(printLine.getTitle());
          }
          if (StringUtils.notEmpty(printLine.getContent())) {
            htmlBuilder.append(printLine.getContent());
          }
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
