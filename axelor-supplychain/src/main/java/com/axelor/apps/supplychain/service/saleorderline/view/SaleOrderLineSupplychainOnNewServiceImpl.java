package com.axelor.apps.supplychain.service.saleorderline.view;

import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.utils.MapTools;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineSupplychainOnNewServiceImpl
    implements SaleOrderLineSupplychainOnNewService {

  protected AnalyticAttrsService analyticAttrsService;
  protected SaleOrderLineSupplychainViewService saleOrderLineSupplychainViewService;

  @Inject
  public SaleOrderLineSupplychainOnNewServiceImpl(
      AnalyticAttrsService analyticAttrsService,
      SaleOrderLineSupplychainViewService saleOrderLineSupplychainViewService) {
    this.analyticAttrsService = analyticAttrsService;
    this.saleOrderLineSupplychainViewService = saleOrderLineSupplychainViewService;
  }

  @Override
  public Map<String, Map<String, Object>> getSupplychainOnNewAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    MapTools.addMap(attrs, saleOrderLineSupplychainViewService.hideSupplychainPanels(saleOrder));
    MapTools.addMap(attrs, saleOrderLineSupplychainViewService.hideDeliveredQty(saleOrder));
    MapTools.addMap(
        attrs, saleOrderLineSupplychainViewService.hideAllocatedQtyBtn(saleOrder, saleOrderLine));
    analyticAttrsService.addAnalyticAxisAttrs(saleOrder.getCompany(), null, attrs);
    MapTools.addMap(
        attrs,
        saleOrderLineSupplychainViewService.setAnalyticDistributionPanelHidden(
            saleOrder, saleOrderLine));
    MapTools.addMap(attrs, saleOrderLineSupplychainViewService.setReservedQtyReadonly(saleOrder));
    return attrs;
  }
}
