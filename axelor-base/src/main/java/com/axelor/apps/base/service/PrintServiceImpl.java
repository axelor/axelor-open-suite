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

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Print;
import com.axelor.apps.base.db.repo.PrintRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.report.IReport;
import com.axelor.apps.report.engine.ReportSettings;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintServiceImpl implements PrintService {

  private final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    print = printRepo.find(print.getId());
    String documentName =
        StringUtils.notEmpty(print.getDocumentName()) ? print.getDocumentName() : "";

    String reportModele =
        print.getDisplayTypeSelect() == PrintRepository.DISPLAY_TYPE_LANDSCAPE
            ? IReport.PRINT_LANDSCAPE
            : IReport.PRINT_PORTRAIT;

    ReportSettings reportSettings =
        ReportFactory.createReport(reportModele, documentName + "-${date}")
            .addParam("PrintId", print.getId())
            .generate();

    if (ObjectUtils.notEmpty(reportSettings)) {

      if (print.getAttach() || StringUtils.notEmpty(print.getMetaFileField())) {
        File file = reportSettings.getFile();
        if (file.exists()) {
          try {
            try (InputStream is = new FileInputStream(file)) {
              MetaFile metaFile = metaFiles.upload(is, documentName);
              Class<? extends Model> modelClass =
                  (Class<? extends Model>) Class.forName(print.getMetaModel().getFullName());
              Model objectModel = JPA.find(modelClass, print.getObjectId());

              if (print.getAttach()) {
                if (ObjectUtils.notEmpty(print.getMetaModel())) {
                  metaFiles.attach(metaFile, documentName, objectModel);
                }
              }
              if (StringUtils.notEmpty(print.getMetaFileField())) {
                saveMetaFileInModel(modelClass, objectModel, metaFile, print.getMetaFileField());
              }
            }

          } catch (IOException | ClassNotFoundException e) {
            throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
          }
        }
      }

      if (CollectionUtils.isNotEmpty(print.getPrintSet())) {
        for (Print subPrint : print.getPrintSet()) {
          generatePDF(subPrint);
        }
      }

      String pdfPath = reportSettings.getFileLink();
      if (StringUtils.notEmpty(pdfPath)) {
        return ActionView.define(documentName).add("html", pdfPath).map();
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, IExceptionMessage.PRINT_ERROR);
      }
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, IExceptionMessage.PRINT_ERROR);
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
