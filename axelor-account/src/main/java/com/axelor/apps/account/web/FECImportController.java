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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.FECImport;
import com.axelor.apps.account.db.repo.FECImportRepository;
import com.axelor.apps.account.service.fecimport.FECImportService;
import com.axelor.apps.account.service.fecimport.FECImporter;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;

public class FECImportController {

  public void runImport(ActionRequest request, ActionResponse response) {
    try {
      FECImport fecImport = request.getContext().asType(FECImport.class);
      fecImport = Beans.get(FECImportRepository.class).find(fecImport.getId());

      ImportConfiguration importConfig = new ImportConfiguration();
      importConfig.setBindMetaFile(fecImport.getImportFECType().getBindMetaFile());
      importConfig.setDataMetaFile(
          Beans.get(MetaFiles.class)
              .upload(
                  new FileInputStream(MetaFiles.getPath(fecImport.getDataMetaFile()).toFile()),
                  "FEC.csv"));

      FECImporter fecImporter = Beans.get(FECImporter.class).addFecImport(fecImport);
      ImportHistory importHistory = fecImporter.init(importConfig).run();

      File readFile = MetaFiles.getPath(importHistory.getLogMetaFile()).toFile();
      Beans.get(FECImportService.class).letterImportedReconcileGroup(fecImport);

      response.setNotify(
          FileUtils.readFileToString(readFile, StandardCharsets.UTF_8)
              .replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setDefault(ActionRequest request, ActionResponse response) {
    try {
      FECImport fecImport = request.getContext().asType(FECImport.class);
      fecImport.setUser(AuthUtils.getUser());

      response.setValues(fecImport);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setCompany(ActionRequest request, ActionResponse response) {
    try {
      FECImport fecImport = request.getContext().asType(FECImport.class);
      if (fecImport.getCompany() == null) {
        MetaFile dataMetaFile = fecImport.getDataMetaFile();
        response.setValue("company", Beans.get(FECImportService.class).getCompany(dataMetaFile));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
