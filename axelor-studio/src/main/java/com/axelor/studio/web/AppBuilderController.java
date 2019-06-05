/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.data.Listener;
import com.axelor.data.xml.XMLImporter;
import com.axelor.db.Model;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.impl.common.IOUtil;

public class AppBuilderController {

  @Inject private MetaFileRepository metaFileRepo;

  @Transactional
  public void importBpm(ActionRequest request, ActionResponse response) {

    String config = "/data-import/import-bpm.xml";

    try {
      InputStream inputStream = this.getClass().getResourceAsStream(config);
      File configFile = File.createTempFile("config", ".xml");
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

      if (metaFileId != null) {
        MetaFile metaFile = metaFileRepo.find(Long.parseLong(metaFileId.toString()));
        if (metaFile != null) {
          metaFileRepo.remove(metaFile);
        }
      }

      response.setValue("importLog", log.toString());

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
