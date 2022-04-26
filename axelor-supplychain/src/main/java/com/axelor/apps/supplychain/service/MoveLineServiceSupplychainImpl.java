package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.service.moveline.MoveLineServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.supplychain.db.repo.SupplychainBatchRepository;
import com.axelor.apps.supplychain.service.batch.BatchAccountingCutOff;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.List;

public class MoveLineServiceSupplychainImpl extends MoveLineServiceImpl
    implements MoveLineServiceSupplychain {
  protected SupplychainBatchRepository supplychainBatchRepo;

  @Inject
  public MoveLineServiceSupplychainImpl(
      MoveLineRepository moveLineRepository,
      InvoiceRepository invoiceRepository,
      PaymentService paymentService,
      AppBaseService appBaseService,
      MoveLineToolService moveLineToolService,
      SupplychainBatchRepository supplychainBatchRepo) {
    super(
        moveLineRepository, invoiceRepository, paymentService, appBaseService, moveLineToolService);
    this.supplychainBatchRepo = supplychainBatchRepo;
  }

  @Override
  public Batch validateCutOffBatch(List<Long> recordIdList, Long batchId) {
    BatchAccountingCutOff batchAccountingCutOff = Beans.get(BatchAccountingCutOff.class);

    batchAccountingCutOff.recordIdList = recordIdList;
    batchAccountingCutOff.run(supplychainBatchRepo.find(batchId));

    return batchAccountingCutOff.getBatch();
  }
}
