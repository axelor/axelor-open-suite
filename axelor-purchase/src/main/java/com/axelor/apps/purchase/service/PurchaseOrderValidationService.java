package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;

public interface PurchaseOrderValidationService {
  public void checkNotOnlyNonDeductibleTaxes(PurchaseOrder purchaseOrder) throws AxelorException;
}
