package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.SaleOrderLineAnalyticService;
import com.axelor.apps.supplychain.service.SaleOrderLineInitValueSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChain;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineInitValueProjectServiceImpl
    extends SaleOrderLineInitValueSupplychainServiceImpl {

  @Inject
  public SaleOrderLineInitValueProjectServiceImpl(
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
      AppSupplychainService appSupplychainService,
      SaleOrderLineAnalyticService saleOrderLineAnalyticService) {
    super(saleOrderLineServiceSupplyChain, appSupplychainService, saleOrderLineAnalyticService);
  }

  @Override
  public Map<String, Object> onNewInitValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> values = super.onNewInitValues(saleOrder, saleOrderLine);
    values.putAll(fillProject(saleOrder, saleOrderLine));
    return values;
  }

  @Override
  public Map<String, Object> onNewEditableInitValues(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> values = super.onNewEditableInitValues(saleOrder, saleOrderLine);
    values.putAll(fillProject(saleOrder, saleOrderLine));
    return values;
  }

  protected Map<String, Object> fillProject(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> values = new HashMap<>();
    Project project = saleOrder.getProject();
    saleOrderLine.setProject(project);
    values.put("project", project);
    return values;
  }
}
