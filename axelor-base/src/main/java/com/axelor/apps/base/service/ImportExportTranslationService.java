package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportExportTranslation;
import java.io.IOException;
import java.nio.file.Path;

public interface ImportExportTranslationService {
  public String exportTranslations(ImportExportTranslation importExportTranslation)
      throws IOException, AxelorException;

  public Path importTranslations(ImportExportTranslation importExportTranslation)
      throws AxelorException;

  /**
   * Copy the file from data.upload.dir to data.export.dir.
   *
   * @param uploadedFilePath
   * @throws IOException
   */
  public void copyFileFromUploadDirToExportDir(String uploadedFilePath) throws IOException;
}
