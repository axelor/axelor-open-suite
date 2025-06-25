package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SolDetailsProdProcessComputeQtyServiceImpl
    implements SolDetailsProdProcessComputeQtyService {

  protected final AppBaseService appBaseService;
  protected final ProdProcessLineComputationService prodProcessLineComputationService;

  @Inject
  public SolDetailsProdProcessComputeQtyServiceImpl(
      AppBaseService appBaseService,
      ProdProcessLineComputationService prodProcessLineComputationService) {
    this.appBaseService = appBaseService;
    this.prodProcessLineComputationService = prodProcessLineComputationService;
  }

  @Override
  public void setQty(
      SaleOrderLine saleOrderLine,
      ProdProcessLine prodProcessLine,
      SaleOrderLineDetails saleOrderLineDetails)
      throws AxelorException {
    int nbDecimalForQty = appBaseService.getNbDecimalDigitForQty();
    WorkCenter workCenter = prodProcessLine.getWorkCenter();
    BigDecimal nbCycle =
        prodProcessLineComputationService.getNbCycle(
            prodProcessLine, saleOrderLine.getQtyToProduce());
    int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();
    switch (workCenterTypeSelect) {
      case WorkCenterRepository.WORK_CENTER_TYPE_HUMAN:
        saleOrderLineDetails.setQty(
            prodProcessLineComputationService
                .getHourHumanDuration(prodProcessLine, nbCycle)
                .setScale(nbDecimalForQty, RoundingMode.HALF_UP));
        break;
      case WorkCenterRepository.WORK_CENTER_TYPE_MACHINE:
        saleOrderLineDetails.setQty(
            prodProcessLineComputationService
                .getHourMachineDuration(prodProcessLine, nbCycle)
                .setScale(nbDecimalForQty, RoundingMode.HALF_UP));
        break;
      default:
        saleOrderLineDetails.setQty(
            prodProcessLineComputationService
                .getHourTotalDuration(prodProcessLine, nbCycle)
                .setScale(nbDecimalForQty, RoundingMode.HALF_UP));
    }
  }
}
