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
package com.axelor.apps.stock.web;

import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.stock.db.StockBatch;
import com.axelor.apps.stock.db.repo.StockBatchRepository;
import com.axelor.apps.stock.service.batch.StockBatchService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class StockBatchController {

  public void runBatch(ActionRequest request, ActionResponse response) {
    try {
      StockBatch stockBatch = request.getContext().asType(StockBatch.class);
      StockBatchService stockBatchService = Beans.get(StockBatchService.class);
      stockBatchService.setBatchModel(
          Beans.get(StockBatchRepository.class).find(stockBatch.getId()));
      ControllerCallableTool<Batch> controllerCallableTool = new ControllerCallableTool<>();

      Batch batch = controllerCallableTool.runInSeparateThread(stockBatchService, response);

      if (batch != null) {
        response.setFlash(batch.getComments());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }
}
