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
package com.axelor.studio.service.loader;

import com.axelor.common.FileUtils;
import com.axelor.common.ResourceUtils;
import com.axelor.data.Listener;
import com.axelor.data.xml.XMLConfig;
import com.axelor.data.xml.XMLImporter;
import com.axelor.db.Model;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.MetaFiles;
import com.axelor.studio.db.AppLoader;
import com.axelor.studio.db.repo.AppLoaderRepository;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;

public class AppLoaderImportServiceImpl implements AppLoaderImportService {

  @Inject protected AppLoaderRepository appLoaderRepository;

  @Inject protected MetaFiles metaFiles;

  @Inject protected AppLoaderExportService appLoaderExportService;

  @Override
  @Transactional
  public void importApps(AppLoader appLoader) {

    if (appLoader.getImportMetaFile() == null) {
      return;
    }

    try {

      File dataDir = Files.createTempDir();
      extractImportZip(appLoader, dataDir);

      File logFile = importApp(appLoader, dataDir);

      appLoader = appLoaderRepository.find(appLoader.getId());
      if (appLoader.getImportLog() == null) {
        appLoader.setImportLog(metaFiles.upload(logFile));
      }
      appLoader.setImportedOn(LocalDateTime.now());
      appLoaderRepository.save(appLoader);

      FileUtils.deleteDirectory(dataDir);

    } catch (IOException e) {
      TraceBackService.trace(e);
    }
  }

  protected void extractImportZip(AppLoader appLoader, File dataDir)
      throws FileNotFoundException, IOException {

    FileInputStream fin =
        new FileInputStream(MetaFiles.getPath(appLoader.getImportMetaFile()).toFile());
    ZipInputStream zipInputStream = new ZipInputStream(fin);
    ZipEntry zipEntry = zipInputStream.getNextEntry();

    while (zipEntry != null) {
      FileOutputStream fout = new FileOutputStream(new File(dataDir, zipEntry.getName()));
      IOUtils.copy(zipInputStream, fout);
      fout.close();
      zipEntry = zipInputStream.getNextEntry();
    }

    zipInputStream.close();
  }

  protected File importApp(AppLoader appLoader, File dataDir)
      throws IOException, FileNotFoundException {

    File confiFile = getAppImportConfigFile(dataDir);

    XMLImporter xmlImporter =
        new XMLImporter(confiFile.getAbsolutePath(), dataDir.getAbsolutePath());
    xmlImporter.setContext(getImportContext(appLoader));

    File logFile =
        appLoader.getImportLog() != null
            ? MetaFiles.getPath(appLoader.getImportLog()).toFile()
            : MetaFiles.createTempFile("import-", "log").toFile();

    PrintWriter pw = new PrintWriter(logFile);

    xmlImporter.addListener(
        new Listener() {

          @Override
          public void imported(Integer total, Integer success) {}

          @Override
          public void imported(Model bean) {}

          @Override
          public void handle(Model bean, Exception e) {
            pw.write("Error Importing: " + bean);
            e.printStackTrace(pw);
          }
        });

    xmlImporter.run();

    pw.flush();
    pw.close();
    return logFile;
  }

  protected File getAppImportConfigFile(File dataDir) throws FileNotFoundException, IOException {

    File configFile = new File(dataDir, "app-config.xml");
    FileOutputStream fout = new FileOutputStream(configFile);
    InputStream inStream = geAppImportConfig();
    IOUtils.copy(inStream, fout);
    inStream.close();
    fout.close();

    File dataConfigFile = new File(dataDir, "data-config.xml");
    if (dataConfigFile.exists()) {
      XMLConfig xmlConfig = XMLConfig.parse(configFile);
      XMLConfig dataConfig = XMLConfig.parse(dataConfigFile);
      xmlConfig.getInputs().addAll(dataConfig.getInputs());
      appLoaderExportService.writeXmlConfig(configFile, xmlConfig);
    }

    return configFile;
  }

  protected InputStream geAppImportConfig() {

    return ResourceUtils.getResourceStream("data-import/import-bpm.xml");
  }

  protected Map<String, Object> getImportContext(AppLoader appLoader) {
    return ImmutableMap.of("appLoaderId", appLoader.getId());
  }
}
