package com.axelor.apps.sale.service.saleorder.print;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderProductPrintingServiceImpl implements SaleOrderProductPrintingService {

  protected AppBaseService appBaseService;

  @Inject
  public SaleOrderProductPrintingServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public Map<String, Object> getGroupProductsOnPrintings(SaleOrder saleOrder) {
    Map<String, Object> saleOrderMap = new HashMap<>();
    AppBase appBase = appBaseService.getAppBase();
    saleOrder.setGroupProductsOnPrintings(appBase.getIsRegroupProductsOnPrintings());
    saleOrderMap.put("groupProductsOnPrintings", saleOrder.getGroupProductsOnPrintings());
    return saleOrderMap;
  }
}
