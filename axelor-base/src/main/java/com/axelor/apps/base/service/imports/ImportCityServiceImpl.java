/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.repo.AppBaseRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.imports.importer.FactoryImporter;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
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
import java.net.URL;
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

  @Inject private FactoryImporter factoryImporter;

  @Inject private MetaFiles metaFiles;

  protected static final String CITYTEXTFILE = "cityTextFile.txt";

  protected static final String FEATURE_CLASS_FOR_CANTON_REGION_DEPARTMENT = "A";

  protected static final String FEATURE_CLASS_FOR_CITY = "P";

  protected static final String FEATURE_CLASS_CODE_FOR_CANTON = "ADM3";

  protected static final String FEATURE_CLASS_CODE_FOR_REGION = "ADM1";

  protected static final String FEATURE_CLASS_CODE_FOR_DEPARTMENT = "ADM2";

  protected static final String FEATURE_CLASS_CODE_FOR_CITY = "ADM4";

  protected static final String CITY_NO_LONGER_EXIST_CODE = "PPLH";

  protected static final String SEPERATOR = "\t";

  final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
      e.printStackTrace();
    }
    return importHistory;
  }

  /**
   * create bind or config file according to typeSelect
   *
   * @param typeSelect
   * @return
   */
  private File getConfigXmlFile(String typeSelect) {

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

      FileOutputStream outputStream = new FileOutputStream(configFile);

      IOUtils.copy(bindFileInputStream, outputStream);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return configFile;
  }

  /**
   * create geonames-city.csv data file from geonames .txt data file
   *
   * @param dataFile
   * @return
   */
  private File getDataCsvFile(MetaFile dataFile) {

    File csvFile = null;
    try {
      File tempDir = Files.createTempDir();
      csvFile = new File(tempDir, "geonames_city.csv");

      Files.copy(MetaFiles.getPath(dataFile).toFile(), csvFile);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return csvFile;
  }

  /**
   * Import city data
   *
   * @param configXmlFile
   * @param dataCsvFile
   * @return
   */
  private ImportHistory importCityData(File configXmlFile, File dataCsvFile) {

    ImportHistory importHistory = null;
    try {
      ImportConfiguration importConfiguration = new ImportConfiguration();
      importConfiguration.setBindMetaFile(metaFiles.upload(configXmlFile));
      importConfiguration.setDataMetaFile(metaFiles.upload(dataCsvFile));

      importHistory = factoryImporter.createImporter(importConfiguration).run();

    } catch (Exception e) {
      e.printStackTrace();
    }
    return importHistory;
  }

  /**
   * Delete temporary config & data file
   *
   * @param configXmlFile
   * @param dataCsvFile
   */
  private void deleteTempFiles(File configXmlFile, File dataCsvFile) {

    try {
      if (configXmlFile.isDirectory() && dataCsvFile.isDirectory()) {
        FileUtils.deleteDirectory(configXmlFile);
        FileUtils.deleteDirectory(dataCsvFile);
      } else {
        configXmlFile.delete();
        dataCsvFile.delete();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * extracting file from the zip
   *
   * @param downloadFileName : zip fileName to download from internet
   * @return
   * @return
   * @throws Exception
   */
  @Override
  public MetaFile downloadZip(String downloadFileName) throws Exception {

    File downloadFile = null;
    File cityTextFile = null;
    File tempDir = null;
    MetaFile metaFile = null;

    try {
      tempDir = Files.createTempDir();
      downloadFile = new File(tempDir, downloadFileName);
      AppBase appBase = Beans.get(AppBaseRepository.class).all().fetchOne();

      cityTextFile = new File(tempDir, CITYTEXTFILE);

      URL url = new URL(appBase.getGeoNamesUrl() + downloadFileName);

      FileUtils.copyURLToFile(url, downloadFile);

      LOG.debug("path for downloaded zip file : " + downloadFile.getPath());

      try (ZipFile zipFile = new ZipFile(downloadFile.getPath());
          FileWriter writer = new FileWriter(cityTextFile)) {

        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
          ZipEntry entry = entries.nextElement();

          if (entry.getName().equals(downloadFileName.replace("zip", "txt"))) {
            BufferedReader stream =
                new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));

            StringBuilder buffer = this.extractDataFromDownloadedFile(stream);

            cityTextFile.createNewFile();

            writer.flush();
            writer.write(buffer.toString().replace("\"", ""));

            LOG.debug("Length of file : " + cityTextFile.length());
            break;
          }
        }
      }
      metaFile = metaFiles.upload(cityTextFile);
      FileUtils.forceDelete(tempDir);

    } catch (UnknownHostException hostExp) {
      throw new Exception(I18n.get(IExceptionMessage.SERVER_CONNECTION_ERROR), hostExp);
    } catch (Exception e) {
      throw e;
    }

    return metaFile;
  }

  private MetaFile extractCityZip(MetaFile dataFile) throws Exception {

    ZipEntry entry = null;
    MetaFile metaFile = null;
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

  private StringBuilder extractDataFromDownloadedFile(BufferedReader downloadedCityFileStream)
      throws IOException {

    HashMap<String, String> regionMap = new HashMap<>();
    HashMap<String, String> departmentMap = new HashMap<>();
    HashMap<String, String> cantonMap = new HashMap<>();
    List<String> cityList = new ArrayList<>();

    String line;

    while ((line = downloadedCityFileStream.readLine()) != null) {
      String[] values = line.split(SEPERATOR);

      if (values[6].equals(FEATURE_CLASS_FOR_CANTON_REGION_DEPARTMENT)) {
        switch (values[7]) {
          case FEATURE_CLASS_CODE_FOR_REGION:
            regionMap.put(values[10], values[1]);
            break;

          case FEATURE_CLASS_CODE_FOR_DEPARTMENT:
            departmentMap.put(values[11], values[1]);
            break;

          case FEATURE_CLASS_CODE_FOR_CANTON:
            cantonMap.put(values[12], values[1]);
            break;

          default:
        }
      }

      if ((values[6].equals(FEATURE_CLASS_FOR_CANTON_REGION_DEPARTMENT)
              && values[7].equals(FEATURE_CLASS_CODE_FOR_CITY))
          || (values[6].equals(FEATURE_CLASS_FOR_CITY)
              && !values[7].equals(CITY_NO_LONGER_EXIST_CODE))) {
        cityList.add(
            String.format(
                "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                values[8], // country code
                values[13], // zip code
                values[1], // city name
                values[10], // region code
                values[11], // department code
                values[12], // canton code
                values[4], // lattitude
                values[5], // longitude
                values[14])); // population
      }
    }

    return this.createCityFile(regionMap, departmentMap, cantonMap, cityList);
  }

  private StringBuilder createCityFile(
      HashMap<String, String> regionMap,
      HashMap<String, String> departmentMap,
      HashMap<String, String> cantonMap,
      List<String> cityList) {

    StringBuilder buffer = new StringBuilder();

    Set<String> checkDuplicateCitySet = new HashSet<>();

    for (String value : cityList) {
      String[] values = value.split(SEPERATOR);

      if (checkDuplicateCitySet.add(values[1].toLowerCase().concat(values[2].toLowerCase()))) {
        buffer.append(
            String.format(
                "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s%n",
                values[0],
                values[1],
                values[2],
                regionMap.get(values[3]),
                values[3],
                departmentMap.get(values[4]),
                values[4],
                cantonMap.get(values[5]),
                values[5],
                values[6],
                values[7],
                values[8]));
      }
    }

    return buffer;
  }
}
