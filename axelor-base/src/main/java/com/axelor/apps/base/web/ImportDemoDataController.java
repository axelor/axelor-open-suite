/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.imports.ImportDemoDataService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

@Singleton
public class ImportDemoDataController {

  @Inject ImportDemoDataService importDemoDataSercvice;

  @Inject MetaFileRepository metaFileRepo;

  @Inject MetaFiles metaFiles;

  public void importDemoDataExcel(ActionRequest request, ActionResponse response)
      throws FileNotFoundException, IOException, AxelorException, ParseException,
          ClassNotFoundException {

    MetaFile metaFile =
        metaFileRepo.find(
            Long.valueOf(((Map) request.getContext().get("importFile")).get("id").toString()));
    File excelFile = MetaFiles.getPath(metaFile).toFile();

    if (Files.getFileExtension(excelFile.getName()).equals("xlsx")) {
      MetaFile logMetaFile = importDemoDataSercvice.importDemoDataExcel(excelFile);
      response.setFlash("Import completed successfully.Please check the log for more details");
      response.setAttr("$logFile", "hidden", false);
      response.setValue("$logFile", logMetaFile);
    } else {
      response.setError(I18n.get(IExceptionMessage.VALIDATE_FILE_TYPE));
    }
  }
}
