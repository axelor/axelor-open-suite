package com.axelor.apps.businessproduction.service;

import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.service.SaleOrderLineBomSyncService;
import com.axelor.apps.production.service.SaleOrderLineDetailsBomSyncServiceImpl;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;

public class SaleOrderLineDetailsBomSyncBusinessProductionServiceImpl
    extends SaleOrderLineDetailsBomSyncServiceImpl {

  @Inject
  public SaleOrderLineDetailsBomSyncBusinessProductionServiceImpl(
      SaleOrderLineBomSyncService saleOrderLineBomSyncService) {
    super(saleOrderLineBomSyncService);
  }

  @Override
  public void syncSaleOrderLineDetailsBom(SaleOrderLineDetails saleOrderLineDetails) {
    SaleOrderLine projectSaleOrderLine = saleOrderLineDetails.getProjectSaleOrderLine();
    if (projectSaleOrderLine != null) {
      saleOrderLineBomSyncService.removeSolDetailsBomLine(
          projectSaleOrderLine.getProjectSaleOrderLineDetailsList(),
          projectSaleOrderLine.getBillOfMaterial());
    } else {
      super.syncSaleOrderLineDetailsBom(saleOrderLineDetails);
    }
  }
}
