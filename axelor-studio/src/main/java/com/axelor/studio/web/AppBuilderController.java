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
package com.axelor.studio.web;

import com.axelor.apps.base.db.App;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.data.Listener;
import com.axelor.data.xml.XMLImporter;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppBuilder;
import com.axelor.studio.db.repo.AppBuilderRepository;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.impl.common.IOUtil;

@ApplicationScoped
public class AppBuilderController {

  @Transactional
  public void importBpm(ActionRequest request, ActionResponse response) {

    String config = "/data-import/import-bpm.xml";

    try {
      InputStream inputStream = this.getClass().getResourceAsStream(config);
      File configFile = MetaFiles.createTempFile("config", ".xml").toFile();
      FileOutputStream fout = new FileOutputStream(configFile);
      IOUtil.copyCompletely(inputStream, fout);

      @SuppressWarnings("rawtypes")
      Path path =
          MetaFiles.getPath((String) ((Map) request.getContext().get("dataFile")).get("filePath"));
      File tempDir = Files.createTempDir();
      File importFile = new File(tempDir, "bpm.xml");
      Files.copy(path.toFile(), importFile);

      XMLImporter importer =
          new XMLImporter(configFile.getAbsolutePath(), tempDir.getAbsolutePath());
      final StringBuilder log = new StringBuilder();
      Listener listner =
          new Listener() {

            @Override
            public void imported(Integer imported, Integer total) {
              //					log.append("Total records: " + total + ", Total imported: " + total);

            }

            @Override
            public void imported(Model arg0) {}

            @Override
            public void handle(Model arg0, Exception err) {
              log.append("Error in import: " + err.getStackTrace().toString());
            }
          };

      importer.addListener(listner);

      importer.run();

      FileUtils.forceDelete(configFile);

      FileUtils.forceDelete(tempDir);

      FileUtils.forceDelete(path.toFile());

      @SuppressWarnings("unchecked")
      Object metaFileId = ((Map<String, Object>) request.getContext().get("dataFile")).get("id");
      MetaFileRepository metaFileRepository = Beans.get(MetaFileRepository.class);
      if (metaFileId != null) {
        MetaFile metaFile = metaFileRepository.find(Long.parseLong(metaFileId.toString()));
        if (metaFile != null) {
          metaFileRepository.remove(metaFile);
        }
      }

      response.setValue("importLog", log.toString());

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void installApp(ActionRequest request, ActionResponse response) throws AxelorException {

    AppBuilder appBuilder = request.getContext().asType(AppBuilder.class);
    appBuilder = Beans.get(AppBuilderRepository.class).find(appBuilder.getId());

    App app = appBuilder.getGeneratedApp();
    Beans.get(AppService.class).installApp(app, null);

    response.setSignal("refresh-app", true);
  }

  public void uninstallApp(ActionRequest request, ActionResponse response) throws AxelorException {

    AppBuilder appBuilder = request.getContext().asType(AppBuilder.class);
    appBuilder = Beans.get(AppBuilderRepository.class).find(appBuilder.getId());

    App app = appBuilder.getGeneratedApp();
    Beans.get(AppService.class).unInstallApp(app);

    response.setSignal("refresh-app", true);
  }
}
