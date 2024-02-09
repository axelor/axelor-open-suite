package com.axelor.apps.account.service.invoice.attributes;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class InvoiceLineAttrsServiceImpl implements InvoiceLineAttrsService {

  protected CurrencyScaleService currencyScaleService;

  @Inject
  public InvoiceLineAttrsServiceImpl(CurrencyScaleService currencyScaleService) {
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
  public void addInTaxPriceScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("inTaxPrice", prefix),
        "scale",
        currencyScaleService.getScale(invoice),
        attrsMap);
  }

  @Override
  public void addExTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("exTaxTotal", prefix),
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

  @Override
  public void addCompanyExTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("companyExTaxTotal", prefix),
        "scale",
        currencyScaleService.getCompanyScale(invoice),
        attrsMap);
  }

  @Override
  public void addCompanyInTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("companyInTaxTotal", prefix),
        "scale",
        currencyScaleService.getCompanyScale(invoice),
        attrsMap);
  }
}
