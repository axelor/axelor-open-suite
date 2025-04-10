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
  protected SaleOrderLineViewSupplychainService saleOrderLineViewSupplychainService;

  @Inject
  public SaleOrderLineSupplychainOnNewServiceImpl(
      AnalyticAttrsService analyticAttrsService,
      SaleOrderLineViewSupplychainService saleOrderLineViewSupplychainService) {
    this.analyticAttrsService = analyticAttrsService;
    this.saleOrderLineViewSupplychainService = saleOrderLineViewSupplychainService;
  }

  @Override
  public Map<String, Map<String, Object>> getSupplychainOnNewAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    MapTools.addMap(attrs, saleOrderLineViewSupplychainService.hideSupplychainPanels(saleOrder));
    MapTools.addMap(attrs, saleOrderLineViewSupplychainService.hideDeliveredQty(saleOrder));
    MapTools.addMap(
        attrs, saleOrderLineViewSupplychainService.hideAllocatedQtyBtn(saleOrder, saleOrderLine));
    analyticAttrsService.addAnalyticAxisAttrs(saleOrder.getCompany(), null, attrs);
    MapTools.addMap(
        attrs,
        saleOrderLineViewSupplychainService.setAnalyticDistributionPanelHidden(
            saleOrder, saleOrderLine));
    MapTools.addMap(attrs, saleOrderLineViewSupplychainService.setReservedQtyReadonly(saleOrder));
    return attrs;
  }
}
