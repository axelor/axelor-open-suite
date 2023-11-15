package com.axelor.apps.account.service.invoice.attributes;

import com.axelor.apps.account.db.Invoice;
import java.util.Map;

public interface InvoiceLineAttrsService {

  void setPriceScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap);

  void setInTaxPriceScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap);

  void setDiscountAmountScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap);

  void setExTaxTotalScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap);

  void setInTaxTotalScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap);

  void setCompanyExTaxTotalScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap);

  void setCompanyInTaxTotalScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap);
}
