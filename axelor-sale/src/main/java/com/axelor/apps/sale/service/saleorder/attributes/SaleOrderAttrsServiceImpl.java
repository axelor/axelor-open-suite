package com.axelor.apps.sale.service.saleorder.attributes;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.CurrencyScaleServiceSale;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderAttrsServiceImpl implements SaleOrderAttrsService {

  protected CurrencyScaleServiceSale currencyScaleServiceSale;
  protected SaleOrderService saleOrderService;

  @Inject
  public SaleOrderAttrsServiceImpl(
      CurrencyScaleServiceSale currencyScaleServiceSale, SaleOrderService saleOrderService) {
    this.currencyScaleServiceSale = currencyScaleServiceSale;
    this.saleOrderService = saleOrderService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void setSaleOrderLineScale(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    int currencyScale = currencyScaleServiceSale.getScale(saleOrder);

    this.addAttr("saleOrderLineList.exTaxTotal", "scale", currencyScale, attrsMap);
    this.addAttr("saleOrderLineList.inTaxTotal", "scale", currencyScale, attrsMap);
  }

  @Override
  public void setSaleOrderLineTaxScale(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    int currencyScale = currencyScaleServiceSale.getScale(saleOrder);

    this.addAttr("saleOrderLineTaxList.inTaxTotal", "scale", currencyScale, attrsMap);
    this.addAttr("saleOrderLineTaxList.exTaxBase", "scale", currencyScale, attrsMap);
    this.addAttr("saleOrderLineTaxList.taxTotal", "scale", currencyScale, attrsMap);
  }

  @Override
  public void addIncotermRequired(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("incoterm", "required", saleOrderService.isIncotermRequired(saleOrder), attrsMap);
  }
}
