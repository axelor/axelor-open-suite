package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.service.invoice.attributes.InvoiceTermPaymentAttrsService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class InvoiceTermPaymentGroupServiceImpl implements InvoiceTermPaymentGroupService {

  protected InvoiceTermPaymentAttrsService invoiceTermPaymentAttrsService;

  @Inject
  public InvoiceTermPaymentGroupServiceImpl(
      InvoiceTermPaymentAttrsService invoiceTermPaymentAttrsService) {
    this.invoiceTermPaymentAttrsService = invoiceTermPaymentAttrsService;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrsMap(InvoiceTermPayment invoiceTermPayment) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();
    InvoiceTerm invoiceTerm = invoiceTermPayment.getInvoiceTerm();

    if (invoiceTerm != null) {
      invoiceTermPaymentAttrsService.addIsMultiCurrency(invoiceTerm, attrsMap);
      invoiceTermPaymentAttrsService.addPaidAmountScale(invoiceTerm, attrsMap);
      invoiceTermPaymentAttrsService.addCompanyPaidAmountScale(invoiceTerm, attrsMap);
      invoiceTermPaymentAttrsService.addFinancialDiscountAmountScale(invoiceTerm, attrsMap);
    }

    return attrsMap;
  }
}
