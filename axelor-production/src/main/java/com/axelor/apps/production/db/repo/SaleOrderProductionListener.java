package com.axelor.apps.production.db.repo;

import com.axelor.apps.production.service.SaleOrderLineBomSyncService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.inject.Beans;
import javax.persistence.PostUpdate;

public class SaleOrderProductionListener {

  @PostUpdate
  public void saleOrderPostUpdate(SaleOrder saleOrder) {
    Beans.get(SaleOrderLineBomSyncService.class).syncSaleOrderLineBom(saleOrder);
  }
}
