package com.axelor.apps.base.job;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public abstract class ThreadedBaseJob extends ThreadedJob {
  @Inject protected BatchRepository batchRepo;

  protected void executeBatch(Class<? extends AbstractBatchService> batchService, String batchCode)
      throws AxelorException {
    Batch batch = Beans.get(batchService).run(batchCode);
    this.updateBatchOrigin(batch);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void updateBatchOrigin(Batch batch) {
    batch.setActionLaunchOrigin(BatchRepository.ACTION_LAUNCH_ORIGIN_SCHEDULED);
    batchRepo.save(batch);
  }
}
