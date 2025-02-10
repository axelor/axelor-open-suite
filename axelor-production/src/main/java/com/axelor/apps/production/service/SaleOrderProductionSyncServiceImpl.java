package com.axelor.apps.production.service;

import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.List;

public class SaleOrderProductionSyncServiceImpl extends SaleOrderSyncAbstractService
    implements SaleOrderProductionSyncService {

  @Inject
  protected SaleOrderProductionSyncServiceImpl(
      SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService,
      SaleOrderLineBomService saleOrderLineBomService,
      SaleOrderLineDetailsBomService saleOrderLineDetailsBomService,
      SolBomCustomizationService solBomCustomizationService,
      SolDetailsBomUpdateService solDetailsBomUpdateService,
      SolBomUpdateService solBomUpdateService,
      AppProductionService appProductionService) {
    super(
        saleOrderLineBomLineMappingService,
        saleOrderLineBomService,
        saleOrderLineDetailsBomService,
        solBomCustomizationService,
        solDetailsBomUpdateService,
        solBomUpdateService,
        appProductionService);
  }

  @Override
  protected List<SaleOrderLineDetails> getSaleOrderListDetailsList(SaleOrderLine saleOrderLine) {
    return saleOrderLine.getSaleOrderLineDetailsList();
  }
}
