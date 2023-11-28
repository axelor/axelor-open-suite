package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportExportTranslation;
import com.axelor.apps.base.db.repo.ImportExportTranslationRepository;
import com.axelor.apps.base.service.ImportExportTranslationService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
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
    importExportTranslationService.exportTranslations(importExportTranslation);
  }

  public void importTranslation(ActionRequest request, ActionResponse response) {
    ImportExportTranslation importExportTranslation =
        request.getContext().asType(ImportExportTranslation.class);
    importExportTranslation =
        Beans.get(ImportExportTranslationRepository.class).find(importExportTranslation.getId());
    MetaFile uploadFile = importExportTranslation.getUploadFile();

    System.out.println(uploadFile.getFileName());

    System.out.println(uploadFile.getFilePath());
  }
}
