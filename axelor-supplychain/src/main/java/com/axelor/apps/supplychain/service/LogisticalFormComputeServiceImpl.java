package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.supplychain.service.packaging.PackagingMassService;
import com.axelor.apps.supplychain.service.packaging.PackagingStockMoveLineService;
import com.google.inject.Inject;

public class LogisticalFormComputeServiceImpl implements LogisticalFormComputeService {

  protected final PackagingStockMoveLineService packagingStockMoveLineService;
  protected final PackagingMassService packagingMassService;

  @Inject
  public LogisticalFormComputeServiceImpl(
      PackagingStockMoveLineService packagingStockMoveLineService,
      PackagingMassService packagingMassService) {
    this.packagingStockMoveLineService = packagingStockMoveLineService;
    this.packagingMassService = packagingMassService;
  }

  @Override
  public void computeLogisticalForm(LogisticalForm logisticalForm) throws AxelorException {
    packagingStockMoveLineService.updateQtyRemainingToPackage(logisticalForm);
    packagingMassService.updatePackagingMass(logisticalForm);
    packagingStockMoveLineService.updateStockMovePackagingInfo(logisticalForm);
  }
}
