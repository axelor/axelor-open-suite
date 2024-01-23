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
package com.axelor.apps.hr.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.HrBatch;
import com.axelor.apps.hr.service.batch.HrBatchService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class HrBatchController {

  /**
   * Launch any type of HR batch
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void launchHrBatch(ActionRequest request, ActionResponse response) {

    try {
      HrBatch hrBatch = request.getContext().asType(HrBatch.class);
      HrBatchService hrBatchService = Beans.get(HrBatchService.class);
      hrBatchService.setBatchModel(hrBatch);

      ControllerCallableTool<Batch> batchControllerCallableTool = new ControllerCallableTool<>();
      Batch batch = batchControllerCallableTool.runInSeparateThread(hrBatchService, response);

      if (batch != null) {
        response.setInfo(batch.getComments());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }
}
