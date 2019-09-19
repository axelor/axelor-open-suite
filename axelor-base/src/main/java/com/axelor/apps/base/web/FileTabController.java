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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.FileTab;
import com.axelor.apps.base.service.advanced.imports.FileTabService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import java.util.Map;

public class FileTabController {

  @Inject private FileTabService fileTabService;

  public void updateFields(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Map<String, Object> map = context.getParent();
      if (map == null || (boolean) map.get("isConfigInFile") == true) {
        return;
      }

      FileTab fileTab = context.asType(FileTab.class);
      fileTabService.updateFields(fileTab);
      response.setValue("fileFieldList", fileTab.getFileFieldList());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void compute(ActionRequest request, ActionResponse response) {
    try {
      FileTab fileTab = request.getContext().asType(FileTab.class);
      fileTab = fileTabService.compute(fileTab);
      response.setValues(fileTab);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
