package com.axelor.apps.businessproduction.service;

import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.google.inject.Inject;

public class SolDetailsRemoveBusinessProductionServiceImpl
    implements SolDetailsRemoveBusinessProductionService {

  protected final SaleOrderLineDetailsRepository saleOrderLineDetailsRepository;

  @Inject
  public SolDetailsRemoveBusinessProductionServiceImpl(
      SaleOrderLineDetailsRepository saleOrderLineDetailsRepository) {
    this.saleOrderLineDetailsRepository = saleOrderLineDetailsRepository;
  }

  @Override
  public void removeSaleOrderLineDetails(SaleOrderLineDetails saleOrderLineDetails) {
    SaleOrderLineDetails originSaleOrderLineDetails =
        saleOrderLineDetails.getOriginSaleOrderLineDetails();
    if (originSaleOrderLineDetails != null) {
      originSaleOrderLineDetails.setBillOfMaterialLine(null);
    }
    if (saleOrderLineDetails.getSaleOrderLine() != null) {
      SaleOrderLineDetails linkedSolDetails =
          saleOrderLineDetailsRepository
              .all()
              .autoFlush(false)
              .filter("self.originSaleOrderLineDetails = :originSolDetails")
              .bind("originSolDetails", saleOrderLineDetails)
              .fetchOne();
      if (linkedSolDetails != null) {
        linkedSolDetails.setOriginSaleOrderLineDetails(null);
      }
    }
  }
}
