package com.axelor.apps.production.service;

import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderBlockingSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderCheckBlockingSupplychainServiceImpl;
import com.google.inject.Inject;
import java.util.List;

public class SaleOrderCheckBlockingServiceProductionServiceImpl
    extends SaleOrderCheckBlockingSupplychainServiceImpl {

  protected final SaleOrderBlockingProductionService saleOrderBlockingProductionService;
  protected final AppProductionService appProductionService;

  @Inject
  public SaleOrderCheckBlockingServiceProductionServiceImpl(
      SaleOrderBlockingSupplychainService saleOrderBlockingSupplychainService,
      AppSupplychainService appSupplychainService,
      SaleOrderBlockingProductionService saleOrderBlockingProductionService,
      AppProductionService appProductionService) {
    super(saleOrderBlockingSupplychainService, appSupplychainService);
    this.saleOrderBlockingProductionService = saleOrderBlockingProductionService;
    this.appProductionService = appProductionService;
  }

  @Override
  public List<String> checkBlocking(SaleOrder saleOrder) {
    var alertList = super.checkBlocking(saleOrder);

    if (saleOrderBlockingProductionService.hasOnGoingBlocking(saleOrder)
        && appProductionService.getAppProduction().getProductionOrderGenerationAuto()) {
      alertList.add(ProductionExceptionMessage.SALE_ORDER_LINES_CANNOT_PRODUCT);
    }

    return alertList;
  }
}
