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
package com.axelor.studio.web;

import com.axelor.apps.base.db.App;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.ModuleBuilder;
import com.axelor.studio.service.module.ModuleExportService;
import com.google.inject.Inject;

public class ModuleBuilderController {

  @Inject private ModuleExportService moduleExportService;

  public void exportModule(ActionRequest request, ActionResponse response) {

    try {
      ModuleBuilder moduleBuilder = request.getContext().asType(ModuleBuilder.class);
      MetaFile metaFile = moduleExportService.export(moduleBuilder.getName());

      response.setView(
          ActionView.define(I18n.get("Module export"))
              .model(App.class.getName())
              .add(
                  "html",
                  "ws/rest/com.axelor.meta.db.MetaFile/"
                      + metaFile.getId()
                      + "/content/download?v="
                      + metaFile.getVersion())
              .param("download", "true")
              .map());

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }
}
