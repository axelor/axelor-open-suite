package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportExportTranslation;
import com.axelor.apps.base.db.repo.ImportExportTranslationRepository;
import com.axelor.apps.base.service.ImportExportTranslationService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.io.IOException;

public class ImportExportTranslationController {
  public void exportTranslation(ActionRequest request, ActionResponse response)
      throws IOException, AxelorException {
    ImportExportTranslation importExportTranslation =
        request.getContext().asType(ImportExportTranslation.class);
    importExportTranslation =
        Beans.get(ImportExportTranslationRepository.class).find(importExportTranslation.getId());
    ImportExportTranslationService importExportTranslationService =
        Beans.get(ImportExportTranslationService.class);

    String filePath = importExportTranslationService.exportTranslations(importExportTranslation);
    System.out.println(filePath);
  }
}
