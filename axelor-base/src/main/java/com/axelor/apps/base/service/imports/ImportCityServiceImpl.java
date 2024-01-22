/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.imports;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.imports.importer.FactoryImporter;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.studio.db.AppBase;
import com.axelor.utils.net.URLService;
import com.google.common.io.Files;
import com.google.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportCityServiceImpl implements ImportCityService {

  private FactoryImporter factoryImporter;

  private MetaFiles metaFiles;

  protected AppBaseService appBaseService;

  protected static final String CITYTEXTFILE = "cityTextFile.txt";

  protected static final String FEATURE_CLASS_FOR_CANTON_REGION_DEPARTMENT = "A";

  protected static final String FEATURE_CLASS_FOR_CITY = "P";

  protected static final String FEATURE_CLASS_CODE_FOR_CANTON = "ADM3";

  protected static final String FEATURE_CLASS_CODE_FOR_REGION = "ADM1";

  protected static final String FEATURE_CLASS_CODE_FOR_DEPARTMENT = "ADM2";

  protected static final String FEATURE_CLASS_CODE_FOR_CITY = "ADM4";

  protected static final String CITY_NO_LONGER_EXIST_CODE = "PPLH";

  protected static final String SEPARATOR = "\t";

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public enum GEONAMES_FILE {
    ZIP,
    DUMP;
  }

  protected PrintWriter printWriter = null;

  protected File errorFile = null;

  protected MetaFileRepository metaFileRepo;

  @Inject
  public ImportCityServiceImpl(
      FactoryImporter factoryImporter,
      MetaFiles metaFiles,
      AppBaseService appBaseService,
      MetaFileRepository metaFileRepo) {
    this.factoryImporter = factoryImporter;
    this.metaFiles = metaFiles;
    this.appBaseService = appBaseService;
    this.metaFileRepo = metaFileRepo;
  }

  /**
   * {@inheritDoc}}
   *
   * @throws AxelorException
   * @throws IOException
   */
  @Override
  public ImportHistory importCity(String typeSelect, MetaFile dataFile)
      throws AxelorException, IOException {

    ImportHistory importHistory = null;
    File configXmlFile = this.getConfigXmlFile(typeSelect);
    File dataCsvFile = this.getDataCsvFile(dataFile);

    importHistory = importCityData(configXmlFile, dataCsvFile);
    this.deleteTempFiles(configXmlFile, dataCsvFile);

    return importHistory;
  }

  /**
   * Creates binding or configuration file according to typeSelect
   *
   * @param typeSelect
   * @return
   */
  protected File getConfigXmlFile(String typeSelect) {

    File configFile = null;
    try {
      configFile = File.createTempFile("input-config", ".xml");

      InputStream bindFileInputStream =
          this.getClass().getResourceAsStream("/import-configs/" + typeSelect + "-config.xml");

      if (bindFileInputStream == null) {
        printWriter.append(I18n.get(BaseExceptionMessage.IMPORTER_3) + "\n");
      }

      try (FileOutputStream outputStream = new FileOutputStream(configFile)) {
        IOUtils.copy(bindFileInputStream, outputStream);
      }

    } catch (Exception e) {
      TraceBackService.trace(e);
      printWriter.append(e.getLocalizedMessage() + " at " + e.getStackTrace()[0] + "\n");
    }
    return configFile;
  }

  /**
   * Creates geonames-city.csv data file from geonames.txt data file
   *
   * @param dataFile
   * @return
   */
  protected File getDataCsvFile(MetaFile dataFile) {

    File csvFile = null;
    try {
      File tempDir = java.nio.file.Files.createTempDirectory(null).toFile();
      csvFile = new File(tempDir, "geonames_city.csv");

      Files.copy(MetaFiles.getPath(dataFile).toFile(), csvFile);

    } catch (Exception e) {
      TraceBackService.trace(e);
      printWriter.append(e.getLocalizedMessage() + " at " + e.getStackTrace()[0] + "\n");
    }
    return csvFile;
  }

  /**
   * Imports city data
   *
   * @param configXmlFile
   * @param dataCsvFile
   * @return
   */
  protected ImportHistory importCityData(File configXmlFile, File dataCsvFile) {

    ImportHistory importHistory = null;
    try {
      ImportConfiguration importConfiguration = new ImportConfiguration();
      importConfiguration.setBindMetaFile(metaFiles.upload(configXmlFile));
      importConfiguration.setDataMetaFile(metaFiles.upload(dataCsvFile));

      importHistory = factoryImporter.createImporter(importConfiguration).run();

    } catch (Exception e) {
      TraceBackService.trace(e);
      printWriter.append(e.getLocalizedMessage() + " at " + e.getStackTrace()[0] + "\n");
    }
    return importHistory;
  }

  /**
   * Deletes temporary configuration & data file
   *
   * @param configXmlFile
   * @param dataCsvFile
   */
  protected void deleteTempFiles(File configXmlFile, File dataCsvFile) {

    try {
      if (configXmlFile.isDirectory() && dataCsvFile.isDirectory()) {
        FileUtils.deleteDirectory(configXmlFile);
        FileUtils.deleteDirectory(dataCsvFile);
      } else {
        configXmlFile.delete();
        dataCsvFile.delete();
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      printWriter.append(e.getLocalizedMessage() + " at " + e.getStackTrace()[0] + "\n");
    }
  }

  /**
   * Extracts file from the zip
   *
   * @param downloadFileName : zip fileName to download from internet
   * @return
   * @return
   * @throws AxelorException if hostname is not valid or if file does not exist
   */
  @Override
  public MetaFile downloadZip(String downloadFileName, GEONAMES_FILE geonamesFile)
      throws AxelorException {
    String downloadUrl = getDownloadUrl(geonamesFile);
    MetaFile metaFile = null;

    if (StringUtils.isEmpty(downloadUrl)) {
      return metaFile;
    }

    try {
      File tempDir = java.nio.file.Files.createTempDirectory(null).toFile();
      File downloadFile = new File(tempDir, downloadFileName);

      File cityTextFile = new File(tempDir, CITYTEXTFILE);

      URLService.fileUrl(downloadFile, downloadUrl + downloadFileName, null, null);

      LOG.debug("path for downloaded zip file : {}", downloadFile.getPath());

      try (ZipFile zipFile = new ZipFile(downloadFile.getPath())) {

        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
          ZipEntry entry = entries.nextElement();

          if (entry.getName().equals(downloadFileName.replace("zip", "txt"))) {
            BufferedReader stream =
                new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));

            cityTextFile.createNewFile();

            switch (geonamesFile) {
              case DUMP:
                cityTextFile = this.extractDataDumpImport(stream, cityTextFile);
                break;

              case ZIP:
                cityTextFile = this.extractDataZipImport(stream, cityTextFile);
                break;

              default:
                printWriter.append(
                    I18n.get(BaseExceptionMessage.INVALID_GEONAMES_IMPORT_FILE) + "\n");
            }

            LOG.debug("Length of file : {}", cityTextFile.length());
            break;
          }
        }
      }
      metaFile = metaFiles.upload(cityTextFile);
      FileUtils.forceDelete(tempDir);

    } catch (UnknownHostException hostExp) {
      printWriter.append(I18n.get(BaseExceptionMessage.SERVER_CONNECTION_ERROR) + "\n");
    } catch (IOException e) {
      printWriter.append(
          String.format(I18n.get(BaseExceptionMessage.NO_DATA_FILE_FOUND), downloadUrl) + "\n");
    }
    return metaFile;
  }

  protected MetaFile extractCityZip(MetaFile dataFile) throws AxelorException, IOException {

    ZipEntry entry;
    MetaFile metaFile;
    File txtFile = File.createTempFile("city", ".txt");
    String requiredFileName = dataFile.getFileName().replace(".zip", ".txt");

    byte[] buffer = new byte[1024];
    int len;
    boolean txtFileFound = false;

    try (ZipInputStream zipFileInStream =
        new ZipInputStream(new FileInputStream(MetaFiles.getPath(dataFile).toFile())); ) {

      while ((entry = zipFileInStream.getNextEntry()) != null) {

        if (entry.getName().equals(requiredFileName)) {

          try (FileOutputStream fos = new FileOutputStream(txtFile); ) {

            while ((len = zipFileInStream.read(buffer)) > 0) {
              fos.write(buffer, 0, len);
            }
          }
          txtFileFound = true;
          break;
        }
      }

      if (!txtFileFound) {
        printWriter.append(
            String.format(
                    I18n.get(BaseExceptionMessage.NO_TEXT_FILE_FOUND),
                    requiredFileName,
                    dataFile.getFileName())
                + "\n");
      }
    }

    metaFile = metaFiles.upload(txtFile);

    FileUtils.forceDelete(txtFile);

    return metaFile;
  }

  protected File extractDataDumpImport(BufferedReader downloadedCityFileStream, File cityTextFile)
      throws IOException {

    HashMap<String, String> regionMap = new HashMap<>();
    HashMap<String, String> departmentMap = new HashMap<>();
    HashMap<String, String> cantonMap = new HashMap<>();
    List<String> cities = new ArrayList<>();

    String line;

    while ((line = downloadedCityFileStream.readLine()) != null) {
      String[] cityLine = line.split(SEPARATOR);

      if (cityLine[6].equals(FEATURE_CLASS_FOR_CANTON_REGION_DEPARTMENT)) {
        switch (cityLine[7]) {
          case FEATURE_CLASS_CODE_FOR_REGION:
            regionMap.put(cityLine[10], cityLine[1]);
            break;

          case FEATURE_CLASS_CODE_FOR_DEPARTMENT:
            departmentMap.put(cityLine[11], cityLine[1]);
            break;

          case FEATURE_CLASS_CODE_FOR_CANTON:
            cantonMap.put(cityLine[12], cityLine[1]);
            break;

          default:
        }
      }

      if ((cityLine[6].equals(FEATURE_CLASS_FOR_CANTON_REGION_DEPARTMENT)
              && cityLine[7].equals(FEATURE_CLASS_CODE_FOR_CITY))
          || (cityLine[6].equals(FEATURE_CLASS_FOR_CITY)
              && !cityLine[7].equals(CITY_NO_LONGER_EXIST_CODE))) {
        cities.add(
            String.format(
                "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                cityLine[8], // country code
                cityLine[13], // insee code
                cityLine[1], // city name
                cityLine[10], // region code
                cityLine[11], // department code
                cityLine[12], // canton code
                cityLine[4], // latitude
                cityLine[5], // longitude
                cityLine[14])); // population
      }
    }

    return this.createCityFileDumpImport(regionMap, departmentMap, cantonMap, cities, cityTextFile);
  }

  protected File extractDataZipImport(BufferedReader downloadedCityFileStream, File cityTextFile)
      throws IOException {

    List<String> cityList = new ArrayList<>();

    String line;

    while ((line = downloadedCityFileStream.readLine()) != null) {
      String[] cityLine = line.split(SEPARATOR);

      cityList.add(
          String.format(
              "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s%n",
              cityLine[0], // country code
              cityLine[1], // zip code
              cityLine[2], // city name
              cityLine[3], // region name
              cityLine[4], // region code
              cityLine[5], // department name
              cityLine[6], // department code
              cityLine[7], // canton name
              cityLine[8], // canton code
              cityLine[9], // latitude
              cityLine[10], // longitude
              cityLine[11])); // accuracy
    }

    return this.createCityFileZipImport(cityList, cityTextFile);
  }

  protected File createCityFileZipImport(List<String> cityList, File cityTextFile)
      throws IOException {

    try (FileWriter writer = new FileWriter(cityTextFile)) {
      Set<String> checkDuplicateCitySet = new HashSet<>();

      for (String city : cityList) {
        String[] cityInfo = city.split(SEPARATOR);

        if (checkDuplicateCitySet.add(
            cityInfo[1].toLowerCase().concat(cityInfo[2].toLowerCase()))) {
          writer.write(city.replace("\"", ""));
        }
      }
    }

    return cityTextFile;
  }

  protected File createCityFileDumpImport(
      HashMap<String, String> regionMap,
      HashMap<String, String> departmentMap,
      HashMap<String, String> cantonMap,
      List<String> cityList,
      File cityTextFile)
      throws IOException {

    try (FileWriter writer = new FileWriter(cityTextFile)) {
      Set<String> checkDuplicateCitySet = new HashSet<>();

      for (String city : cityList) {
        String[] cityInfo = city.split(SEPARATOR);

        if (checkDuplicateCitySet.add(
            cityInfo[1].toLowerCase().concat(cityInfo[2].toLowerCase()))) {
          writer.write(
              String.format(
                      "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s%n",
                      cityInfo[0],
                      cityInfo[1],
                      cityInfo[2],
                      regionMap.get(cityInfo[3]),
                      cityInfo[3],
                      departmentMap.get(cityInfo[4]),
                      cityInfo[4],
                      cantonMap.get(cityInfo[5]),
                      cityInfo[5],
                      cityInfo[6],
                      cityInfo[7],
                      cityInfo[8])
                  .replace("\"", ""));
        }
      }
    }

    return cityTextFile;
  }

  protected String getDownloadUrl(GEONAMES_FILE geonamesFile) throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();
    String downloadUrl = null;

    switch (geonamesFile) {
      case DUMP:
        downloadUrl = appBase.getGeoNamesDumpUrl();
        if (StringUtils.isEmpty(downloadUrl)) {
          printWriter.append(I18n.get(BaseExceptionMessage.GEONAMES_DUMP_URL_NOT_SPECIFIED) + "\n");
        }
        break;

      case ZIP:
        downloadUrl = appBase.getGeoNamesZipUrl();
        if (StringUtils.isEmpty(downloadUrl)) {
          printWriter.append(I18n.get(BaseExceptionMessage.GEONAMES_ZIP_URL_NOT_SPECIFIED) + "\n");
        }
        break;

      default:
        printWriter.append(I18n.get(BaseExceptionMessage.INVALID_GEONAMES_IMPORT_FILE) + "\n");
    }

    return downloadUrl;
  }

  /**
   * Imports cities from a predefined Geonames configuration.
   *
   * @param downloadFileName
   * @param typeSelect
   * @return
   */
  public Map<String, Object> importFromGeonamesAutoConfig(
      String downloadFileName, String typeSelect) {
    List<ImportHistory> importHistoryList = new ArrayList<>();
    Map<String, Object> importCityMap = new HashMap<>();
    try {
      File tempDir = Files.createTempDir();
      errorFile = new File(tempDir.getAbsolutePath(), "Error-File.txt");
      printWriter = new PrintWriter(errorFile);

      MetaFile zipImportDataFile = this.downloadZip(downloadFileName, GEONAMES_FILE.ZIP);
      MetaFile dumpImportDataFile = this.downloadZip(downloadFileName, GEONAMES_FILE.DUMP);

      if (zipImportDataFile != null && dumpImportDataFile != null) {
        importHistoryList.add(this.importCity(typeSelect + "-zip", zipImportDataFile));
        importHistoryList.add(this.importCity(typeSelect + "-dump", dumpImportDataFile));
      }
    } catch (Exception e) {
      printWriter.append(e.getLocalizedMessage() + " at " + e.getStackTrace()[0] + "\n");
    }
    printWriter.close();
    importCityMap.put("importHistoryList", importHistoryList);
    importCityMap.put("errorFile", this.getMetaFile(errorFile));
    return importCityMap;
  }

  /**
   * Imports cities from a custom Geonames file. This is useful for the countries not present in the
   * predefined list.
   *
   * @param map
   * @param typeSelect
   * @return
   */
  public Map<String, Object> importFromGeonamesManualConfig(
      Map<String, Object> map, String typeSelect) {
    List<ImportHistory> importHistoryList = new ArrayList<>();
    Map<String, Object> importCityMap = new HashMap<>();
    try {
      File tempDir = Files.createTempDir();
      errorFile = new File(tempDir.getAbsolutePath(), "Error-File.txt");
      printWriter = new PrintWriter(errorFile);
      if (map != null) {
        MetaFile dataFile = metaFileRepo.find(Long.parseLong(map.get("id").toString()));

        String extension = Files.getFileExtension(dataFile.getFileName());
        if (extension != null
            && (extension.equals("txt") || extension.equals("csv") || extension.equals("zip"))) {
          if (extension.equals("zip")) {
            dataFile = this.extractCityZip(dataFile);
          }
          importHistoryList.add(this.importCity(typeSelect + "-zip", dataFile));
        } else {
          printWriter.append(I18n.get(BaseExceptionMessage.INVALID_DATA_FILE_EXTENSION) + "\n");
        }
      }
    } catch (Exception e) {
      printWriter.append(e.getLocalizedMessage() + " at " + e.getStackTrace()[0] + "\n");
    }
    printWriter.close();
    importCityMap.put("importHistoryList", importHistoryList);
    importCityMap.put("errorFile", this.getMetaFile(errorFile));
    return importCityMap;
  }

  protected MetaFile getMetaFile(File file) {
    MetaFile metafile = null;
    if (file != null && file.length() != 0) {
      try {
        metafile = metaFiles.upload(file);
      } catch (Exception e) {
        TraceBackService.trace(e);
      }
    }
    return metafile;
  }
}
