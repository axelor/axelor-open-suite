package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;

public interface HoldBackLineService {

  Invoice generateInvoiceLinesForHoldBacks(Invoice invoice) throws AxelorException;
}
