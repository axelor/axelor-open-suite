package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.repo.LogisticalFormStockRepository;
import com.axelor.apps.stock.service.LogisticalFormSequenceService;
import com.axelor.apps.supplychain.service.LogisticalFormComputeService;
import com.axelor.apps.supplychain.service.packaging.PackagingSequenceService;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class LogisticalFormSupplychainRepository extends LogisticalFormStockRepository {

  protected PackagingSequenceService packagingSequenceService;
  protected LogisticalFormComputeService logisticalFormComputeService;

  @Inject
  public LogisticalFormSupplychainRepository(
      LogisticalFormSequenceService logisticalFormSequenceService,
      PackagingSequenceService packagingSequenceService,
      LogisticalFormComputeService logisticalFormComputeService) {
    super(logisticalFormSequenceService);
    this.packagingSequenceService = packagingSequenceService;
    this.logisticalFormComputeService = logisticalFormComputeService;
  }

  @Override
  public LogisticalForm save(LogisticalForm logisticalForm) {
    try {
      packagingSequenceService.generatePackagingNumber(logisticalForm);
      logisticalFormComputeService.computeLogisticalForm(logisticalForm);
      return super.save(logisticalForm);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}
