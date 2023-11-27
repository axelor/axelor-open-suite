package com.axelor.apps.account.service.invoice.attributes;

import com.axelor.apps.account.db.Invoice;
import java.util.Map;

public interface InvoiceLineTaxAttrsService {

  void setExTaxBaseScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix);

  void setTaxTotalScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix);

  void setInTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix);
}
