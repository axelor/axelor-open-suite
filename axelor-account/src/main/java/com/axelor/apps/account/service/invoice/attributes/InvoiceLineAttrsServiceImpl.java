package com.axelor.apps.account.service.invoice.attributes;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.CurrencyScaleServiceAccount;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class InvoiceLineAttrsServiceImpl implements InvoiceLineAttrsService {

  protected CurrencyScaleServiceAccount currencyScaleServiceAccount;

  @Inject
  public InvoiceLineAttrsServiceImpl(CurrencyScaleServiceAccount currencyScaleServiceAccount) {
    this.currencyScaleServiceAccount = currencyScaleServiceAccount;
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
        currencyScaleServiceAccount.getScale(invoice),
        attrsMap);
  }

  @Override
  public void addExTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("exTaxTotal", prefix),
        "scale",
        currencyScaleServiceAccount.getScale(invoice),
        attrsMap);
  }

  @Override
  public void addInTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("inTaxTotal", prefix),
        "scale",
        currencyScaleServiceAccount.getScale(invoice),
        attrsMap);
  }

  @Override
  public void addCompanyExTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("companyExTaxTotal", prefix),
        "scale",
        currencyScaleServiceAccount.getCompanyScale(invoice),
        attrsMap);
  }

  @Override
  public void addCompanyInTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("companyInTaxTotal", prefix),
        "scale",
        currencyScaleServiceAccount.getCompanyScale(invoice),
        attrsMap);
  }
}
