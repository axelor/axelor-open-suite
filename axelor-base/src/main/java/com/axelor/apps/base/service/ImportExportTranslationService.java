package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ImportExportTranslation;
import java.io.IOException;
import java.nio.file.Path;

public interface ImportExportTranslationService {
  public void exportTranslations(ImportExportTranslation importExportTranslation)
      throws IOException;

  public Path importTranslations(ImportExportTranslation importExportTranslation);
}
