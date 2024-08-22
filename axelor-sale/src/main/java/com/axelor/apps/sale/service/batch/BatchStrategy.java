package com.axelor.apps.sale.service.batch;

import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.sale.db.LoyaltyAccount;
import com.axelor.apps.sale.db.repo.LoyaltyAccountRepository;
import com.axelor.apps.sale.service.loyalty.LoyaltyAccountService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public abstract class BatchStrategy extends AbstractBatch {

  protected final LoyaltyAccountService loyaltyAccountService;
  protected final LoyaltyAccountRepository loyaltyAccountRepository;

  @Inject
  protected BatchStrategy(
      LoyaltyAccountService loyaltyAccountService,
      LoyaltyAccountRepository loyaltyAccountRepository) {
    super();
    this.loyaltyAccountService = loyaltyAccountService;
    this.loyaltyAccountRepository = loyaltyAccountRepository;
  }

  protected void updateLoyaltyAccount(LoyaltyAccount loyaltyAccount) {

    loyaltyAccount.addBatchSetItem(Beans.get(BatchRepository.class).find(batch.getId()));

    incrementDone();
  }

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_SALE_BATCH);
  }
}
