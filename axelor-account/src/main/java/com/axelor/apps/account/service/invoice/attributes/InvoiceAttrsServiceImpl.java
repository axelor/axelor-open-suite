package com.axelor.apps.account.service.invoice.attributes;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.ScaleServiceAccount;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class InvoiceAttrsServiceImpl implements InvoiceAttrsService {

  protected ScaleServiceAccount scaleServiceAccount;

  @Inject
  public InvoiceAttrsServiceImpl(ScaleServiceAccount scaleServiceAccount) {
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
  public void setInvoiceLineScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap) {
    if (invoice != null && ObjectUtils.notEmpty(invoice.getInvoiceLineList())) {
      int currencyScale = scaleServiceAccount.getScale(invoice, false);

      this.addAttr("invoiceLineList.priceDiscounted", "scale", currencyScale, attrsMap);
      this.addAttr("invoiceLineList.inTaxPrice", "scale", currencyScale, attrsMap);
      this.addAttr("invoiceLineList.exTaxTotal", "scale", currencyScale, attrsMap);
      this.addAttr("invoiceLineList.inTaxTotal", "scale", currencyScale, attrsMap);
    }
  }

  @Override
  public void setInvoiceLineTaxScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap) {
    if (invoice != null && ObjectUtils.notEmpty(invoice.getInvoiceLineTaxList())) {
      int currencyScale = scaleServiceAccount.getScale(invoice, false);

      this.addAttr("invoiceLineTaxList.exTaxBase", "scale", currencyScale, attrsMap);
      this.addAttr("invoiceLineTaxList.taxTotal", "scale", currencyScale, attrsMap);
    }
  }
}
