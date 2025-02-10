package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.util.Map;

public interface InvoiceGlobalDiscountService {

  void applyGlobalDiscountOnLines(Invoice invoice) throws AxelorException;

  BigDecimal computeDiscountFixedEquivalence(Invoice invoice);

  BigDecimal computeDiscountPercentageEquivalence(Invoice invoice);

  Map<String, Map<String, Object>> setDiscountDummies(Invoice invoice);
}
