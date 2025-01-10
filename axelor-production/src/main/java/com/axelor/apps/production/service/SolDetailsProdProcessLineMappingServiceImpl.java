package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.studio.db.AppBase;
import com.axelor.studio.db.AppProduction;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class SolDetailsProdProcessLineMappingServiceImpl
    implements SolDetailsProdProcessLineMappingService {

  protected final AppBaseService appBaseService;
  protected final AppProductionService appProductionService;

  @Inject
  public SolDetailsProdProcessLineMappingServiceImpl(
      AppBaseService appBaseService, AppProductionService appProductionService) {
    this.appBaseService = appBaseService;
    this.appProductionService = appProductionService;
  }

  @Override
  public SaleOrderLineDetails mapToSaleOrderLineDetails(ProdProcessLine prodProcessLine) {
    Objects.requireNonNull(prodProcessLine);

    AppBase appBase = appBaseService.getAppBase();
    int digitForQty = appBase.getNbDecimalDigitForQty();
    Unit productUnit = prodProcessLine.getProdProcess().getProduct().getUnit();
    WorkCenter workCenter = prodProcessLine.getWorkCenter();
    SaleOrderLineDetails saleOrderLineDetails = getDefaultOperationSolDetails(prodProcessLine);
    if (workCenter != null) {
      setWorkCenterValues(workCenter, saleOrderLineDetails, digitForQty, productUnit);
      BigDecimal totalPrice =
          saleOrderLineDetails
              .getPrice()
              .multiply(saleOrderLineDetails.getQty())
              .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
      saleOrderLineDetails.setTotalPrice(totalPrice);
      return saleOrderLineDetails;
    }
    return saleOrderLineDetails;
  }

  protected void setWorkCenterValues(
      WorkCenter workCenter,
      SaleOrderLineDetails saleOrderLineDetails,
      int digitForQty,
      Unit productUnit) {
    saleOrderLineDetails.setWorkCenter(workCenter);
    saleOrderLineDetails.setTitle(workCenter.getName());

    switch (workCenter.getWorkCenterTypeSelect()) {
      case WorkCenterRepository.WORK_CENTER_TYPE_HUMAN:
        saleOrderLineDetails.setQty(
            new BigDecimal(workCenter.getHrDurationPerCycle())
                .divide(BigDecimal.valueOf(3600), digitForQty, RoundingMode.HALF_UP));
        setHumanUnit(saleOrderLineDetails, workCenter, productUnit);
        saleOrderLineDetails.setPrice(workCenter.getHrCostAmount());
        break;
      case WorkCenterRepository.WORK_CENTER_TYPE_MACHINE:
        saleOrderLineDetails.setQty(
            new BigDecimal(workCenter.getDurationPerCycle())
                .divide(BigDecimal.valueOf(3600), digitForQty, RoundingMode.HALF_UP));
        setMachineUnit(saleOrderLineDetails, workCenter, productUnit);
        saleOrderLineDetails.setPrice(workCenter.getCostAmount());
        break;
      case WorkCenterRepository.WORK_CENTER_TYPE_BOTH:
        saleOrderLineDetails.setQty(
            new BigDecimal(workCenter.getDurationPerCycle())
                .divide(BigDecimal.valueOf(3600), digitForQty, RoundingMode.HALF_UP));
        saleOrderLineDetails.setPrice(workCenter.getCostAmount());
        break;
      default:
        break;
    }
  }

  protected SaleOrderLineDetails getDefaultOperationSolDetails(ProdProcessLine prodProcessLine) {
    SaleOrderLineDetails saleOrderLineDetails = new SaleOrderLineDetails();
    saleOrderLineDetails.setProdProcessLine(prodProcessLine);
    saleOrderLineDetails.setTypeSelect(SaleOrderLineDetailsRepository.TYPE_OPERATION);
    return saleOrderLineDetails;
  }

  protected void setMachineUnit(
      SaleOrderLineDetails saleOrderLineDetails, WorkCenter workCenter, Unit productUnit) {
    AppProduction appProduction = appProductionService.getAppProduction();
    AppBase appBase = appBaseService.getAppBase();
    switch (workCenter.getCostTypeSelect()) {
      case WorkCenterRepository.COST_TYPE_PER_HOUR:
        saleOrderLineDetails.setUnit(appBase.getUnitHours());
        break;
      case WorkCenterRepository.COST_TYPE_PER_CYCLE:
        saleOrderLineDetails.setUnit(appProduction.getCycleUnit());
        break;
      case WorkCenterRepository.COST_TYPE_PER_PIECE:
      default:
        saleOrderLineDetails.setUnit(productUnit);
        break;
    }
  }

  protected void setHumanUnit(
      SaleOrderLineDetails saleOrderLineDetails, WorkCenter workCenter, Unit productUnit) {
    AppBase appBase = appBaseService.getAppBase();
    switch (workCenter.getHrCostTypeSelect()) {
      case WorkCenterRepository.COST_TYPE_PER_HOUR:
        saleOrderLineDetails.setUnit(appBase.getUnitHours());
        break;
      case WorkCenterRepository.COST_TYPE_PER_PIECE:
      default:
        saleOrderLineDetails.setUnit(productUnit);
        break;
    }
  }
}
