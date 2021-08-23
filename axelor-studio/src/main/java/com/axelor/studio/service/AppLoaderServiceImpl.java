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
package com.axelor.studio.service;

import com.axelor.common.FileUtils;
import com.axelor.common.ResourceUtils;
import com.axelor.data.Listener;
import com.axelor.data.xml.XMLImporter;
import com.axelor.db.Model;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.MetaFiles;
import com.axelor.studio.db.AppLoader;
import com.axelor.studio.db.repo.AppLoaderRepository;
import com.axelor.text.GroovyTemplates;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;

public class AppLoaderServiceImpl implements AppLoaderService {

  @Inject protected MetaFiles metaFiles;

  @Inject protected AppLoaderRepository appLoaderRepository;

  @Override
  @Transactional
  public void exportApps(AppLoader appLoader) {

    if (CollectionUtils.isEmpty(appLoader.getExportedAppBuilderSet())) {
      return;
    }

    InputStream inputStream = getTemplate();

    GroovyTemplates templates = new GroovyTemplates();
    try {
      File file = MetaFiles.createTempFile("app", ".xml").toFile();
      FileWriter writer = new FileWriter(file);
      Map<String, Object> ctx = getExportContext(appLoader);
      templates.from(new InputStreamReader(inputStream)).make(ctx).render(writer);
      writer.close();
      if (appLoader.getExportMetaFile() != null) {
        metaFiles.upload(file, appLoader.getExportMetaFile());
      } else {
        appLoader.setExportMetaFile(metaFiles.upload(file));
      }
      appLoader.setExportedOn(LocalDateTime.now());
      appLoaderRepository.save(appLoader);
    } catch (IOException e) {
      TraceBackService.trace(e);
    }
  }

  public Map<String, Object> getExportContext(AppLoader appLoader) {

    List<Long> ids =
        appLoader.getExportedAppBuilderSet().stream()
            .map(it -> it.getId())
            .collect(Collectors.toList());

    Map<String, Object> ctx = new HashMap<>();
    ctx.put("__ids__", ids);

    return ctx;
  }

  protected InputStream getTemplate() {
    return ResourceUtils.getResourceStream("data-export/export-bpm.tmpl");
  }

  @Override
  @Transactional
  public void importApps(AppLoader appLoader) {

    if (appLoader.getImportMetaFile() == null) {
      return;
    }

    final AppLoader finalLoader = appLoader;

    try {

      File configFile = getImportConfigFile();

      File dataDir = Files.createTempDir();
      File importFile = new File(dataDir, "bpm.xml");
      Files.copy(MetaFiles.getPath(appLoader.getImportMetaFile()).toFile(), importFile);

      XMLImporter xmlImporter =
          new XMLImporter(configFile.getAbsolutePath(), dataDir.getAbsolutePath());
      xmlImporter.setContext(getImportContext(finalLoader));

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

  protected Map<String, Object> getImportContext(AppLoader appLoader) {
    return ImmutableMap.of("appLoaderId", appLoader.getId());
  }

  protected File getImportConfigFile() throws IOException, FileNotFoundException {

    File configFile = MetaFiles.createTempFile("config-", ".xml").toFile();
    FileOutputStream fout = new FileOutputStream(configFile);
    InputStream inStream = ResourceUtils.getResourceStream("data-import/import-bpm.xml");
    IOUtils.copy(inStream, fout);
    inStream.close();
    fout.close();

    return configFile;
  }
}
