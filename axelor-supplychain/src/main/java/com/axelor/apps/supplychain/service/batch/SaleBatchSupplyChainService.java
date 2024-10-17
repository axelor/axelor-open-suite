package com.axelor.apps.supplychain.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.sale.db.SaleBatch;
import com.axelor.apps.sale.db.repo.SaleBatchRepository;
import com.axelor.apps.sale.service.batch.SaleBatchService;
import com.axelor.db.Model;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class SaleBatchSupplyChainService extends SaleBatchService {

  @Inject
  public SaleBatchSupplyChainService(SaleBatchRepository saleBatchRepo) {
    super(saleBatchRepo);
  }

  @Override
  public Batch run(Model batchModel) throws AxelorException {
    SaleBatch saleBatch = (SaleBatch) batchModel;
    switch (saleBatch.getActionSelect()) {
      case SaleBatchRepository.ACTION_INVOICING:
        return generateSubscriberInvoices(saleBatch);
      default:
        return super.run(batchModel);
    }
  }

  protected Batch generateSubscriberInvoices(SaleBatch saleBatch) {
    return Beans.get(BatchInvoicing.class).run(saleBatch);
  }
}
