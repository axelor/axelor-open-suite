package com.axelor.apps.sale.service.batch;

import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.google.inject.Inject;

public abstract class BatchStrategy extends AbstractBatch {

  protected final AppSaleService appSaleService;

  @Inject
  protected BatchStrategy(AppSaleService appSaleService) {
    super();
    this.appSaleService = appSaleService;
  }

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_SALE_BATCH);
  }
}
