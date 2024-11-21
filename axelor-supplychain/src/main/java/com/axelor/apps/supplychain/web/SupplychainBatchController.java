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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.db.repo.SupplychainBatchRepository;
import com.axelor.apps.supplychain.service.batch.SupplychainBatchService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class SupplychainBatchController {

  public void runBatch(ActionRequest request, ActionResponse response) {
    try {
      SupplychainBatch supplychainBatch = request.getContext().asType(SupplychainBatch.class);
      SupplychainBatchService supplychainBatchService = Beans.get(SupplychainBatchService.class);
      supplychainBatchService.setBatchModel(
          Beans.get(SupplychainBatchRepository.class).find(supplychainBatch.getId()));
      ControllerCallableTool<Batch> controllerCallableTool = new ControllerCallableTool<>();

      Batch batch = controllerCallableTool.runInSeparateThread(supplychainBatchService, response);

      if (batch != null) {
        response.setInfo(batch.getComments());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void updateStockHistory(ActionRequest request, ActionResponse response) {
    try {
      SupplychainBatch supplychainBatch = request.getContext().asType(SupplychainBatch.class);
      supplychainBatch = Beans.get(SupplychainBatchRepository.class).find(supplychainBatch.getId());
      Batch batch = Beans.get(SupplychainBatchService.class).updateStockHistory(supplychainBatch);
      response.setInfo(batch.getComments());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
