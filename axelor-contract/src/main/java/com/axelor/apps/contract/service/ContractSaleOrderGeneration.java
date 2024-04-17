package com.axelor.apps.contract.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.sale.db.SaleOrder;

public interface ContractSaleOrderGeneration {
  SaleOrder generateSaleOrder(Contract contract) throws AxelorException;
}
