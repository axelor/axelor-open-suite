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
package com.axelor.apps.production.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.production.db.BillOfMaterialImport;
import com.axelor.apps.production.db.repo.BillOfMaterialImportRepository;
import com.axelor.apps.production.service.bomimport.BillOfMaterialImportService;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;

@Singleton
public class BillOfMaterialImportController {

  public void runImport(ActionRequest request, ActionResponse response)
      throws IOException, AxelorException {

    BillOfMaterialImport billOfMaterialImport =
        request.getContext().asType(BillOfMaterialImport.class);

    BillOfMaterialImportService billOfMaterialImportService =
        Beans.get(BillOfMaterialImportService.class);

    ImportHistory importHistory = billOfMaterialImportService.processImport(billOfMaterialImport);

    File readFile = MetaFiles.getPath(importHistory.getLogMetaFile()).toFile();
    response.setNotify(
        FileUtils.readFileToString(readFile, StandardCharsets.UTF_8)
            .replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));

    billOfMaterialImportService.setStatusToImported(billOfMaterialImport);
    response.setReload(true);
  }

  public void createBoMFromImport(ActionRequest request, ActionResponse response)
      throws AxelorException {

    BillOfMaterialImportService billOfMaterialImportService =
        Beans.get(BillOfMaterialImportService.class);

    BillOfMaterialImport billOfMaterialImport =
        Beans.get(BillOfMaterialImportRepository.class)
            .find(request.getContext().asType(BillOfMaterialImport.class).getId());

    billOfMaterialImportService.createBoMFromImport(billOfMaterialImport);

    billOfMaterialImportService.setStatusToValidated(billOfMaterialImport);
    response.setReload(true);
  }

  public void getCreatedProducts(ActionRequest request, ActionResponse response) {
    BillOfMaterialImport billOfMaterialImport =
        request.getContext().asType(BillOfMaterialImport.class);

    response.setValue(
        "$createdProducts",
        Beans.get(BillOfMaterialImportService.class).getCreatedProducts(billOfMaterialImport));
  }
}
