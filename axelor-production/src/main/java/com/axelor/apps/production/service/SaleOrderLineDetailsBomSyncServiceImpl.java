package com.axelor.apps.production.service;

import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;

public class SaleOrderLineDetailsBomSyncServiceImpl implements SaleOrderLineDetailsBomSyncService {

  protected final SaleOrderLineBomSyncService saleOrderLineBomSyncService;

  @Inject
  public SaleOrderLineDetailsBomSyncServiceImpl(
      SaleOrderLineBomSyncService saleOrderLineBomSyncService) {
    this.saleOrderLineBomSyncService = saleOrderLineBomSyncService;
  }

  @Override
  public void syncSaleOrderLineDetailsBom(SaleOrderLineDetails saleOrderLineDetails) {
    SaleOrderLine saleOrderLine = saleOrderLineDetails.getSaleOrderLine();
    if (saleOrderLine != null) {
      saleOrderLineBomSyncService.removeBomLines(saleOrderLine);
    }
  }
}
