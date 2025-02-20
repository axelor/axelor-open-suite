package com.axelor.apps.businessproduction.db.repo;

import com.axelor.apps.businessproduction.service.SolDetailsRemoveBusinessProductionService;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.inject.Beans;
import javax.persistence.PreRemove;

public class SaleOrderLineDetailsBusinessProductionListener {

  @PreRemove
  public void preRemove(SaleOrderLineDetails saleOrderLineDetails) {
    Beans.get(SolDetailsRemoveBusinessProductionService.class)
        .removeSaleOrderLineDetails(saleOrderLineDetails);
  }
}
