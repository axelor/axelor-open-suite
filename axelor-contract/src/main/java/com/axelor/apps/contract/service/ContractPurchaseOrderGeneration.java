package com.axelor.apps.contract.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.purchase.db.PurchaseOrder;

public interface ContractPurchaseOrderGeneration {

  PurchaseOrder generatePurchaseOrder(Contract contract) throws AxelorException;
}
