package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.interfaces.GlobalDiscounter;
import com.axelor.apps.base.service.discount.GlobalDiscountServiceImpl;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;

public class SaleOrderGlobalDiscountServiceImpl extends GlobalDiscountServiceImpl {

  protected final SaleOrderComputeService saleOrderComputeService;

  @Inject
  public SaleOrderGlobalDiscountServiceImpl(SaleOrderComputeService saleOrderComputeService) {
    this.saleOrderComputeService = saleOrderComputeService;
  }

  @Override
  protected void compute(GlobalDiscounter globalDiscounter) throws AxelorException {
    if (globalDiscounter instanceof SaleOrder) {
      saleOrderComputeService.computeSaleOrder((SaleOrder) globalDiscounter);
    }
  }
}
