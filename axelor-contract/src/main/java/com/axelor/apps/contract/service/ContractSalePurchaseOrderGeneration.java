package com.axelor.apps.contract.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.persist.Transactional;

public interface ContractSalePurchaseOrderGeneration {
  SaleOrder generateSaleOrder(Contract contract) throws AxelorException;

  @Transactional
  PurchaseOrder generatePurchaseOrder(Contract contract) throws AxelorException;
}
