package com.axelor.apps.sale.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.saleorder.attributes.SaleOrderAttrsService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderGroupServiceImpl implements SaleOrderGroupService {

  protected SaleOrderAttrsService saleOrderAttrsService;

  @Inject
  public SaleOrderGroupServiceImpl(SaleOrderAttrsService saleOrderAttrsService) {
    this.saleOrderAttrsService = saleOrderAttrsService;
  }

  @Override
  public Map<String, Map<String, Object>> onChangeSaleOrderLine(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    if (saleOrder != null && ObjectUtils.notEmpty(saleOrder.getSaleOrderLineList())) {
      saleOrderAttrsService.addIncotermRequired(saleOrder, attrsMap);
      saleOrderAttrsService.setSaleOrderLineScale(saleOrder, attrsMap);
      saleOrderAttrsService.setSaleOrderLineTaxScale(saleOrder, attrsMap);
    }

    return attrsMap;
  }
}
