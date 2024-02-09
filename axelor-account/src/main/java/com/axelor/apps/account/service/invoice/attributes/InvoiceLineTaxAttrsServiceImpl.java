package com.axelor.apps.account.service.invoice.attributes;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class InvoiceLineTaxAttrsServiceImpl implements InvoiceLineTaxAttrsService {

  protected CurrencyScaleService currencyScaleService;

  @Inject
  public InvoiceLineTaxAttrsServiceImpl(CurrencyScaleService currencyScaleService) {
    this.currencyScaleService = currencyScaleService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  protected String computeField(String field, String prefix) {
    return String.format("%s%s", prefix, field);
  }

  @Override
  public void addExTaxBaseScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("exTaxBase", prefix),
        "scale",
        currencyScaleService.getScale(invoice),
        attrsMap);
  }

  @Override
  public void addTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("taxTotal", prefix),
        "scale",
        currencyScaleService.getScale(invoice),
        attrsMap);
  }

  @Override
  public void addInTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("inTaxTotal", prefix),
        "scale",
        currencyScaleService.getScale(invoice),
        attrsMap);
  }
}
