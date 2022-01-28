/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.imports;

import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.imports.importer.FactoryImporter;
import com.axelor.apps.tool.net.URLService;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
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
import java.lang.invoke.MethodHandles;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

  @Inject
  public ImportCityServiceImpl(
      FactoryImporter factoryImporter, MetaFiles metaFiles, AppBaseService appBaseService) {
    this.factoryImporter = factoryImporter;
    this.metaFiles = metaFiles;
    this.appBaseService = appBaseService;
  }

  /** {@inheritDoc}} */
  @Override
  public ImportHistory importCity(String typeSelect, MetaFile dataFile) {

    ImportHistory importHistory = null;
    try {
      if (dataFile.getFileType().equals("application/zip")) {
        dataFile = this.extractCityZip(dataFile);
      }
      File configXmlFile = this.getConfigXmlFile(typeSelect);
      File dataCsvFile = this.getDataCsvFile(dataFile);

      importHistory = importCityData(configXmlFile, dataCsvFile);
      this.deleteTempFiles(configXmlFile, dataCsvFile);

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
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
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.IMPORTER_3));
      }

      try (FileOutputStream outputStream = new FileOutputStream(configFile)) {
        IOUtils.copy(bindFileInputStream, outputStream);
      }

    } catch (Exception e) {
      TraceBackService.trace(e);
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

    try {
      File tempDir = java.nio.file.Files.createTempDirectory(null).toFile();
      File downloadFile = new File(tempDir, downloadFileName);

      File cityTextFile = new File(tempDir, CITYTEXTFILE);

      URLService.fileUrl(downloadFile, downloadUrl + downloadFileName, null, null);

      LOG.debug("path for downloaded zip file : {}", downloadFile.getPath());

      StringBuilder buffer;
      try (ZipFile zipFile = new ZipFile(downloadFile.getPath());
          FileWriter writer = new FileWriter(cityTextFile)) {

        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
          ZipEntry entry = entries.nextElement();

          if (entry.getName().equals(downloadFileName.replace("zip", "txt"))) {
            BufferedReader stream =
                new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));

            switch (geonamesFile) {
              case DUMP:
                buffer = this.extractDataDumpImport(stream);
                break;

              case ZIP:
                buffer = this.extractDataZipImport(stream);
                break;

              default:
                throw new AxelorException(
                    TraceBackRepository.CATEGORY_NO_VALUE,
                    I18n.get(IExceptionMessage.INVALID_GEONAMES_IMPORT_FILE));
            }

            cityTextFile.createNewFile();

            writer.flush();
            writer.write(buffer.toString().replace("\"", ""));

            LOG.debug("Length of file : {}", cityTextFile.length());
            break;
          }
        }
      }
      MetaFile metaFile = metaFiles.upload(cityTextFile);
      FileUtils.forceDelete(tempDir);

      return metaFile;

    } catch (UnknownHostException hostExp) {
      throw new AxelorException(
          hostExp,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.SERVER_CONNECTION_ERROR));

    } catch (IOException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.NO_DATA_FILE_FOUND),
          downloadUrl);
    }
  }

  protected MetaFile extractCityZip(MetaFile dataFile) throws IOException {

    ZipEntry entry;
    MetaFile metaFile;
    File txtFile = File.createTempFile("city", ".txt");
    String requiredFileName = dataFile.getFileName().replace(".zip", ".txt");

    byte[] buffer = new byte[1024];
    int len;

    try (ZipInputStream zipFileInStream =
        new ZipInputStream(new FileInputStream(MetaFiles.getPath(dataFile).toFile())); ) {

      while ((entry = zipFileInStream.getNextEntry()) != null) {

        if (entry.getName().equals(requiredFileName)) {

          try (FileOutputStream fos = new FileOutputStream(txtFile); ) {

            while ((len = zipFileInStream.read(buffer)) > 0) {
              fos.write(buffer, 0, len);
            }
          }
          break;
        }
      }
    }

    metaFile = metaFiles.upload(txtFile);

    FileUtils.forceDelete(txtFile);

    return metaFile;
  }

  protected StringBuilder extractDataDumpImport(BufferedReader downloadedCityFileStream)
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

    return this.createCityFileDumpImport(regionMap, departmentMap, cantonMap, cities);
  }

  protected StringBuilder extractDataZipImport(BufferedReader downloadedCityFileStream)
      throws IOException {

    List<String> cityList = new ArrayList<>();

    String line;

    while ((line = downloadedCityFileStream.readLine()) != null) {
      String[] cityLine = line.split(SEPARATOR);

      cityList.add(
          String.format(
              "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s%n",
              cityLine[0], // country code
              cityLine[1], // zip code
              cityLine[2], // city name
              cityLine[3], // region name
              cityLine[4], // region code
              cityLine[5], // department name
              cityLine[6], // department code
              cityLine[7], // canton name
              cityLine[8])); // canton code
    }

    return this.createCityFileZipImport(cityList);
  }

  protected StringBuilder createCityFileZipImport(List<String> cityList) {

    StringBuilder buffer = new StringBuilder();

    Set<String> checkDuplicateCitySet = new HashSet<>();

    for (String city : cityList) {
      String[] cityInfo = city.split(SEPARATOR);

      if (checkDuplicateCitySet.add(cityInfo[1].toLowerCase().concat(cityInfo[2].toLowerCase()))) {
        buffer.append(city);
      }
    }

    return buffer;
  }

  protected StringBuilder createCityFileDumpImport(
      HashMap<String, String> regionMap,
      HashMap<String, String> departmentMap,
      HashMap<String, String> cantonMap,
      List<String> cityList) {

    StringBuilder buffer = new StringBuilder();

    Set<String> checkDuplicateCitySet = new HashSet<>();

    for (String city : cityList) {
      String[] cityInfo = city.split(SEPARATOR);

      if (checkDuplicateCitySet.add(cityInfo[1].toLowerCase().concat(cityInfo[2].toLowerCase()))) {
        buffer.append(
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
                cityInfo[8]));
      }
    }

    return buffer;
  }

  protected String getDownloadUrl(GEONAMES_FILE geonamesFile) throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();
    String downloadUrl;

    switch (geonamesFile) {
      case DUMP:
        downloadUrl = appBase.getGeoNamesDumpUrl();
        break;
      case ZIP:
        downloadUrl = appBase.getGeoNamesZipUrl();
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(IExceptionMessage.INVALID_GEONAMES_IMPORT_FILE));
    }

    if (StringUtils.isEmpty(downloadUrl)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.GEONAMES_URL_NOT_SPECIFIED),
          downloadUrl);
    }

    return downloadUrl;
  }
}
