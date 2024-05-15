package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineGroupServiceImpl implements SaleOrderLineGroupService {

  protected final SaleOrderLineAttrsSetService saleOrderLineAttrsSetService;
  protected final SaleOrderLineRecordUpdateService saleOrderLineRecordUpdateService;

  @Inject
  public SaleOrderLineGroupServiceImpl(
      SaleOrderLineAttrsSetService saleOrderLineAttrsSetService,
      SaleOrderLineRecordUpdateService saleOrderLineRecordUpdateService) {
    this.saleOrderLineAttrsSetService = saleOrderLineAttrsSetService;
    this.saleOrderLineRecordUpdateService = saleOrderLineRecordUpdateService;
  }

  @Override
  public void getOnNewValuesMap(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    saleOrderLineRecordUpdateService.setNonNegotiableValue(saleOrder, attrsMap);
    saleOrderLineAttrsSetService.hideQtyWarningLabel(attrsMap);
    saleOrderLineAttrsSetService.showPriceDiscounted(saleOrder, saleOrderLine, attrsMap);
    saleOrderLineAttrsSetService.setScaleAttrs(attrsMap);
    saleOrderLineRecordUpdateService.setInitialQty(attrsMap);
    saleOrderLineRecordUpdateService.setCompanyCurrencyValue(saleOrder, saleOrderLine, attrsMap);
    saleOrderLineRecordUpdateService.setCurrencyValue(saleOrder, saleOrderLine, attrsMap);
    saleOrderLineAttrsSetService.manageHiddenAttrForPrices(saleOrder, attrsMap);
    saleOrderLineAttrsSetService.defineTypesToSelect(attrsMap);
    saleOrderLineAttrsSetService.setHiddenAttrForDeliveredQty(saleOrder, attrsMap);
    saleOrderLineAttrsSetService.displayAndSetLanguages(saleOrder, attrsMap);
    saleOrderLineRecordUpdateService.initDummyFields(attrsMap);
  }
}
