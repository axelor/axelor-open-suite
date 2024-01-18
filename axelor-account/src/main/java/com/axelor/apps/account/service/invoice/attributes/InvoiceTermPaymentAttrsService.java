package com.axelor.apps.account.service.invoice.attributes;

import com.axelor.apps.account.db.InvoiceTerm;
import java.util.Map;

public interface InvoiceTermPaymentAttrsService {

  void addIsMultiCurrency(InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap);

  void addPaidAmountScale(InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap);

  void addCompanyPaidAmountScale(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap);

  void addFinancialDiscountAmountScale(
      InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap);
}
