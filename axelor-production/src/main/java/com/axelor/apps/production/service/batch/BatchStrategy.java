package com.axelor.apps.production.service.batch;

import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;

public abstract class BatchStrategy extends AbstractBatch {

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_PRODUCTION_BATCH);
  }

  @Override
  protected Integer getFetchLimit() {
    Integer batchFetchLimit = this.batch.getProductionBatch().getFetchLimit();
    if (batchFetchLimit == 0) {
      batchFetchLimit = super.getFetchLimit();
    }
    return batchFetchLimit;
  }
}
