package com.axelor.apps.purchase.service.attributes;

import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class PurchaseOrderAttrsServiceImpl implements PurchaseOrderAttrsService {

  protected CurrencyScaleService currencyScaleService;

  @Inject
  public PurchaseOrderAttrsServiceImpl(CurrencyScaleService currencyScaleService) {
    this.currencyScaleService = currencyScaleService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void setPurchaseOrderLineScale(
      PurchaseOrder purchaseOrder, Map<String, Map<String, Object>> attrsMap) {
    int currencyScale = currencyScaleService.getScale(purchaseOrder);

    this.addAttr("purchaseOrderLineList.exTaxTotal", "scale", currencyScale, attrsMap);
    this.addAttr("purchaseOrderLineList.inTaxTotal", "scale", currencyScale, attrsMap);
  }

  @Override
  public void setPurchaseOrderLineTaxScale(
      PurchaseOrder purchaseOrder, Map<String, Map<String, Object>> attrsMap) {
    int currencyScale = currencyScaleService.getScale(purchaseOrder);

    this.addAttr("purchaseOrderLineTaxList.inTaxTotal", "scale", currencyScale, attrsMap);
    this.addAttr("purchaseOrderLineTaxList.exTaxBase", "scale", currencyScale, attrsMap);
    this.addAttr("purchaseOrderLineTaxList.taxTotal", "scale", currencyScale, attrsMap);
  }

  @Override
  public Map<String, Map<String, Object>> onChangePurchaseOrderLine(PurchaseOrder purchaseOrder) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    if (purchaseOrder != null && ObjectUtils.notEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      this.setPurchaseOrderLineScale(purchaseOrder, attrsMap);
      this.setPurchaseOrderLineTaxScale(purchaseOrder, attrsMap);
    }

    return attrsMap;
  }
}
