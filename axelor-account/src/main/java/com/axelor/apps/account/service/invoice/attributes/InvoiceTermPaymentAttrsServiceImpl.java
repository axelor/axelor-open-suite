package com.axelor.apps.account.service.invoice.attributes;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class InvoiceTermPaymentAttrsServiceImpl implements InvoiceTermPaymentAttrsService {

  protected CurrencyScaleService currencyScaleService;
  protected InvoiceTermService invoiceTermService;

  @Inject
  public InvoiceTermPaymentAttrsServiceImpl(
      CurrencyScaleService currencyScaleService, InvoiceTermService invoiceTermService) {
    this.currencyScaleService = currencyScaleService;
    this.invoiceTermService = invoiceTermService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void addIsMultiCurrency(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "$isMultiCurrency", "value", invoiceTermService.isMultiCurrency(invoiceTerm), attrsMap);
  }

  @Override
  public void addPaidAmountScale(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("paidAmount", "scale", currencyScaleService.getScale(invoiceTerm), attrsMap);
  }

  @Override
  public void addCompanyPaidAmountScale(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "companyPaidAmount", "scale", currencyScaleService.getCompanyScale(invoiceTerm), attrsMap);
  }

  @Override
  public void addFinancialDiscountAmountScale(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("paidAmount", "scale", currencyScaleService.getScale(invoiceTerm), attrsMap);
  }
}
