package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.db.Contract;

public interface ContractYearEndBonusService {
  void invoiceYebContract(Contract contract, Invoice invoice) throws AxelorException;
}
