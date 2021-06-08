package com.axelor.apps.account.job;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.batch.AccountingBatchService;
import com.axelor.apps.base.job.ThreadedJob;
import com.axelor.apps.base.job.UncheckedJobExecutionException;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import org.quartz.JobExecutionContext;

public class BlockCustomersWithLatePaymentsJob extends ThreadedJob {

  @Override
  public void executeInThread(JobExecutionContext context) {
    try {
      AccountingBatch accountingBatch =
          Beans.get(AccountingBatchRepository.class)
              .all()
              .filter("self.actionSelect = :select")
              .bind("select", AccountingBatchRepository.ACTION_LATE_PAYMENT_CUSTOMER_BLOCKING)
              .fetchOne();

      if (ObjectUtils.notEmpty(accountingBatch)) {
        Beans.get(AccountingBatchService.class).blockCustomersWithLatePayments(accountingBatch);
      } else {
        TraceBackService.trace(
            new AxelorException(
                TraceBackRepository.CATEGORY_INCONSISTENCY,
                String.format(IExceptionMessage.BATCH_DOES_NOT_EXIST)));
      }
    } catch (Exception e) {
      throw new UncheckedJobExecutionException(e);
    }
  }
}
