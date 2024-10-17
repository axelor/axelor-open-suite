package com.axelor.apps.contract.batch;

import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;

public abstract class BatchStrategy extends AbstractBatch {

  @Override
  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_CONTRACT_BATCH);
  }

  @Override
  protected Integer getFetchLimit() {
    Integer batchFetchLimit = this.batch.getContractBatch().getFetchLimit();
    if (batchFetchLimit == 0) {
      batchFetchLimit = super.getFetchLimit();
    }
    return batchFetchLimit;
  }
}
