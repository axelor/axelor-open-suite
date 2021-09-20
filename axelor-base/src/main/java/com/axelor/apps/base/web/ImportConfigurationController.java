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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.ImportConfigurationRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.imports.ImportService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.io.File;
import java.nio.charset.StandardCharsets;
import javax.enterprise.context.ApplicationScoped;
import org.apache.commons.io.FileUtils;

@ApplicationScoped
public class ImportConfigurationController {

  public void run(ActionRequest request, ActionResponse response) {

    ImportConfiguration importConfiguration =
        request.getContext().asType(ImportConfiguration.class);
    try {

      ImportHistory importHistory = Beans.get(ImportService.class).run(importConfiguration);

      response.setValue("statusSelect", ImportConfigurationRepository.STATUS_COMPLETED);
      response.setValue(
          "endDateTime", Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime());

      response.setAttr("importHistoryList", "value:add", importHistory);
      File readFile = MetaFiles.getPath(importHistory.getLogMetaFile()).toFile();
      response.setNotify(
          FileUtils.readFileToString(readFile, StandardCharsets.UTF_8)
              .replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));

    } catch (Exception e) {
      response.setValue("statusSelect", ImportConfigurationRepository.STATUS_ERROR);
      TraceBackService.trace(response, e);
    }
  }
}
