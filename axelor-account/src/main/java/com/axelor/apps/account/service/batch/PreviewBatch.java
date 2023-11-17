package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.service.debtrecovery.DoubtfulCustomerService;
import com.google.inject.Inject;
import java.util.List;

public abstract class PreviewBatch extends BatchStrategy {
  protected List<Long> recordIdList;

  protected MoveLineRepository moveLineRepo;

  public PreviewBatch(MoveLineRepository moveLineRepo) {
    this.moveLineRepo = moveLineRepo;
  }

  @Inject
  public PreviewBatch(
      DoubtfulCustomerService doubtfulCustomerService,
      BatchAccountCustomer batchAccountCustomer,
      MoveLineRepository moveLineRepo) {
    super(doubtfulCustomerService, batchAccountCustomer);

    this.moveLineRepo = moveLineRepo;
  }

  public void setRecordIdList(List<Long> recordIdList) {
    this.recordIdList = recordIdList;
  }

  @Override
  protected void process() {
    AccountingBatch accountingBatch = batch.getAccountingBatch();

    if (this.recordIdList == null) {
      this._processByQuery(accountingBatch);
    } else {
      this._processByIds(accountingBatch);
    }
  }

  protected abstract void _processByQuery(AccountingBatch accountingBatch);

  protected abstract void _processByIds(AccountingBatch accountingBatch);
}
