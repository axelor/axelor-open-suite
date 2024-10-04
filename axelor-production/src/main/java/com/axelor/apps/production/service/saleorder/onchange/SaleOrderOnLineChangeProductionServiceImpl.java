package com.axelor.apps.production.service.saleorder.onchange;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.service.SaleOrderProductionSyncService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComplementaryProductService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderShipmentService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.onchange.SaleOrderOnLineChangeSupplyChainServiceImpl;
import com.axelor.studio.db.repo.AppSaleRepository;
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
      SaleOrderComplementaryProductService saleOrderComplementaryProductService,
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
        saleOrderComplementaryProductService,
        saleOrderSupplychainService,
        saleOrderShipmentService);
    this.appProductionService = appProductionService;
    this.saleOrderProductionSyncService = saleOrderProductionSyncService;
  }

  @Override
  public void onLineChange(SaleOrder saleOrder) throws AxelorException {
    super.onLineChange(saleOrder);

    if (appProductionService.isApp("production")
        && appSaleService.getAppSale().getListDisplayTypeSelect()
            == AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_MULTI) {
      saleOrderProductionSyncService.syncSaleOrderLineList(saleOrder);
    }
  }
}
