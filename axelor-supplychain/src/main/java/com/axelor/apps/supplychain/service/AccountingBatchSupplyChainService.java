package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.bankpayment.service.batch.AccountingBatchBankPaymentService;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.supplychain.service.batch.BatchAccountingCutOffSupplyChain;
import com.axelor.inject.Beans;

public class AccountingBatchSupplyChainService extends AccountingBatchBankPaymentService {

  @Override
  public Batch accountingCutOff(AccountingBatch accountingBatch) {
    return Beans.get(BatchAccountingCutOffSupplyChain.class).run(accountingBatch);
  }
}
