package com.axelor.apps.production.db.repo;

import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.service.SaleOrderLineDetailsBomSyncService;
import com.axelor.inject.Beans;
import javax.persistence.PostRemove;

public class SaleOrderLineDetailsListener {

  @PostRemove
  public void postRemove(SaleOrderLineDetails saleOrderLineDetails) {
    Beans.get(SaleOrderLineDetailsBomSyncService.class)
        .syncSaleOrderLineDetailsBom(saleOrderLineDetails);
  }
}
