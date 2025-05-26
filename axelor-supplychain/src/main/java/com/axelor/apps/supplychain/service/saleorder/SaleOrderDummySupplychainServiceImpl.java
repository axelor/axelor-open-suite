package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderVersionService;
import com.axelor.apps.sale.service.saleorder.views.SaleOrderDummyServiceImpl;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderDummySupplychainServiceImpl extends SaleOrderDummyServiceImpl {

  protected final AccountingSituationSupplychainService accountingSituationSupplychainService;

  @Inject
  public SaleOrderDummySupplychainServiceImpl(
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      SaleOrderVersionService saleOrderVersionService,
      AccountingSituationSupplychainService accountingSituationSupplychainService) {
    super(appBaseService, appSaleService, saleOrderVersionService);
    this.accountingSituationSupplychainService = accountingSituationSupplychainService;
  }

  @Override
  public Map<String, Object> getOnLoadSplitDummies(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> dummies = super.getOnLoadSplitDummies(saleOrder);
    dummies.putAll(fillIsUsedCreditExceeded(saleOrder));
    return dummies;
  }

  protected Map<String, Object> fillIsUsedCreditExceeded(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> dummies = new HashMap<>();
    dummies.put(
        "$isUsedCreditExceeded",
        accountingSituationSupplychainService.isUsedCreditExceeded(saleOrder));
    return dummies;
  }
}
