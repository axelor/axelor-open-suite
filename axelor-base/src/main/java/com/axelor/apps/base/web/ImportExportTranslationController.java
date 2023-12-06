package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.ImportExportTranslation;
import com.axelor.apps.base.db.repo.ImportExportTranslationRepository;
import com.axelor.apps.base.service.ImportExportTranslationService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportExportTranslationController {
  public void exportTranslation(ActionRequest request, ActionResponse response) throws IOException {
    ImportExportTranslation importExportTranslation =
        request.getContext().asType(ImportExportTranslation.class);
    importExportTranslation =
        Beans.get(ImportExportTranslationRepository.class).find(importExportTranslation.getId());
    ImportExportTranslationService importExportTranslationService =
        Beans.get(ImportExportTranslationService.class);
    try {
      importExportTranslationService.exportTranslations(importExportTranslation);
    } catch (AxelorException e) {
      Logger logger = LoggerFactory.getLogger(getClass());
      logger.error("File input error.", e);
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
    response.setInfo("File successfully exported.");
    response.setReload(true);
  }

  public void importTranslation(ActionRequest request, ActionResponse response) {
    ImportExportTranslation importExportTranslation =
        request.getContext().asType(ImportExportTranslation.class);
    importExportTranslation =
        Beans.get(ImportExportTranslationRepository.class).find(importExportTranslation.getId());
    ImportExportTranslationService importExportTranslationService =
        Beans.get(ImportExportTranslationService.class);
    Path path = null;
    try {
      path = importExportTranslationService.importTranslations(importExportTranslation);
    } catch (AxelorException e) {
      Logger logger = LoggerFactory.getLogger(getClass());
      logger.error("Read CSV file failed.", e);
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
    if (path == null) {
      response.setInfo("The import file is empty or it has error format.");
      response.setReload(true);
    } else {
      response.setInfo("File successfully imported.");
      response.setReload(true);
    }
  }
}
