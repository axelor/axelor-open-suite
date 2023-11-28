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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    Query languageQuery =
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
    List<String> resultList = languageQuery.getResultList();
    int languageNumber = resultList.size();
    String[] headers = new String[languageNumber + 1];
    headers[0] = "key";
    for (int i = 1; i < headers.length; i++) {
      headers[i] = resultList.get(i - 1);
    }

    Query messageQuery =
        JPA.em()
            .createNativeQuery(
                "select language, message_key, message_value "
                    + "from META_TRANSLATION "
                    + "where meta_translation.language = ANY ( "
                    + "select base_language.code "
                    + "from base_import_export_translation "
                    + "left join base_import_export_translation_language_set "
                    + "on base_import_export_translation_language_set.base_import_export_translation = base_import_export_translation.id "
                    + "left join base_language "
                    + "on base_import_export_translation_language_set.language_set = base_language.id "
                    + "where base_import_export_translation.id = :curId ) "
                    + "limit 20 "
                    + "offset 0 ")
            .setParameter("curId", importExportTranslation.getId());

    List<Object[]> messageQueryResultList = messageQuery.getResultList();
    Object[] messageRecord;
    String messageKey;
    String languageCode;
    String messageValue;

    List<String[]> translationList = new ArrayList<>();
    String[] row = new String[languageNumber + 1];
    String[] messageValueArray;
    // key:messageKey, value: String arrays containing message,values
    Map<String, String[]> map = new HashMap<>();
    for (int i = 0; i < messageQueryResultList.size(); i++) {
      // each record
      messageRecord = messageQueryResultList.get(i);
      languageCode = (String) messageRecord[0];
      messageKey = (String) messageRecord[1];
      messageValue = (String) messageRecord[2];

      //      System.out.println(languageCode);
      //      System.out.println(messageKey);
      //      System.out.println(messageValue);

      if (!map.containsKey(messageKey)) {
        messageValueArray = new String[languageNumber];
        int position = findPosition(languageCode, headers) - 1;
        System.out.println(position);
        messageValueArray[position] = messageValue;
        System.out.println(messageValueArray);
        map.put(messageKey, messageValueArray);

        System.out.println("");
      } else {
        messageValueArray = map.get(messageKey);
        int position = findPosition(languageCode, headers) - 1;
        System.out.println(position);
        messageValueArray[position] = messageValue;
        System.out.println(messageValueArray);
        map.put(messageKey, messageValueArray);
        System.out.println(map);
        System.out.println("");
      }
    }

    for (Map.Entry entry : map.entrySet()) {
      String key = (String) entry.getKey();
      Object[] value = (Object[]) entry.getValue();
      row = new String[languageNumber + 1];
      row[0] = key;
      for (int j = 0; j < value.length; j++) {
        row[j + 1] = (String) value[j];
      }
      translationList.add(row);
    }

    String fileName = "Exported Translation" + " - " + java.time.LocalDateTime.now();
    File file = MetaFiles.createTempFile(fileName, ".csv").toFile();
    CsvHelper.csvWriter(file.getParent(), file.getName(), ';', headers, translationList);
    try (InputStream is = new FileInputStream(file)) {
      Beans.get(MetaFiles.class).attach(is, file.getName(), importExportTranslation);
    }
    importExportTranslationRepository.save(importExportTranslation);

    return file.getPath();
  }

  protected int findPosition(String languageCode, String[] headers) {
    for (int i = 1; i < headers.length; i++) {
      if (languageCode.equals(headers[i])) {
        return i;
      }
    }
    return 0;
  }
}
