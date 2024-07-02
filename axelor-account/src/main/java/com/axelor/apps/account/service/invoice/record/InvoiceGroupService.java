package com.axelor.apps.account.service.invoice.record;

import com.axelor.apps.account.db.Invoice;
import java.util.Map;

public interface InvoiceGroupService {
  Map<String, Map<String, Object>> getHideCancelAttrsMap(Invoice invoice);
}
