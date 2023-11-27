package com.axelor.apps.account.service.invoice.attributes;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.CurrencyScaleServiceAccount;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class InvoiceLineTaxAttrsServiceImpl implements InvoiceLineTaxAttrsService {

  protected CurrencyScaleServiceAccount currencyScaleServiceAccount;

  @Inject
  public InvoiceLineTaxAttrsServiceImpl(CurrencyScaleServiceAccount currencyScaleServiceAccount) {
    this.currencyScaleServiceAccount = currencyScaleServiceAccount;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void setExTaxBaseScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("exTaxBase", prefix),
        "scale",
        currencyScaleServiceAccount.getScale(invoice),
        attrsMap);
  }

  @Override
  public void setTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("taxTotal", prefix),
        "scale",
        currencyScaleServiceAccount.getScale(invoice),
        attrsMap);
  }

  @Override
  public void setInTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("inTaxTotal", prefix),
        "scale",
        currencyScaleServiceAccount.getScale(invoice),
        attrsMap);
  }

  protected String computeField(String field, String prefix) {
    return prefix != null ? prefix.concat(".").concat(field) : field;
  }
}
