package com.axelor.apps.contract.web;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.contract.db.ContractBatch;
import com.axelor.apps.contract.db.repo.ContractBatchRepository;
import com.axelor.apps.contract.service.BatchContract;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class ContractBatchController {

    public void runBatch(ActionRequest request, ActionResponse response) {
        try {
            ContractBatch contractBatch = request.getContext().asType(ContractBatch.class);
            contractBatch = Beans.get(ContractBatchRepository.class).find(contractBatch.getId());
            Batch batch = Beans.get(BatchContract.class).run(contractBatch);
            response.setFlash(batch.getComments());
            response.setReload(true);
        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
    }

}
