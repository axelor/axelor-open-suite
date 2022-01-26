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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.FECImport;
import com.axelor.apps.account.db.repo.FECImportRepository;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.service.imports.importer.FactoryImporter;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class FECImportController {

  public void runImport(ActionRequest request, ActionResponse response) {
    try {
      FECImport fecImport = request.getContext().asType(FECImport.class);
      fecImport = Beans.get(FECImportRepository.class).find(fecImport.getId());

      ImportConfiguration importConfig = new ImportConfiguration();
      importConfig.setBindMetaFile(fecImport.getBindMetaFile());
      importConfig.setDataMetaFile(
          Beans.get(MetaFiles.class)
              .upload(
                  new FileInputStream(MetaFiles.getPath(fecImport.getDataMetaFile()).toFile()),
                  "FEC.csv"));

      ImportHistory importHistory =
          Beans.get(FactoryImporter.class).createImporter(importConfig).run();
      File readFile = MetaFiles.getPath(importHistory.getLogMetaFile()).toFile();
      response.setNotify(
          FileUtils.readFileToString(readFile, StandardCharsets.UTF_8)
              .replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setDefault(ActionRequest request, ActionResponse response) {
    try {
      FECImport fecImport = request.getContext().asType(FECImport.class);
      fecImport.setUser(AuthUtils.getUser());

      File configFile = File.createTempFile("input-config", ".xml");
      InputStream bindFileInputStream =
          this.getClass().getResourceAsStream("/FEC-config/import-FEC-config.xml");
      FileOutputStream outputStream = new FileOutputStream(configFile);
      IOUtils.copy(bindFileInputStream, outputStream);
      fecImport.setBindMetaFile(Beans.get(MetaFiles.class).upload(configFile));

      FileUtils.forceDelete(configFile);

      response.setValues(fecImport);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
