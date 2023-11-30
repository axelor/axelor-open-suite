package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ImportExportTranslation;
import com.axelor.apps.base.db.ImportExportTranslationHistory;
import com.axelor.apps.base.db.repo.ImportExportTranslationHistoryRepository;
import com.axelor.apps.base.db.repo.ImportExportTranslationRepository;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.axelor.utils.helpers.file.CsvHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportExportTranslationServiceImpl implements ImportExportTranslationService {

  protected MetaTranslationRepository metaTranslationRepository;
  protected ImportExportTranslationRepository importExportTranslationRepository;
  protected ImportExportTranslationHistoryRepository importExportTranslationHistoryRepository;

  public static final int LIMIT = 50;

  protected int errorNumber = 0;
  protected int countRecords = 0;

  @Inject
  public ImportExportTranslationServiceImpl(
      MetaTranslationRepository metaTranslationRepository,
      ImportExportTranslationRepository importExportTranslationRepository,
      ImportExportTranslationHistoryRepository importExportTranslationHistoryRepository) {
    this.metaTranslationRepository = metaTranslationRepository;
    this.importExportTranslationRepository = importExportTranslationRepository;
    this.importExportTranslationHistoryRepository = importExportTranslationHistoryRepository;
  }

  /**
   * This method created an exported translation csv file for the input ImportExportTranslation
   * object.
   *
   * @param importExportTranslation
   * @throws IOException
   */
  @Transactional
  @Override
  public void exportTranslations(ImportExportTranslation importExportTranslation)
      throws IOException {
    errorNumber = 0;
    Long id = importExportTranslation.getId();
    List<String> languageHeaders = getLanguageHeaders(id);
    int languageNumber = languageHeaders.size();
    String[] headers = computeCsvFileHeaders(languageHeaders);
    Map<String, String[]> map;
    map = saveRecordsIntoMap(importExportTranslation, languageNumber, headers);
    List<String[]> translationList = addRecordRowIntoList(map, languageNumber);
    String fileName = "Exported Translations" + " - " + java.time.LocalDateTime.now();
    File file = MetaFiles.createTempFile(fileName, ".csv").toFile();
    CsvHelper.csvWriter(file.getParent(), file.getName(), ';', '\"', headers, translationList);
    try (InputStream is = new FileInputStream(file)) {
      Beans.get(MetaFiles.class).attach(is, file.getName(), importExportTranslation);
    } catch (IOException e) {
      Logger logger = LoggerFactory.getLogger(getClass());
      logger.error("FileInputStream error", e);
    }
    importExportTranslation = importExportTranslationRepository.find(id);
    ImportExportTranslationHistory importExportTranslationHistory =
        new ImportExportTranslationHistory();
    importExportTranslationHistory.setActionType("Export");
    importExportTranslationHistory.setRecordNumber(countRecords);
    importExportTranslationHistory.setErrorNumber(errorNumber);
    importExportTranslation.addImportExportTranslationHistoryListItem(
        importExportTranslationHistory);
    importExportTranslationRepository.save(importExportTranslation);
  }

  /**
   * This method add each record from map into a list.
   * The map entry contains the key, Language1 message_value, Language2 message_value, ....
   * @param map
   * @param languageNumber
   * @return List containing each row of records.
   */
  protected List<String[]> addRecordRowIntoList(Map<String, String[]> map, int languageNumber) {
    String[] row;
    List<String[]> translationList = new ArrayList<>();
    countRecords = 0;
    for (Map.Entry entry : map.entrySet()) {
      try {
        String key = (String) entry.getKey();
        Object[] value = (Object[]) entry.getValue();
        row = new String[languageNumber + 1];
        row[0] = key;
        for (int j = 0; j < value.length; j++) {
          row[j + 1] = (String) value[j];
          countRecords++;
        }
        translationList.add(row);
      } catch (Exception e) {
        errorNumber++;
      }
    }
    return translationList;
  }

  /**
   * This method query translation records from database and save them in a map.
   * All language codes are saved in the value of the map: String[].
   * @param importExportTranslation
   * @param languageNumber
   * @param headers
   * @return A Map containing all translations message key and corresponding message values.
   */
  protected Map<String, String[]> saveRecordsIntoMap(
      ImportExportTranslation importExportTranslation, int languageNumber, String[] headers) {
    int offset = 0;
    Map<String, String[]> map = new HashMap<>();
    Object[] messageRecord;
    String messageKey;
    String languageCode;
    String messageValue;
    List<Object[]> messageQueryResultList;
    String[] messageValueArray;
    do {
      JPA.clear();
      messageQueryResultList = getResultListByLimitAndOffset(offset, importExportTranslation);
      if (messageQueryResultList.isEmpty()) {
        break;
      } else {
        for (int i = 0; i < messageQueryResultList.size(); i++) {
          try {
            messageRecord = messageQueryResultList.get(i);
            languageCode = (String) messageRecord[0];
            messageKey = (String) messageRecord[1];
            messageValue = (String) messageRecord[2];
            if (!map.containsKey(messageKey)) {
              messageValueArray = new String[languageNumber];
              int position = findPosition(languageCode, headers) - 1;
              messageValueArray[position] = messageValue;
              map.put(messageKey, messageValueArray);
            } else {
              messageValueArray = map.get(messageKey);
              int position = findPosition(languageCode, headers) - 1;
              messageValueArray[position] = messageValue;
              map.put(messageKey, messageValueArray);
            }
          } catch (Exception e) {
            errorNumber++;
          }
        }
        offset += LIMIT;
      }
    } while (true);
    return map;
  }

  /**
   * This method compute the csv file headers from all language codes.
   * @param languageHeaders
   * @return Headers String array.
   */
  protected String[] computeCsvFileHeaders(List<String> languageHeaders) {
    int languageNumber = languageHeaders.size();
    String[] headers = new String[languageNumber + 1];
    headers[0] = "key";
    for (int i = 1; i < headers.length; i++) {
      headers[i] = languageHeaders.get(i - 1);
    }
    return headers;
  }

  /**
   * This method creates a query in database of translation records with a limit and an offset.
   *
   * @param offset
   * @param importExportTranslation
   * @return Queried result list with the limit and the offset.
   */
  protected List<Object[]> getResultListByLimitAndOffset(
      int offset, ImportExportTranslation importExportTranslation) {
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
                    + "limit :limit "
                    + "offset :offset ")
            .setParameter("curId", importExportTranslation.getId())
            .setParameter("limit", LIMIT)
            .setParameter("offset", offset);
    return messageQuery.getResultList();
  }

  /**
   * This method find the location in headers[] of a specific languageCode. Note that this headers
   * array is for the csv file headers.
   *
   * @param languageCode
   * @param headers
   * @return The index of that languageCode in headers[]
   */
  protected int findPosition(String languageCode, String[] headers) {
    for (int i = 1; i < headers.length; i++) {
      if (languageCode.equals(headers[i])) {
        return i;
      }
    }
    return 0;
  }

  /**
   * This method queries the languageCode corresponding to the id of the importExportTranslation
   * object.
   *
   * @param importExportTranslationId
   * @return A List&lt;String&gt; type list containing all the language codes corresponding to the
   *     importExportTranslation object.
   */
  protected List<String> getLanguageHeaders(long importExportTranslationId) {
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
            .setParameter("curId", importExportTranslationId);
    return (List<String>) languageQuery.getResultList();
  }

  /**
   * This method imports the translation file and saves each record into ImportExportTranslation
   * obj.
   *
   * @param importExportTranslation
   * @return
   */
  @Transactional
  @Override
  public Path importTranslations(ImportExportTranslation importExportTranslation) {
    errorNumber = 0;
    Path filePath = MetaFiles.getPath(importExportTranslation.getUploadFile());
    char separator = ';';
    List<String[]> data = null;
    try {
      data = CsvHelper.cSVFileReader(filePath.toString(), separator);
    } catch (Exception e) {
      Logger logger = LoggerFactory.getLogger(getClass());
      logger.error("Read CSV file failed.", e);
    }
    if (data == null) return null;
    List<String> headers = Arrays.asList(data.get(0));
    int dataLineNumber = 0;
    for (int i = 1; i < data.size(); i++) {
      List<String> dataLine = Arrays.asList(data.get(i));
      try {
        dataLineNumber += insertOrUpdateTranslation(dataLine, headers);
      } catch (Exception e) {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.error("Error processing data line: " + dataLine, e);
        errorNumber++;
      }
    }
    ImportExportTranslationHistory importExportTranslationHistory =
        new ImportExportTranslationHistory();
    importExportTranslation =
        importExportTranslationRepository.find(importExportTranslation.getId());
    importExportTranslationHistory.setActionType("Import");
    importExportTranslationHistory.setRecordNumber(dataLineNumber);
    importExportTranslationHistory.setErrorNumber(errorNumber);
    importExportTranslationHistory.setImportExportTranslation(importExportTranslation);
    importExportTranslation.addImportExportTranslationHistoryListItem(
        importExportTranslationHistory);
    importExportTranslationRepository.save(importExportTranslation);
    return filePath;
  }

  /**
   * This method insert/update each dataLine.
   *
   * @param dataLine
   * @param headers
   * @return total records imported or updated for the dataLine.
   */
  @Transactional
  protected int insertOrUpdateTranslation(List<String> dataLine, List<String> headers)
      throws Exception {
    int importNumber = 0;
    String key = dataLine.get(0);

    if (dataLine.size() != headers.size()) {
      throw new Exception("CSV file line has different number of columns against headers");
    }

    for (int i = 1; i < dataLine.size(); i++) {
      // dataLine: messageKey, en_value, fr_value, L3_value, ...
      String messageValue = dataLine.get(i);
      String languageCode = headers.get(i);
      Query existenceQuery =
          JPA.em()
              .createNativeQuery(
                  "select message_value "
                      + "from meta_translation "
                      + "where meta_translation.language = :languageCode "
                      + "and message_key = :key ")
              .setParameter("languageCode", languageCode)
              .setParameter("key", key);
      List existenceQueryResultList = existenceQuery.getResultList();
      if (existenceQueryResultList.isEmpty()) {
        // insert
        MetaTranslation metaTranslation = new MetaTranslation();
        metaTranslation.setKey(key);
        metaTranslation.setLanguage(languageCode);
        metaTranslation.setMessage(messageValue);
        metaTranslationRepository.save(metaTranslation);
      } else {
        // update
        Query updateQuery =
            JPA.em()
                .createNativeQuery(
                    "update meta_translation "
                        + "set message_value = :messageValue "
                        + "where message_key = :key "
                        + "and meta_translation.language = :languageCode ")
                .setParameter("messageValue", messageValue)
                .setParameter("key", key)
                .setParameter("languageCode", languageCode);
        updateQuery.executeUpdate();
      }
      importNumber++;
    }
    return importNumber;
  }
}
