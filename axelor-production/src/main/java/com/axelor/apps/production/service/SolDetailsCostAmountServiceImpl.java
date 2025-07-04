package com.axelor.apps.production.service;

import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class SolDetailsCostAmountServiceImpl implements SolDetailsCostAmountService {

  protected final ProdProcessLineHourlyCostComputeService prodProcessLineHourlyCostComputeService;
  protected final ProdProcessLineComputationService prodProcessLineComputationService;
  protected final SolDetailsDurationService solDetailsDurationService;

  @Inject
  public SolDetailsCostAmountServiceImpl(
      ProdProcessLineHourlyCostComputeService prodProcessLineHourlyCostComputeService,
      ProdProcessLineComputationService prodProcessLineComputationService,
      SolDetailsDurationService solDetailsDurationService) {
    this.prodProcessLineHourlyCostComputeService = prodProcessLineHourlyCostComputeService;
    this.prodProcessLineComputationService = prodProcessLineComputationService;
    this.solDetailsDurationService = solDetailsDurationService;
  }

  @Override
  public BigDecimal computeSolDetailsMachineCostAmount(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrderLine saleOrderLine) {
    BigDecimal nbCycles =
        prodProcessLineComputationService.computeNbCycle(
            saleOrderLine.getQtyToProduce(), saleOrderLineDetails.getMaxCapacityPerCycle());
    BigDecimal machineCostAmount =
        prodProcessLineHourlyCostComputeService.getMachineCostAmount(
            saleOrderLineDetails.getDurationPerCycle(),
            saleOrderLineDetails.getMaxCapacityPerCycle(),
            saleOrderLineDetails.getCostTypeSelect(),
            nbCycles,
            saleOrderLineDetails.getCostAmount());
    return machineCostAmount.multiply(
        solDetailsDurationService.computeSolDetailsDuration(saleOrderLineDetails, saleOrderLine));
  }

  @Override
  public BigDecimal computeSolDetailsHumanCostAmount(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrderLine saleOrderLine) {
    BigDecimal humanCostAmount =
        prodProcessLineHourlyCostComputeService.getHumanCostAmount(
            saleOrderLineDetails.getHrCostTypeSelect(),
            saleOrderLineDetails.getHrCostAmount(),
            saleOrderLineDetails.getProdProcessLine().getWorkCenter());
    return humanCostAmount.multiply(
        solDetailsDurationService.computeSolDetailsHumanDuration(
            saleOrderLineDetails, saleOrderLine));
  }
}
