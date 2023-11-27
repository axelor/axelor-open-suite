package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ImportExportTranslation;
import com.axelor.apps.base.db.repo.ImportExportTranslationRepository;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.axelor.utils.helpers.file.CsvHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;

public class ImportExportTranslationServiceImpl implements ImportExportTranslationService {

  protected MetaTranslationRepository metaTranslationRepository;
  protected ImportExportTranslationRepository importExportTranslationRepository;

  @Inject
  public ImportExportTranslationServiceImpl(
      MetaTranslationRepository metaTranslationRepository,
      ImportExportTranslationRepository importExportTranslationRepository) {
    this.metaTranslationRepository = metaTranslationRepository;
    this.importExportTranslationRepository = importExportTranslationRepository;
  }

  @Transactional
  @Override
  public String exportTranslations(ImportExportTranslation importExportTranslation)
      throws IOException {
    Long id = importExportTranslation.getId();

    // 1. find language code
    // 2. add headerline
    // 3. each message_key, find message_value
    // 4. add line to file

    Query query =
        JPA.em()
            .createNativeQuery(
                "select base_language.code "
                    + "from base_import_export_translation "
                    + "left join base_import_export_translation_language_set "
                    + "on base_import_export_translation_language_set.base_import_export_translation = base_import_export_translation.id "
                    + "left join base_language "
                    + "on base_import_export_translation_language_set.language_set = base_language.id "
                    + "where base_import_export_translation.id = :curId ")
            .setParameter("curId", importExportTranslation.getId());
    List resultList = query.getResultList();
    int languageNumber = resultList.size();
    String[] headers = new String[languageNumber + 1];
    headers[0] = "key";
    for (int i = 1; i < headers.length; i++) {
      headers[i] = (String) resultList.get(i - 1);
    }
    String[] row = new String[languageNumber + 1];
    List<String[]> list = new ArrayList<>();
    row[0] = "key";
    for (int i = 1; i < row.length; i++) {
      row[i] = "" + i;
    }
    list.add(row);
    String fileName = "Exported Translation" + " - " + java.time.LocalDateTime.now();
    File file = MetaFiles.createTempFile(fileName, ".csv").toFile();
    CsvHelper.csvWriter(file.getParent(), file.getName(), ';', headers, list);
    try (InputStream is = new FileInputStream(file)) {
      Beans.get(MetaFiles.class).attach(is, file.getName(), importExportTranslation);
    }
    importExportTranslationRepository.save(importExportTranslation);

    return file.getPath();
  }
}
