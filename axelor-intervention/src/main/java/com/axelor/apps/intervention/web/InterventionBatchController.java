/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.intervention.web;

import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.intervention.db.InterventionBatch;
import com.axelor.apps.intervention.db.repo.InterventionBatchRepository;
import com.axelor.apps.intervention.service.batch.InterventionBatchService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class InterventionBatchController {

  public void generateInterventions(ActionRequest request, ActionResponse response) {
    try {
      InterventionBatch interventionBatch = request.getContext().asType(InterventionBatch.class);
      interventionBatch =
          Beans.get(InterventionBatchRepository.class).find(interventionBatch.getId());

      InterventionBatchService interventionBatchService = Beans.get(InterventionBatchService.class);
      interventionBatchService.setBatchModel(interventionBatch);

      ControllerCallableTool<Batch> controllerCallableTool = new ControllerCallableTool<>();
      Batch batch = controllerCallableTool.runInSeparateThread(interventionBatchService, response);
      if (batch != null) {
        response.setInfo(batch.getComments());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
