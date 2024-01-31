package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import java.util.Map;

public interface InvoiceLineTaxGroupService {

  void setInvoiceLineTaxScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix);
}
