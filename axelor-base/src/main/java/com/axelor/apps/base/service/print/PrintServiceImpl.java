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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Print;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
  protected PrintHtmlGenerationService printHtmlGenerationService;
  protected PrintPdfGenerationService printPdfGenerationService;

  @Inject
  PrintServiceImpl(
      PrintRepository printRepo,
      MetaFiles metaFiles,
      PrintHtmlGenerationService printHtmlGenerationService,
      PrintPdfGenerationService printPdfGenerationService) {
    this.printRepo = printRepo;
    this.metaFiles = metaFiles;
    this.attachmentPath = AppService.getFileUploadDir();
    this.printHtmlGenerationService = printHtmlGenerationService;
    this.printPdfGenerationService = printPdfGenerationService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Map<String, Object> generatePDF(Print print) throws AxelorException {
    try {
      print = printRepo.find(print.getId());

      String html = printHtmlGenerationService.generateHtml(print, attachmentPath);
      ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
      File exportFile = printPdfGenerationService.generateFile(print, html, pdfOutputStream);

      String documentName =
          (StringUtils.notEmpty(print.getDocumentName()) ? print.getDocumentName() : "")
              + "-"
              + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)
              + FILE_EXTENSION_PDF;

      MetaFile metaFile = metaFiles.upload(exportFile);
      metaFile.setFileName(documentName);
      File file = MetaFiles.getPath(metaFile).toFile();

      String fileLink =
          PdfTool.getFileLinkFromPdfFile(
              PdfTool.printCopiesToFile(file, 1), metaFile.getFileName());

      attachFileToModel(print, file, metaFile, documentName);

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

  protected void attachFileToModel(Print print, File file, MetaFile metaFile, String documentName)
      throws ClassNotFoundException {
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
