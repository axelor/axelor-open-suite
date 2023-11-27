package com.axelor.apps.account.service.invoice.attributes;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.service.CurrencyScaleServiceAccount;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InvoiceTermPaymentAttrsServiceImpl implements InvoiceTermPaymentAttrsService {

  protected CurrencyScaleServiceAccount currencyScaleServiceAccount;

  @Inject
  public InvoiceTermPaymentAttrsServiceImpl(
      CurrencyScaleServiceAccount currencyScaleServiceAccount) {
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
  public void addIsMultiCurrency(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {
    boolean isMultiCurrency;

    if (invoiceTerm.getInvoice() != null) {
      isMultiCurrency =
          Objects.equals(
              invoiceTerm.getInvoice().getCurrency(),
              invoiceTerm.getInvoice().getCompany().getCurrency());
    } else {
      isMultiCurrency =
          Objects.equals(
              invoiceTerm.getMoveLine().getMove().getCurrency(),
              invoiceTerm.getMoveLine().getMove().getCompany().getCurrency());
    }

    this.addAttr("$isMultiCurrency", "value", isMultiCurrency, attrsMap);
  }

  @Override
  public void addPaidAmountScale(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "paidAmount",
        "scale",
        invoiceTerm.getInvoice() != null
            ? currencyScaleServiceAccount.getScale(invoiceTerm.getInvoice())
            : currencyScaleServiceAccount.getScale(invoiceTerm.getMoveLine()),
        attrsMap);
  }

  @Override
  public void addCompanyPaidAmountScale(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "companyPaidAmount",
        "scale",
        invoiceTerm.getInvoice() != null
            ? currencyScaleServiceAccount.getCompanyScale(invoiceTerm.getInvoice())
            : currencyScaleServiceAccount.getCompanyScale(invoiceTerm.getMoveLine()),
        attrsMap);
  }

  @Override
  public void addFinancialDiscountAmountScale(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "paidAmount",
        "scale",
        invoiceTerm.getInvoice() != null
            ? currencyScaleServiceAccount.getScale(invoiceTerm.getInvoice())
            : currencyScaleServiceAccount.getScale(invoiceTerm.getMoveLine()),
        attrsMap);
  }
}
