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
package com.axelor.apps.quality.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.service.QIIdentificationService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class QIIdentificationController {

  public void updateQIIdentification(ActionRequest request, ActionResponse response) {
    try {
      QIIdentification qiIdentification = request.getContext().asType(QIIdentification.class);
      Beans.get(QIIdentificationService.class).updateQIIdentification(qiIdentification);
      response.setValue("writtenBy", qiIdentification.getWrittenBy());
      response.setValue("writtenOn", qiIdentification.getWrittenOn());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
