package com.axelor.apps.supplychain.service.saleOrderLine;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineAttrsSetService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineGroupServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineRecordUpdateService;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineSupplyChainGroupServiceImpl extends SaleOrderLineGroupServiceImpl {

  protected final SaleOrderLineAttrsSetSupplychainService saleOrderLineAttrsSetSupplychainService;
  protected final SaleOrderLineRecordUpdateSupplyChainService
      saleOrderLineRecordUpdateSupplyChainService;

  @Inject
  public SaleOrderLineSupplyChainGroupServiceImpl(
      SaleOrderLineAttrsSetService saleOrderLineAttrsSetService,
      SaleOrderLineRecordUpdateService saleOrderLineRecordUpdateService,
      SaleOrderLineAttrsSetSupplychainService saleOrderLineAttrsSetSupplychainService,
      SaleOrderLineRecordUpdateSupplyChainService saleOrderLineRecordUpdateSupplyChainService) {
    super(saleOrderLineAttrsSetService, saleOrderLineRecordUpdateService);
    this.saleOrderLineAttrsSetSupplychainService = saleOrderLineAttrsSetSupplychainService;
    this.saleOrderLineRecordUpdateSupplyChainService = saleOrderLineRecordUpdateSupplyChainService;
  }

  @Override
  public void getOnNewValuesMap(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    super.getOnNewValuesMap(saleOrder, saleOrderLine, attrsMap);
    saleOrderLineAttrsSetSupplychainService.setIsReadOnlyValue(saleOrder, saleOrderLine, attrsMap);
    saleOrderLineAttrsSetSupplychainService.hideUpdateAllocatedQtyBtn(
        saleOrder, saleOrderLine, attrsMap);
    saleOrderLineAttrsSetSupplychainService.setRequestedReservedQtyToReadOnly(saleOrder, attrsMap);
    saleOrderLineRecordUpdateSupplyChainService.updateRequestedReservedQty(saleOrderLine, attrsMap);
  }
}
