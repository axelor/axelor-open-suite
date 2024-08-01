package com.axelor.apps.base.service.batch;

import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;

public abstract class MailBatchStrategy extends AbstractBatch {

  @Override
  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_MAIL_BATCH);
  }

  @Override
  protected Integer getFetchLimit() {
    Integer batchFetchLimit = this.batch.getMailBatch().getFetchLimit();
    if (batchFetchLimit == 0) {
      batchFetchLimit = super.getFetchLimit();
    }
    return batchFetchLimit;
  }
}
