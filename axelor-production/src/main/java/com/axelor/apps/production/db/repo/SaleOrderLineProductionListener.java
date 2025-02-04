package com.axelor.apps.production.db.repo;

import com.axelor.apps.production.service.SaleOrderLineBomSyncService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.inject.Beans;
import javax.persistence.PostUpdate;

public class SaleOrderLineProductionListener {

  @PostUpdate
  void postUpdate(SaleOrderLine saleOrderLine) {
    Beans.get(SaleOrderLineBomSyncService.class).removeBomLines(saleOrderLine);
  }
}
