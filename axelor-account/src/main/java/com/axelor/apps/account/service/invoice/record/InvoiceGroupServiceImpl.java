package com.axelor.apps.account.service.invoice.record;

import com.axelor.apps.account.db.Invoice;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class InvoiceGroupServiceImpl implements InvoiceGroupService {

  protected InvoiceAttrsService invoiceAttrsService;

  @Inject
  public InvoiceGroupServiceImpl(InvoiceAttrsService invoiceAttrsService) {
    this.invoiceAttrsService = invoiceAttrsService;
  }

  @Override
  public Map<String, Map<String, Object>> getHideCancelAttrsMap(Invoice invoice) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    invoiceAttrsService.hideCancelButton(invoice, attrsMap);

    return attrsMap;
  }
}
