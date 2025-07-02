package com.axelor.apps.production.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SolDetailsDurationServiceImpl implements SolDetailsDurationService {

  protected final ProdProcessLineComputationService prodProcessLineComputationService;

  @Inject
  public SolDetailsDurationServiceImpl(
      ProdProcessLineComputationService prodProcessLineComputationService) {
    this.prodProcessLineComputationService = prodProcessLineComputationService;
  }

  @Override
  public BigDecimal computeSolDetailsDuration(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrderLine saleOrderLine) {
    BigDecimal nbCycle =
        prodProcessLineComputationService.computeNbCycle(
            saleOrderLine.getQtyToProduce(), saleOrderLineDetails.getMaxCapacityPerCycle());
    return prodProcessLineComputationService
        .computeMachineDuration(
            nbCycle,
            saleOrderLineDetails.getDurationPerCycle(),
            prodProcessLineComputationService.computeMachineInstallingDuration(
                nbCycle,
                saleOrderLineDetails.getStartingDuration(),
                saleOrderLineDetails.getEndingDuration(),
                saleOrderLineDetails.getSetupDuration()))
        .divide(BigDecimal.valueOf(3600), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }
}
