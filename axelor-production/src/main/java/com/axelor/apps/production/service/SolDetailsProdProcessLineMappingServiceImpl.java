package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Objects;

public class SolDetailsProdProcessLineMappingServiceImpl
    implements SolDetailsProdProcessLineMappingService {

  protected final AppBaseService appBaseService;
  protected final AppProductionService appProductionService;
  protected final SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService;
  protected final ProdProcessLineComputationService prodProcessLineComputationService;

  @Inject
  public SolDetailsProdProcessLineMappingServiceImpl(
      AppBaseService appBaseService,
      AppProductionService appProductionService,
      SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService,
      ProdProcessLineComputationService prodProcessLineComputationService) {
    this.appBaseService = appBaseService;
    this.appProductionService = appProductionService;
    this.saleOrderLineDetailsPriceService = saleOrderLineDetailsPriceService;
    this.prodProcessLineComputationService = prodProcessLineComputationService;
  }

  @Override
  public SaleOrderLineDetails mapToSaleOrderLineDetails(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, ProdProcessLine prodProcessLine)
      throws AxelorException {
    Objects.requireNonNull(prodProcessLine);

    SaleOrderLineDetails saleOrderLineDetails = getDefaultOperationSolDetails(prodProcessLine);
    setQty(saleOrderLine, prodProcessLine, saleOrderLineDetails);
    saleOrderLineDetailsPriceService.computePrices(saleOrderLineDetails, saleOrder, saleOrderLine);
    setUnit(saleOrderLineDetails);

    return saleOrderLineDetails;
  }

  protected SaleOrderLineDetails getDefaultOperationSolDetails(ProdProcessLine prodProcessLine) {
    SaleOrderLineDetails saleOrderLineDetails = new SaleOrderLineDetails();
    saleOrderLineDetails.setProdProcessLine(prodProcessLine);
    saleOrderLineDetails.setTypeSelect(SaleOrderLineDetailsRepository.TYPE_OPERATION);
    saleOrderLineDetails.setTitle(prodProcessLine.getName());
    return saleOrderLineDetails;
  }

  protected void setQty(
      SaleOrderLine saleOrderLine,
      ProdProcessLine prodProcessLine,
      SaleOrderLineDetails saleOrderLineDetails)
      throws AxelorException {
    WorkCenter workCenter = prodProcessLine.getWorkCenter();
    BigDecimal nbCycle =
        prodProcessLineComputationService.getNbCycle(
            prodProcessLine, saleOrderLine.getQtyToProduce());
    int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();
    switch (workCenterTypeSelect) {
      case WorkCenterRepository.WORK_CENTER_TYPE_HUMAN:
        saleOrderLineDetails.setQty(
            prodProcessLineComputationService.getHourHumanDuration(prodProcessLine, nbCycle));
        break;
      case WorkCenterRepository.WORK_CENTER_TYPE_MACHINE:
        saleOrderLineDetails.setQty(
            prodProcessLineComputationService.getHourMachineDuration(prodProcessLine, nbCycle));
        break;
      default:
        saleOrderLineDetails.setQty(
            prodProcessLineComputationService.getHourTotalDuration(prodProcessLine, nbCycle));
    }
  }

  protected void setUnit(SaleOrderLineDetails saleOrderLineDetails) {
    AppBase appBase = appBaseService.getAppBase();
    saleOrderLineDetails.setUnit(appBase.getUnitHours());
  }
}
