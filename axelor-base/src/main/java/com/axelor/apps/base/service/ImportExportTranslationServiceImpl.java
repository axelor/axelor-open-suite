package com.axelor.apps.base.service;

import static com.axelor.apps.base.service.administration.AbstractBatch.FETCH_LIMIT;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportExportTranslation;
import com.axelor.apps.base.db.ImportExportTranslationHistory;
import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.repo.ImportExportTranslationHistoryRepository;
import com.axelor.apps.base.db.repo.ImportExportTranslationRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportExportTranslationServiceImpl implements ImportExportTranslationService {

  public static final char separator = ';';
  protected MetaTranslationRepository metaTranslationRepository;
  protected ImportExportTranslationRepository importExportTranslationRepository;
  protected ImportExportTranslationHistoryRepository importExportTranslationHistoryRepository;

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
   * @throws AxelorException
   */
  @Override
  public String exportTranslations(ImportExportTranslation importExportTranslation)
      throws IOException, AxelorException {
    errorNumber = 0;
    countRecords = 0;
    ImportExportTranslationHistory importExportTranslationHistory =
        saveHistory(importExportTranslation, "Export");
    List<String> languageCodes = getLanguageCodes(importExportTranslation);
    int languageNumber = languageCodes.size();
    // headers: key, code1, code2, ...
    String[] headers = computeCsvFileHeaders(languageCodes);
    Map<String, String[]> map =
        saveRecordsIntoMap(importExportTranslation, languageNumber, headers);
    List<String[]> translationList =
        addRecordRowIntoList(map, languageNumber, importExportTranslationHistory);
    String fileName = "Exported Translations" + " - " + java.time.LocalDateTime.now();
    File file = MetaFiles.createTempFile(fileName, ".csv").toFile();
    CsvHelper.csvWriter(
        file.getParent(), file.getName(), separator, '\"', headers, translationList);
    try (InputStream is = new FileInputStream(file)) {
      Beans.get(MetaFiles.class).attach(is, file.getName(), importExportTranslation);
    } catch (IOException e) {
      throw new AxelorException(
          importExportTranslationHistory,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          "File input error.");
    }
    saveHistory(importExportTranslationHistory);
    return file.getPath();
  }

  /**
   * This method saves an ImportExportTranslationHistory object.
   *
   * @param importExportTranslation
   * @param actionType "Export" or "Import"
   * @return importExportTranslationHistory
   */
  @Transactional
  protected ImportExportTranslationHistory saveHistory(
      ImportExportTranslation importExportTranslation, String actionType) {
    ImportExportTranslationHistory importExportTranslationHistory =
        new ImportExportTranslationHistory();
    importExportTranslationHistory.setActionType(actionType);
    importExportTranslationHistory.setRecordNumber(countRecords);
    importExportTranslationHistory.setErrorNumber(errorNumber);
    importExportTranslation.addImportExportTranslationHistoryListItem(
        importExportTranslationHistory);
    importExportTranslationRepository.save(importExportTranslation);
    return importExportTranslationHistory;
  }

  @Transactional
  protected void saveHistory(ImportExportTranslationHistory importExportTranslationHistory) {
    importExportTranslationHistory.setRecordNumber(countRecords);
    importExportTranslationHistory.setErrorNumber(errorNumber);
    importExportTranslationHistoryRepository.save(importExportTranslationHistory);
  }

  /**
   * This method add each record from map into a list. The map entry contains the key, Language1
   * message_value, Language2 message_value, ....
   *
   * @param map
   * @param languageNumber
   * @return List containing each row of records.
   */
  protected List<String[]> addRecordRowIntoList(
      Map<String, String[]> map,
      int languageNumber,
      ImportExportTranslationHistory importExportTranslationHistory) {
    String[] row;
    List<String[]> translationList = new ArrayList<>();
    for (Map.Entry<String, String[]> entry : map.entrySet()) {
      String[] value;
      try {
        value = entry.getValue();
        row = new String[languageNumber + 1];
        row[0] = entry.getKey();
        if (row[0].contains("\\")) {
          row[0] = row[0].replace("\\", "\\\\");
        }
        for (int j = 0; j < value.length; j++) {
          row[j + 1] = value[j];
          countRecords++;
        }
        translationList.add(row);
      } catch (Exception e) {
        errorNumber++;
        AxelorException ae =
            new AxelorException(
                importExportTranslationHistory,
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                "Import line processing error. " + "Error message: " + e.getMessage());
        TraceBackService.trace(ae);
      }
    }
    translationList.sort(Comparator.comparing(array -> array[0]));
    return translationList;
  }

  /**
   * This method query translation records from database and save them in a map. All language codes
   * are saved in the value of the map: String[].
   *
   * @param importExportTranslation
   * @param languageNumber
   * @param headers
   * @return A Map containing all translations message key and corresponding message values.
   */
  protected Map<String, String[]> saveRecordsIntoMap(
      ImportExportTranslation importExportTranslation, int languageNumber, String[] headers) {
    int offset = 0;
    Map<String, String[]> translationMap = saveTranslationKeyIntoMap(languageNumber);
    Object[] messageRecord;
    String messageKey;
    String languageCode;
    String messageValue;
    List<Object[]> messageQueryResultList;
    String[] messageValueArray;
    do {
      messageQueryResultList = getResultListByLimitAndOffset(offset, importExportTranslation);
      if (messageQueryResultList.isEmpty()) {
        break;
      }
      for (Object[] objects : messageQueryResultList) {
        try {
          messageRecord = objects;
          languageCode = (String) messageRecord[0];
          messageKey = (String) messageRecord[1];
          messageValue = (String) messageRecord[2];
          messageValueArray = translationMap.get(messageKey);
          int position = findPosition(languageCode, headers);
          messageValueArray[position] = messageValue;
          translationMap.put(messageKey, messageValueArray);
        } catch (Exception e) {
          errorNumber++;
        }
      }
      offset += FETCH_LIMIT;
    } while (!messageQueryResultList.isEmpty());
    return translationMap;
  }

  /**
   * This method queries all the existing translation key in the database and save the in a map.
   *
   * @param languageNumber
   */
  protected Map<String, String[]> saveTranslationKeyIntoMap(int languageNumber) {
    TypedQuery<String> query =
        JPA.em().createQuery("select distinct mt.key " + "from MetaTranslation mt ", String.class);
    List<String> keyResultList = query.getResultList();
    int initialCapacity = keyResultList.size() * 2;
    Map<String, String[]> translationMap = new HashMap<>(initialCapacity);
    for (String key : keyResultList) {
      translationMap.put(key, new String[languageNumber]);
    }
    return translationMap;
  }

  /**
   * This method compute the csv file headers from all language codes.
   *
   * @param languageCodes all language codes, e.g. en, fr, etc.
   * @return Headers String array.
   */
  protected String[] computeCsvFileHeaders(List<String> languageCodes) {
    return Stream.concat(Stream.of("key"), languageCodes.stream()).toArray(String[]::new);
  }

  /**
   * This method creates a query in database of translation records with a limit and an offset.
   *
   * @param offset the offset of the sql statement
   * @param importExportTranslation from which importExportTranslation obj those languageHeaders are
   *     obtained
   * @return Queried result list with the limit and the offset.
   */
  protected List<Object[]> getResultListByLimitAndOffset(
      int offset, ImportExportTranslation importExportTranslation) {
    Query translationRecordsQuery =
        JPA.em()
            .createQuery(
                "select mt.language, mt.key, mt.message "
                    + "from MetaTranslation mt "
                    + "where mt.language = ANY ( "
                    + "Select ls.code "
                    + "from ImportExportTranslation iet "
                    + "left join iet.languageSet ls "
                    + "where iet.id = :curId"
                    + ") "
                    + "order by mt.id ")
            .setParameter("curId", importExportTranslation.getId());
    translationRecordsQuery.setFirstResult(offset);
    translationRecordsQuery.setMaxResults(FETCH_LIMIT);
    return (List<Object[]>) translationRecordsQuery.getResultList();
  }

  /**
   * This method find the location in headers[] of a specific languageCode and return the correct
   * position where the messageValue should be put in the messageValueArray. Note that this headers
   * array is for the csv file headers.
   *
   * @param languageCode
   * @param headers
   * @return The index of that languageCode in headers[]
   */
  protected int findPosition(String languageCode, String[] headers) {
    for (int i = 1; i < headers.length; i++) {
      if (languageCode.equals(headers[i])) {
        return i - 1;
      }
    }
    return 0;
  }

  /**
   * This method queries the languageCode corresponding to the id of the ImportExportTranslation
   * object.
   *
   * @param importExportTranslation The current ImportExportTranslation obj
   * @return A List&lt;String&gt; type list containing all the language codes corresponding to the
   *     importExportTranslation object.
   */
  protected List<String> getLanguageCodes(ImportExportTranslation importExportTranslation) {
    Set<Language> languageSet = importExportTranslation.getLanguageSet();
    List<String> languageHeaders = new ArrayList<>();
    for (Language language : languageSet) {
      languageHeaders.add(language.getCode());
    }
    return languageHeaders;
  }

  /**
   * This method imports the translation file and saves each record into ImportExportTranslation
   * obj.
   *
   * @param importExportTranslation The current ImportExportTranslation obj
   * @return File path
   */
  @Override
  public Path importTranslations(ImportExportTranslation importExportTranslation)
      throws AxelorException {
    countRecords = 0;
    errorNumber = 0;
    ImportExportTranslationHistory importExportTranslationHistory =
        saveHistory(importExportTranslation, "Import");
    Path filePath = MetaFiles.getPath(importExportTranslation.getUploadFile());
    List<String[]> data;
    try {
      data = CsvHelper.cSVFileReader(filePath.toString(), separator);
    } catch (Exception e) {
      Logger logger = LoggerFactory.getLogger(getClass());
      logger.error("Read CSV file failed.", e);
      throw new AxelorException(
          importExportTranslationHistory,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          "Read file error.");
    }
    if (data == null) {
      return null;
    }
    List<String> headers = Arrays.asList(data.get(0));
    for (int i = 1; i < data.size(); i++) {
      if (i % FETCH_LIMIT == 0) {
        JPA.clear();
      }
      List<String> dataLine = Arrays.asList(data.get(i));
      try {
        countRecords +=
            insertOrUpdateTranslation(dataLine, headers, importExportTranslationHistory);
      } catch (AxelorException e) {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.error("Error processing data line: " + dataLine, e);
        errorNumber++;
        TraceBackService.trace(e);
      }
    }
    importExportTranslationHistory =
        importExportTranslationHistoryRepository.find(importExportTranslationHistory.getId());
    saveHistory(importExportTranslationHistory);
    return filePath;
  }

  /**
   * This method insert/update each dataLine.
   *
   * @param dataLine
   * @param headers
   * @param importExportTranslationHistory
   * @return total records imported or updated for the dataLine.
   */
  @Transactional
  protected int insertOrUpdateTranslation(
      List<String> dataLine,
      List<String> headers,
      ImportExportTranslationHistory importExportTranslationHistory)
      throws AxelorException {
    int importNumber = 0;
    String key = dataLine.get(0);
    if (dataLine.size() != headers.size()) {
      throw new AxelorException(
          importExportTranslationHistory,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          "Data line process error: " + dataLine);
    }
    for (int i = 1; i < dataLine.size(); i++) {
      // dataLine: messageKey, language1_value, language2_value, language3_value, ...
      String messageValue = dataLine.get(i);
      String languageCode = headers.get(i);
      TypedQuery<String> keyExistenceQuery =
          JPA.em()
              .createQuery(
                  "select mt.key " + "from MetaTranslation mt " + "where mt.key = :key",
                  String.class)
              .setParameter("key", key);
      List<String> keyExistenceQueryResultList = keyExistenceQuery.getResultList();

      if (keyExistenceQueryResultList.isEmpty()) {
        errorNumber++;
        AxelorException ae =
            new AxelorException(
                importExportTranslationHistory,
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                "The key doesn't exist. " + key);
        TraceBackService.trace(ae);
      } else {
        // key exists, but the message, key may not exist
        Query languageTranslationExistenceQuery =
            JPA.em()
                .createQuery(
                    "select mt.message, mt.language "
                        + "from MetaTranslation mt "
                        + "where mt.language = :languageCode "
                        + "and mt.key = :key ")
                .setParameter("key", key)
                .setParameter("languageCode", languageCode);
        List<Object[]> languageTranslationExistenceQueryResultList =
            languageTranslationExistenceQuery.getResultList();
        if (!languageTranslationExistenceQueryResultList.isEmpty()) {
          Query updateQuery =
              JPA.em()
                  .createQuery(
                      "update MetaTranslation mt "
                          + "set mt.message = :messageValue "
                          + "where mt.key = :key "
                          + "and mt.language = :languageCode ")
                  .setParameter("messageValue", messageValue)
                  .setParameter("key", key)
                  .setParameter("languageCode", languageCode);
          updateQuery.executeUpdate();
        } else {
          // this translation is newly added.
          saveNewTranslation(key, messageValue, languageCode);
        }
        importNumber++;
      }
    }
    return importNumber;
  }

  protected void saveNewTranslation(String key, String messageValue, String language) {
    MetaTranslation metaTranslation = new MetaTranslation();
    metaTranslation.setKey(key);
    metaTranslation.setMessage(messageValue);
    metaTranslation.setLanguage(language);
    metaTranslationRepository.save(metaTranslation);
  }
}
