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
package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.utils.ConfiguratorCreatorImportUtilsService;
import com.axelor.data.Listener;
import com.axelor.data.xml.XMLImporter;
import com.axelor.db.Model;
import com.axelor.meta.MetaFiles;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.impl.common.IOUtil;

public class ConfiguratorCreatorImportServiceImpl implements ConfiguratorCreatorImportService {

  protected ConfiguratorCreatorService configuratorCreatorService;
  protected ConfiguratorCreatorImportUtilsService configuratorCreatorImportUtilsService;

  @Inject
  public ConfiguratorCreatorImportServiceImpl(
      ConfiguratorCreatorService configuratorCreatorService,
      ConfiguratorCreatorImportUtilsService configuratorCreatorImportUtilsService) {
    this.configuratorCreatorService = configuratorCreatorService;
    this.configuratorCreatorImportUtilsService = configuratorCreatorImportUtilsService;
  }

  private static final String CONFIG_FILE_PATH =
      "/data-import/import-configurator-creator-config.xml";

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public String importConfiguratorCreators(String filePath) throws IOException {
    return importConfiguratorCreators(filePath, CONFIG_FILE_PATH);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public String importConfiguratorCreators(String filePath, String configFilePath)
      throws IOException {
    Path path = MetaFiles.getPath(filePath);
    try (InputStream fileInPutStream = new FileInputStream(path.toFile())) {
      return importConfiguratorCreators(fileInPutStream, configFilePath);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public String importConfiguratorCreators(InputStream xmlInputStream) throws IOException {
    return importConfiguratorCreators(xmlInputStream, CONFIG_FILE_PATH);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public String importConfiguratorCreators(InputStream xmlInputStream, String configFilePath)
      throws IOException {
    InputStream inputStream = this.getClass().getResourceAsStream(configFilePath);
    File configFile = File.createTempFile("config", ".xml");
    FileOutputStream fout = new FileOutputStream(configFile);
    IOUtil.copyCompletely(inputStream, fout);

    File tempDir = Files.createTempDir();
    File importFile = new File(tempDir, "configurator-creator.xml");
    FileUtils.copyInputStreamToFile(xmlInputStream, importFile);

    XMLImporter importer = new XMLImporter(configFile.getAbsolutePath(), tempDir.getAbsolutePath());
    final StringBuilder importLog = new StringBuilder();
    Listener listener =
        new Listener() {

          @Override
          public void imported(Integer imported, Integer total) {
            importLog.append("Total records: " + total + ", Total imported: " + total);
          }

          @Override
          public void imported(Model arg0) {
            try {
              completeAfterImport(arg0);
            } catch (AxelorException e) {
              importLog.append("Error in import: " + Arrays.toString(e.getStackTrace()));
            }
          }

          @Override
          public void handle(Model arg0, Exception err) {
            importLog.append("Error in import: " + Arrays.toString(err.getStackTrace()));
          }
        };

    importer.addListener(listener);

    importer.run();

    FileUtils.forceDelete(configFile);

    FileUtils.forceDelete(tempDir);

    return importLog.toString();
  }

  protected void completeAfterImport(Object arg0) throws AxelorException {
    if (arg0.getClass().equals(ConfiguratorCreator.class)) {
      completeAfterImport((ConfiguratorCreator) arg0);
    }
  }

  protected void completeAfterImport(ConfiguratorCreator creator) throws AxelorException {
    configuratorCreatorImportUtilsService.fixAttributesName(creator);
    configuratorCreatorService.updateAttributes(creator);
    configuratorCreatorService.removeTemporalAttributesAndIndicators(creator);
    configuratorCreatorService.updateIndicators(creator);
  }
}
