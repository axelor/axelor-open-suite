package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.service.analytic.AnalyticGroupService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineInitValueServiceImpl;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineInitValueSupplychainServiceImpl
    extends SaleOrderLineInitValueServiceImpl {

  protected SupplyChainConfigService supplyChainConfigService;
  protected AnalyticGroupService analyticGroupService;

  @Inject
  public SaleOrderLineInitValueSupplychainServiceImpl(
      SupplyChainConfigService supplyChainConfigService,
      AnalyticGroupService analyticGroupService) {
    super();
    this.supplyChainConfigService = supplyChainConfigService;
    this.analyticGroupService = analyticGroupService;
  }

  @Override
  public Map<String, Object> onNewInitValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> values = super.onNewInitValues(saleOrder, saleOrderLine);
    values.putAll(fillRequestQty(saleOrder, saleOrderLine));
    values.putAll(fillAnalyticFields(saleOrder, saleOrderLine));
    return values;
  }

  @Override
  public Map<String, Object> onLoadInitValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> values = super.onLoadInitValues(saleOrder, saleOrderLine);

    values.putAll(fillAnalyticFields(saleOrder, saleOrderLine));

    return values;
  }

  protected Map<String, Object> fillRequestQty(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    if (saleOrder.getCompany() != null) {
      SupplyChainConfig supplyChainConfig =
          supplyChainConfigService.getSupplyChainConfig(saleOrder.getCompany());
      if (supplyChainConfig != null && supplyChainConfig.getAutoRequestReservedQty()) {
        values.put("isQtyRequested", true);
        values.put("requestedReservedQty", saleOrderLine.getQty());
      }
    }
    return values;
  }

  protected Map<String, Object> fillAnalyticFields(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);
    values.putAll(
        analyticGroupService.getAnalyticAccountValueMap(analyticLineModel, saleOrder.getCompany()));
    values.put("isQtyRequested", true);
    values.put("requestedReservedQty", saleOrderLine.getQty());

    return values;
  }
}
