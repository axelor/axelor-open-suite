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
        response.setInfo(batch.getComments());
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
        response.setInfo(batch.getComments());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
