package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePackService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineProductService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderOnLineChangeSupplyChainServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderShipmentService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderSupplychainService;
import com.google.inject.Inject;

public class SaleOrderOnLineChangeProductionServiceImpl
    extends SaleOrderOnLineChangeSupplyChainServiceImpl {

  protected final AppProductionService appProductionService;
  protected final SaleOrderProductionSyncService saleOrderProductionSyncService;

  @Inject
  public SaleOrderOnLineChangeProductionServiceImpl(
      AppSaleService appSaleService,
      SaleOrderService saleOrderService,
      SaleOrderMarginService saleOrderMarginService,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLinePackService saleOrderLinePackService,
      SaleOrderLineProductService saleOrderLineProductService,
      SaleOrderSupplychainService saleOrderSupplychainService,
      SaleOrderShipmentService saleOrderShipmentService,
      AppProductionService appProductionService,
      SaleOrderProductionSyncService saleOrderProductionSyncService) {
    super(
        appSaleService,
        saleOrderService,
        saleOrderMarginService,
        saleOrderComputeService,
        saleOrderLineRepository,
        saleOrderLineComputeService,
        saleOrderLinePackService,
        saleOrderLineProductService,
        saleOrderSupplychainService,
        saleOrderShipmentService);

    this.appProductionService = appProductionService;
    this.saleOrderProductionSyncService = saleOrderProductionSyncService;
  }

  @Override
  public void onLineChange(SaleOrder saleOrder) throws AxelorException {
    super.onLineChange(saleOrder);

    if (appProductionService.isApp("production")) {
      saleOrderProductionSyncService.syncSaleOrderLineList(saleOrder);
    }
  }
}
