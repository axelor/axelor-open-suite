/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.imports.ImportConfigurationCallableService;
import com.axelor.apps.base.service.imports.ImportConfigurationService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class ImportConfigurationController {

  public void run(ActionRequest request, ActionResponse response) {

    ImportConfiguration importConfiguration =
        request.getContext().asType(ImportConfiguration.class);
    try {
      ImportConfigurationCallableService importConfigurationService =
          Beans.get(ImportConfigurationCallableService.class);
      importConfigurationService.setImportConfig(importConfiguration);
      ControllerCallableTool<ImportHistory> controllerCallableTool = new ControllerCallableTool<>();
      controllerCallableTool.runInSeparateThread(importConfigurationService, response);
    } catch (Exception e) {
      Beans.get(ImportConfigurationService.class).updateStatusError(importConfiguration);
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }
}
