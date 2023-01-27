/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.ImportBatch;
import com.axelor.apps.base.db.repo.ImportBatchRepository;
import com.axelor.apps.base.service.batch.ImportBatchService;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ImportBatchController {

  public void importData(ActionRequest request, ActionResponse response) {

    try {
      ImportBatch importBatch =
          Beans.get(ImportBatchRepository.class)
              .find(request.getContext().asType(ImportBatch.class).getId());
      if (importBatch != null) {
        Batch batch = Beans.get(ImportBatchService.class).importData(importBatch);
        response.setReload(true);
        response.setFlash(batch.getComments());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void advancedImportData(ActionRequest request, ActionResponse response) {
    try {
      ImportBatch importBatch =
          Beans.get(ImportBatchRepository.class)
              .find(request.getContext().asType(ImportBatch.class).getId());
      if (importBatch != null) {
        Batch batch = Beans.get(ImportBatchService.class).advancedImportData(importBatch);
        response.setReload(true);
        response.setFlash(batch.getComments());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
