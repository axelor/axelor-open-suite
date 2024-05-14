package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineGroupServiceImpl implements SaleOrderLineGroupService {

  protected final XService xService;

  @Inject
  public SaleOrderLineGroupServiceImpl(XService xService) {
    this.xService = xService;
  }

  @Override
  public void getOnNewValuesMap(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    xService.setNonNegotiableValue(saleOrder, attrsMap);
    xService.hideQtyWarningLabel(attrsMap);
    xService.showPriceDiscounted(saleOrder, saleOrderLine, attrsMap);
    xService.setScaleAttrs(attrsMap);
    xService.setInitialQty(attrsMap);
    xService.setCompanyCurrencyValue(saleOrder, saleOrderLine, attrsMap);
    xService.setCurrencyValue(saleOrder, saleOrderLine, attrsMap);
    xService.manageHiddenAttrForPrices(saleOrder, attrsMap);
    xService.defineTypesToSelect(attrsMap);
    xService.setHiddenAttrForDeliveredQty(saleOrder, attrsMap);
    xService.displayAndSetLanguages(saleOrder, attrsMap);
    xService.initDummyFields(attrsMap);
  }
}
