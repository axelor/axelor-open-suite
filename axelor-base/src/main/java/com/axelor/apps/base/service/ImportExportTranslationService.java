package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ImportExportTranslation;
import java.io.IOException;

public interface ImportExportTranslationService {
  public String exportTranslations(ImportExportTranslation importExportTranslation)
      throws IOException;
}
