package com.axelor.apps.supplychain.service.saleorderline.view;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineProductSupplychainService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineOnSaleSupplyChangeServiceImpl
    implements SaleOrderLineOnSaleSupplyChangeService {

  protected SaleOrderLineProductSupplychainService saleOrderLineProductSupplychainService;

  @Inject
  public SaleOrderLineOnSaleSupplyChangeServiceImpl(
      SaleOrderLineProductSupplychainService saleOrderLineProductSupplychainService) {
    this.saleOrderLineProductSupplychainService = saleOrderLineProductSupplychainService;
  }

  @Override
  public Map<String, Object> onSaleSupplyChangeValues(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(
        saleOrderLineProductSupplychainService.getProductionInformation(saleOrderLine, saleOrder));
    saleOrderLineMap.putAll(
        saleOrderLineProductSupplychainService.setSupplierPartnerDefault(saleOrderLine, saleOrder));
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Map<String, Object>> onSaleSupplyChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    return attrs;
  }
}
