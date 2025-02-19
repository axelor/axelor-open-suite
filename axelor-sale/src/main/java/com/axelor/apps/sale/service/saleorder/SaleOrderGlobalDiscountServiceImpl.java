package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.interfaces.GlobalDiscounter;
import com.axelor.apps.base.interfaces.GlobalDiscounterLine;
import com.axelor.apps.base.service.discount.GlobalDiscountAbstractService;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;
import java.util.List;

public class SaleOrderGlobalDiscountServiceImpl extends GlobalDiscountAbstractService
    implements SaleOrderGlobalDiscountService {

  protected final SaleOrderComputeService saleOrderComputeService;

  @Inject
  public SaleOrderGlobalDiscountServiceImpl(SaleOrderComputeService saleOrderComputeService) {
    this.saleOrderComputeService = saleOrderComputeService;
  }

  @Override
  protected void compute(GlobalDiscounter globalDiscounter) throws AxelorException {
    saleOrderComputeService.computeSaleOrder(getSaleOrder(globalDiscounter));
  }

  @Override
  protected List<? extends GlobalDiscounterLine> getGlobalDiscounterLines(
      GlobalDiscounter globalDiscounter) {
    return getSaleOrder(globalDiscounter).getSaleOrderLineList();
  }

  protected SaleOrder getSaleOrder(GlobalDiscounter globalDiscounter) {
    SaleOrder saleOrder = null;
    if (globalDiscounter instanceof SaleOrder) {
      saleOrder = (SaleOrder) globalDiscounter;
    }
    return saleOrder;
  }
}
