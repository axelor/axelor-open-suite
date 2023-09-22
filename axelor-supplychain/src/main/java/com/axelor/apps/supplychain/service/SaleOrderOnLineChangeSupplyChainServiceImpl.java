package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderOnLineChangeServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.google.inject.Inject;

public class SaleOrderOnLineChangeSupplyChainServiceImpl extends SaleOrderOnLineChangeServiceImpl {

  protected SaleOrderSupplychainService saleOrderSupplychainService;

  @Inject
  public SaleOrderOnLineChangeSupplyChainServiceImpl(
      AppSaleService appSaleService,
      SaleOrderService saleOrderService,
      SaleOrderLineService saleOrderLineService,
      SaleOrderMarginService saleOrderMarginService,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderSupplychainService saleOrderSupplychainService) {
    super(
        appSaleService,
        saleOrderService,
        saleOrderLineService,
        saleOrderMarginService,
        saleOrderComputeService,
        saleOrderLineRepository);
    this.saleOrderSupplychainService = saleOrderSupplychainService;
  }

  @Override
  public void onLineChange(SaleOrder saleOrder) throws AxelorException {
    super.onLineChange(saleOrder);
    saleOrderSupplychainService.setAdvancePayment(saleOrder);
    saleOrderSupplychainService.updateTimetableAmounts(saleOrder);
    saleOrderSupplychainService.updateAmountToBeSpreadOverTheTimetable(saleOrder);
  }
}
