package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.ImportExportTranslation;
import com.axelor.apps.base.db.repo.ImportExportTranslationRepository;
import com.axelor.apps.base.service.ImportExportTranslationService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportExportTranslationController {

  public void exportTranslation(ActionRequest request, ActionResponse response) {
    ImportExportTranslation importExportTranslation =
        request.getContext().asType(ImportExportTranslation.class);
    ImportExportTranslationService importExportTranslationService =
        Beans.get(ImportExportTranslationService.class);

    Callable<String> exportTask =
        () -> {
          try {
            String path =
                importExportTranslationService.exportTranslations(
                    Beans.get(ImportExportTranslationRepository.class)
                        .find(importExportTranslation.getId()));
            if (path != null) {
              String[] filePath = path.split("/");
              response.setExportFile(filePath[filePath.length - 1]);
              response.setInfo(I18n.get("File successfully exported."));
            }
            response.setReload(true);
            return path;
          } catch (AxelorException e) {
            Logger logger = LoggerFactory.getLogger(getClass());
            logger.error("File input error.", e);
            TraceBackService.trace(response, e, ResponseMessageType.ERROR);
            return null;
          }
        };
    ControllerCallableTool<String> controllerCallableTool = new ControllerCallableTool<>();
    controllerCallableTool.runInSeparateThread(exportTask, response);
  }

  public void importTranslation(ActionRequest request, ActionResponse response) {
    ImportExportTranslation importExportTranslation =
        request.getContext().asType(ImportExportTranslation.class);
    ImportExportTranslationService importExportTranslationService =
        Beans.get(ImportExportTranslationService.class);
    Callable<Path> importTask =
        () -> importExportTranslationService.importTranslations(
                  Beans.get(ImportExportTranslationRepository.class)
                      .find(importExportTranslation.getId()));
      try {
        ControllerCallableTool<Path> controllerCallableTool = new ControllerCallableTool<>();
          Path path = controllerCallableTool.runInSeparateThread(importTask, response);
          if (path != null)  {
              response.setInfo(I18n.get("File successfully imported."));
          }
      } catch (Exception e) {
          Logger logger = LoggerFactory.getLogger(getClass());
          logger.error("Read CSV file failed.", e);
          TraceBackService.trace(response, e, ResponseMessageType.ERROR);
        } finally {
            response.setReload(true);
        }
  }
}
