package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import java.util.Map;

public interface InvoiceLineGroupService {

  void setInvoiceLineScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix);
}
