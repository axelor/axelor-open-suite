package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportExportTranslation;
import com.axelor.apps.base.db.repo.ImportExportTranslationRepository;
import com.axelor.apps.base.service.ImportExportTranslationService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.io.IOException;
import java.nio.file.Path;

public class ImportExportTranslationController {
  public void exportTranslation(ActionRequest request, ActionResponse response)
      throws IOException, AxelorException {
    ImportExportTranslation importExportTranslation =
        request.getContext().asType(ImportExportTranslation.class);
    importExportTranslation =
        Beans.get(ImportExportTranslationRepository.class).find(importExportTranslation.getId());
    ImportExportTranslationService importExportTranslationService =
        Beans.get(ImportExportTranslationService.class);
    importExportTranslationService.exportTranslations(importExportTranslation);
    response.setReload(true);
  }

  public void importTranslation(ActionRequest request, ActionResponse response) {
    ImportExportTranslation importExportTranslation =
        request.getContext().asType(ImportExportTranslation.class);
    importExportTranslation =
        Beans.get(ImportExportTranslationRepository.class).find(importExportTranslation.getId());
    ImportExportTranslationService importExportTranslationService =
        Beans.get(ImportExportTranslationService.class);
    Path path = importExportTranslationService.importTranslations(importExportTranslation);
    if (path == null) {
      response.setInfo("The import file is empty or it has error format.");
      response.setReload(true);
    } else {
      response.setInfo("File successfully imported.");
      response.setReload(true);
    }
  }

  public void languageSetDomain(ActionRequest request, ActionResponse response) {
    String domain = " self.isNative = false ";
    response.setAttr("languageSet", "domain", domain);
  }
}
