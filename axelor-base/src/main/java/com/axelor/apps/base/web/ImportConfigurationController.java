/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.service.imports.ImportService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.MetaFiles;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;

@Singleton
public class ImportConfigurationController {

  @Inject private ImportService importService;

  public void run(ActionRequest request, ActionResponse response) {

    ImportConfiguration importConfiguration =
        request.getContext().asType(ImportConfiguration.class);

    try {

      ImportHistory importHistory = importService.run(importConfiguration);
      response.setAttr("importHistoryList", "value:add", importHistory);
      File readFile = MetaFiles.getPath(importHistory.getLogMetaFile()).toFile();
      response.setNotify(
          FileUtils.readFileToString(readFile, StandardCharsets.UTF_8)
              .replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
