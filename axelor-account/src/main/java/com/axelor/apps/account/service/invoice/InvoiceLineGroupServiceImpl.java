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
  public void setInvoiceLineScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap) {
    if (invoice != null && ObjectUtils.notEmpty(invoice.getInvoiceLineList())) {
      String prefix = "invoiceLineList";

      invoiceLineAttrsService.setInTaxPriceScale(invoice, attrsMap, prefix);
      invoiceLineAttrsService.setExTaxTotalScale(invoice, attrsMap, prefix);
      invoiceLineAttrsService.setInTaxTotalScale(invoice, attrsMap, prefix);
    }
  }
}
