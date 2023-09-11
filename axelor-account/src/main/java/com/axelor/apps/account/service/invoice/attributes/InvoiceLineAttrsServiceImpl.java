package com.axelor.apps.account.service.invoice.attributes;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.ScaleServiceAccount;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class InvoiceLineAttrsServiceImpl implements InvoiceLineAttrsService {

  protected ScaleServiceAccount scaleServiceAccount;

  @Inject
  public InvoiceLineAttrsServiceImpl(ScaleServiceAccount scaleServiceAccount) {
    this.scaleServiceAccount = scaleServiceAccount;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void setPriceScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("price", "scale", scaleServiceAccount.getScale(invoice, false), attrsMap);
  }

  @Override
  public void setInTaxPriceScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("inTaxPrice", "scale", scaleServiceAccount.getScale(invoice, false), attrsMap);
  }

  @Override
  public void setDiscountAmountScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("discountAmount", "scale", scaleServiceAccount.getScale(invoice, false), attrsMap);
  }

  @Override
  public void setExTaxTotalScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("exTaxTotal", "scale", scaleServiceAccount.getScale(invoice, false), attrsMap);
  }

  @Override
  public void setInTaxTotalScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("inTaxTotal", "scale", scaleServiceAccount.getScale(invoice, false), attrsMap);
  }

  @Override
  public void setCompanyExTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "companyExTaxTotal", "scale", scaleServiceAccount.getScale(invoice, true), attrsMap);
  }

  @Override
  public void setCompanyInTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "companyInTaxTotal", "scale", scaleServiceAccount.getScale(invoice, true), attrsMap);
  }
}
