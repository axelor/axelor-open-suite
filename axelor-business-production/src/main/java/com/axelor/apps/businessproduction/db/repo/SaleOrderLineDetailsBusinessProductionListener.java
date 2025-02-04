package com.axelor.apps.businessproduction.db.repo;

import com.axelor.apps.production.db.SaleOrderLineDetails;
import javax.persistence.PreRemove;

public class SaleOrderLineDetailsBusinessProductionListener {

  @PreRemove
  public void preRemove(SaleOrderLineDetails saleOrderLineDetails) {
    SaleOrderLineDetails originSaleOrderLineDetails =
        saleOrderLineDetails.getOriginSaleOrderLineDetails();
    if (originSaleOrderLineDetails != null) {
      originSaleOrderLineDetails.setBillOfMaterialLine(null);
    }
  }
}
