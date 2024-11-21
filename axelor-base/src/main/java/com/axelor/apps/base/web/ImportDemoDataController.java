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
package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.imports.ImportDemoDataService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.io.Files;
import com.google.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

@Singleton
public class ImportDemoDataController {

  public void importDemoDataExcel(ActionRequest request, ActionResponse response)
      throws FileNotFoundException, IOException, AxelorException, ParseException,
          ClassNotFoundException {
    MetaFile metaFile =
        Beans.get(MetaFileRepository.class)
            .find(
                Long.valueOf(((Map) request.getContext().get("importFile")).get("id").toString()));
    File excelFile = MetaFiles.getPath(metaFile).toFile();

    if (Files.getFileExtension(excelFile.getName()).equals("xlsx")) {
      File tmpFile = File.createTempFile("Import", ".log");

      if (Beans.get(ImportDemoDataService.class).importDemoDataExcel(excelFile, tmpFile)) {
        response.setInfo(I18n.get(BaseExceptionMessage.IMPORT_COMPLETED_MESSAGE));
      } else {
        response.setInfo(I18n.get(BaseExceptionMessage.INVALID_DATA_FORMAT_ERROR));
      }

      response.setAttr("$logFile", "hidden", false);
      FileInputStream inStream = new FileInputStream(tmpFile);
      response.setValue("$logFile", Beans.get(MetaFiles.class).upload(inStream, "Import.log"));

    } else {
      response.setError(I18n.get(BaseExceptionMessage.VALIDATE_FILE_TYPE));
    }
  }
}
