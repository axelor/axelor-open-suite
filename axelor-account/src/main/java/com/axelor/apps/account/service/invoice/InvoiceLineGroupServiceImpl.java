package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.attributes.InvoiceLineAttrsService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.Map;

public class InvoiceLineGroupServiceImpl implements InvoiceLineGroupService {

  protected InvoiceLineAttrsService invoiceLineAttrsService;

  @Inject
  public InvoiceLineGroupServiceImpl(InvoiceLineAttrsService invoiceLineAttrsService) {
    this.invoiceLineAttrsService = invoiceLineAttrsService;
  }

  @Override
  public void setInvoiceLineScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    if (invoice != null && ObjectUtils.notEmpty(invoice.getInvoiceLineList())) {
      invoiceLineAttrsService.addInTaxPriceScale(invoice, attrsMap, prefix);
      invoiceLineAttrsService.addExTaxTotalScale(invoice, attrsMap, prefix);
      invoiceLineAttrsService.addInTaxTotalScale(invoice, attrsMap, prefix);
    }
  }
}
