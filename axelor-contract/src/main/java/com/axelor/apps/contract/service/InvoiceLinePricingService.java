package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;

public interface InvoiceLinePricingService {
  void computePricing(Invoice invoice) throws AxelorException;
}
