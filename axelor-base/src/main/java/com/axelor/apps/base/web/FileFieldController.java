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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.FileField;
import com.axelor.apps.base.service.advanced.imports.FileFieldService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class FileFieldController {

  public void fillType(ActionRequest request, ActionResponse response) {
    try {
      FileField fileField = request.getContext().asType(FileField.class);
      fileField = Beans.get(FileFieldService.class).fillType(fileField);
      response.setValues(fileField);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
