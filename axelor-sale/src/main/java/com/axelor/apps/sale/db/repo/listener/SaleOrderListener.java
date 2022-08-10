package com.axelor.apps.sale.db.repo.listener;

import com.axelor.apps.base.db.AppSale;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import javax.persistence.PrePersist;

public class SaleOrderListener {

  @PrePersist
  public void beforeSave(SaleOrder saleOrder) throws AxelorException {

    AppSale appSale = Beans.get(AppSaleService.class).getAppSale();

    if (appSale.getManagePartnerComplementaryProduct()) {
      Beans.get(SaleOrderService.class).manageComplementaryProductSOLines(saleOrder);
    }
  }
}
