/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.studio.web;

import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.ExportModule;
import com.axelor.studio.db.ImportModule;
import com.axelor.studio.db.repo.ExportModuleRepository;
import com.axelor.studio.db.repo.ImportModuleRepository;
import com.axelor.studio.exception.IExceptionMessage;
import com.axelor.studio.service.exporter.DataWriter;
import com.axelor.studio.service.exporter.ExcelWriter;
import com.axelor.studio.service.exporter.ExporterService;
import com.axelor.studio.service.importer.DataReaderService;
import com.axelor.studio.service.importer.ExcelReaderService;
import com.axelor.studio.service.importer.ImporterService;
import com.axelor.studio.service.module.ModuleExportService;
import com.axelor.studio.service.module.ModuleImportService;
import com.axelor.studio.service.module.ModuleInstallService;
import com.google.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class ExportImportController {

  private static final String buildLogFile = "AppBuildLog.txt";

  @Inject private ModuleExportService moduleExportService;

  @Inject private ModuleImportService moduleImportService;

  @Inject private ModuleInstallService moduleInstallService;

  @Inject private ExportModuleRepository exportModuleRepo;

  @Inject private ImportModuleRepository importModuleRepo;

  @Inject private ExporterService exporterService;

  @Inject private ImporterService importerService;

  @Inject private MetaFiles metaFiles;

  public void exportModule(ActionRequest request, ActionResponse response) {
    try {
      ExportModule exportModule = request.getContext().asType(ExportModule.class);
      exportModule = exportModuleRepo.find(exportModule.getId());

      MetaFile generatedFile =
          moduleExportService.exportModule(exportModule.getName(), exportModule.getGeneratedFile());

      response.setValue("generatedFile", generatedFile);
      response.setValue("exportDate", LocalDate.now());
    } catch (Exception e) {
      response.setError(e.getMessage());
      TraceBackService.trace(e);
    }
  }

  public void exportExcel(ActionRequest request, ActionResponse response) {
    try {
      ExportModule exportModule = request.getContext().asType(ExportModule.class);
      exportModule = exportModuleRepo.find(exportModule.getId());
      DataWriter writer = new ExcelWriter();
      DataReaderService reader = new ExcelReaderService();

      MetaFile exportFile = exporterService.export(null, writer, reader, false);

      response.setValue("generatedFile", exportFile);
      response.setValue("exportDate", LocalDate.now());
    } catch (Exception e) {
      response.setError(e.getMessage());
      TraceBackService.trace(response, e);
    }
  }

  public void importModule(ActionRequest request, ActionResponse response) {
    try {
      ImportModule importModule = request.getContext().asType(ImportModule.class);
      importModule = importModuleRepo.find(importModule.getId());

      moduleImportService.importModule(importModule);

      response.setValue("importDate", LocalDate.now());
      response.setFlash(I18n.get(IExceptionMessage.MODULE_IMPORTED));
    } catch (Exception e) {
      response.setError(e.getMessage());
      TraceBackService.trace(e);
    }
  }

  public void importExcel(ActionRequest request, ActionResponse response) {
    try {
      ImportModule importModule = request.getContext().asType(ImportModule.class);
      importModule = importModuleRepo.find(importModule.getId());
      MetaFile importFile = importModule.getDataFile();
      DataReaderService reader = new ExcelReaderService();

      File logFile = importerService.importData(reader, importFile, false);

      response.setValue("importDate", LocalDate.now());

      if (logFile != null) {
        response.setFlash(
            I18n.get("Input file is not valid. " + "Please check the log file generated"));
        response.setValue("errLogFile", metaFiles.upload(logFile));
      } else {
        response.setValue("errLogFile", null);
        response.setFlash(I18n.get("Imported successfully"));
      }

    } catch (Exception e) {
      response.setError(e.getMessage());
      TraceBackService.trace(response, e);
    }
  }

  public void restartServer(ActionRequest request, ActionResponse response) {

    try {
      String errorLog = moduleInstallService.buildApp();
      if (errorLog != null) {
        response.setFlash(I18n.get(IExceptionMessage.BUILD_LOG_CHECK));
        generateErrLogFile(response, errorLog);
      } else {
        moduleInstallService.restartServer(false);
      }

    } catch (Exception e) {
      response.setError(e.getMessage());
      TraceBackService.trace(e);
    }
  }

  private void generateErrLogFile(ActionResponse response, String errorLog) throws IOException {

    MetaFiles metaFiles = Beans.get(MetaFiles.class);
    MetaFile metaFile =
        metaFiles.upload(new ByteArrayInputStream(errorLog.getBytes()), buildLogFile);

    response.setValue("errLogFile", metaFile);
  }
}
