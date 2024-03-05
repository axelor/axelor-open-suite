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
package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.ImportExportTranslation;
import com.axelor.apps.base.db.repo.ImportExportTranslationRepository;
import com.axelor.apps.base.service.ImportExportTranslationService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.utils.FileExportTools;
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
              FileExportTools.copyFileToExportDir(path);
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
        () -> {
          Path path = null;
          try {
            path =
                importExportTranslationService.importTranslations(
                    Beans.get(ImportExportTranslationRepository.class)
                        .find(importExportTranslation.getId()));
            response.setInfo(I18n.get("The import file is empty or it has error format."));
            response.setReload(true);
            if (path == null) {
              response.setInfo(I18n.get("The import file is empty or it has error format."));
              response.setReload(true);
            } else {
              response.setInfo(I18n.get("File successfully imported."));
              response.setReload(true);
            }
          } catch (AxelorException e) {
            Logger logger = LoggerFactory.getLogger(getClass());
            logger.error("Read CSV file failed.", e);
            TraceBackService.trace(response, e, ResponseMessageType.ERROR);
          }
          return path;
        };
    ControllerCallableTool<Path> controllerCallableTool = new ControllerCallableTool<>();
    controllerCallableTool.runInSeparateThread(importTask, response);
  }
}
