package com.axelor.apps.account.service.invoice.attributes;

import com.axelor.apps.account.db.Invoice;
import java.util.Map;

public interface InvoiceLineTaxAttrsService {

  void addExTaxBaseScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix);

  void addTaxTotalScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix);

  void addInTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix);
}
